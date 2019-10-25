package com.digitoll.erp.service;

import com.digitoll.commons.aggregation.AggregatedResults;
import com.digitoll.commons.dto.SaleAggregationDTO;
import com.digitoll.commons.enumeration.DateGroupingBases;
import com.digitoll.commons.enumeration.UserRole;
import com.digitoll.commons.exception.ExpiredAuthTokenException;
import com.digitoll.commons.exception.NoPosIdAssignedToUserException;
import com.digitoll.commons.exception.SaleRowIncompleteDataException;
import com.digitoll.commons.kapsch.classes.EVignetteProduct;
import com.digitoll.commons.kapsch.response.VignetteRegistrationResponse;
import com.digitoll.commons.aggregation.AggregatedResult;
import com.digitoll.commons.model.Partner;
import com.digitoll.commons.model.SaleRow;
import com.digitoll.commons.model.User;
import com.digitoll.commons.response.PaginatedRowsResponse;
import com.digitoll.commons.response.ProductsResponse;
import com.digitoll.commons.response.SaleRowDTO;
import com.digitoll.commons.util.DateTimeUtil;
import com.digitoll.erp.component.PdfComponent;
import com.digitoll.erp.component.TranslationComponent;
import com.digitoll.erp.repository.*;
import com.digitoll.erp.utils.ErpTestHelper;
import org.bson.types.Decimal128;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = SaleReportService.class)
@RunWith(SpringRunner.class)
public class SaleReportServiceTest {

    private static final int COUNT = 1;
    private static final BigDecimal AMOUNT = BigDecimal.valueOf(100);

    @MockBean
    private SaleRepository saleRepository;

    @MockBean
    private AggregationService aggregationService;

    @MockBean
    private SaleRowRepository saleRowRepository;

    @MockBean
    private PartnerRepository partnerRepository;

    @MockBean
    private TranslationComponent translationComponent;

    @MockBean
    private PdfComponent pdfComponent;

    @MockBean
    private KapschService kapschService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleService roleService;

    @MockBean
    private MongoTemplate mongoTemplate;

    @MockBean
    private HashMap<String, Partner> _mPartners;

    @Autowired
    private SaleReportService saleReportService;
    private ErpTestHelper erpTestHelper;

    @Before
    public void init() {
        erpTestHelper = new ErpTestHelper();
    }

    @Test
    public void getAdminSales() throws NoPosIdAssignedToUserException, ParseException {
        User user = erpTestHelper.createUser();
        user.getRoles().add(erpTestHelper.createRole(UserRole.ADMIN.getRoleCode()));
        List<SaleRow> listAll = new ArrayList<>();
        listAll.add(erpTestHelper.createSaleRow());
        listAll.add(erpTestHelper.createSaleRow());
        listAll.add(erpTestHelper.createSaleRow());

        when(userRepository.findByUsername(Mockito.anyString())).thenReturn(user);
        when(roleService.isUserAdmin(Mockito.any(User.class))).thenReturn(true);
        when(roleService.isUserPartnerAdmin(Mockito.any(User.class))).thenReturn(false);
        when(mongoTemplate.find(Mockito.any(Query.class), eq(SaleRow.class))).thenReturn(listAll);
        when(mongoTemplate.count(Mockito.any(Query.class), eq(SaleRow.class))).thenReturn((long) listAll.size());

        assertEquals(getSalesByCriteria(erpTestHelper.getPage(), user.getUsername()).getSaleRows().size(), listAll.size());
    }


    @Test
    public void getPartnerAdminSales() throws NoPosIdAssignedToUserException, ParseException {
        User user = erpTestHelper.createUser();
        user.getRoles().add(erpTestHelper.createRole(UserRole.PARTNER_ADMIN.getRoleCode()));
        List<SaleRow> partnerAdminSales = new ArrayList<>();
        SaleRow saleRow = erpTestHelper.createSaleRow();
        saleRow.setPartnerId(user.getPartnerId());
        partnerAdminSales.add(saleRow);
        PageRequest page = erpTestHelper.getPage();
        Query mockQuery = new Query();
        mockQuery.addCriteria(Criteria.where("partnerId").is(user.getPartnerId()));

        when(userRepository.findByUsername(Mockito.anyString())).thenReturn(user);
        when(roleService.isUserAdmin(Mockito.any(User.class))).thenReturn(false);
        when(roleService.isUserPartnerAdmin(Mockito.any(User.class))).thenReturn(true);
        when(mongoTemplate.find(mockQuery.with(page), SaleRow.class)).thenReturn(partnerAdminSales);
        when(mongoTemplate.count(Mockito.any(Query.class), eq(SaleRow.class))).thenReturn((long) partnerAdminSales.size());

        List<SaleRowDTO> result = getSalesByCriteria(page, user.getUsername()).getSaleRows();

        assertFalse(result.isEmpty());
        for (SaleRowDTO saleRowDTO : result) {
            assertEquals(saleRowDTO.getPartnerId(), user.getPartnerId());
        }
    }

    @Test
    public void getUserSales() throws NoPosIdAssignedToUserException, ParseException {
        User user = erpTestHelper.createUser();
        user.getRoles().add(erpTestHelper.createRole(UserRole.PARTNER_EMPLOYEE.getRoleCode()));
        List<SaleRow> userSales = new ArrayList<>();
        SaleRow saleRow = erpTestHelper.createSaleRow();
        saleRow.setPosId(ErpTestHelper.POS_ID);
        userSales.add(saleRow);
        PageRequest page = erpTestHelper.getPage();
        Query mockQuery = new Query();
        mockQuery.addCriteria(Criteria.where("posId").in(user.getPosIds()));

        when(userRepository.findByUsername(Mockito.anyString())).thenReturn(user);
        when(roleService.isUserAdmin(Mockito.any(User.class))).thenReturn(false);
        when(roleService.isUserPartnerAdmin(Mockito.any(User.class))).thenReturn(false);
        when(mongoTemplate.find(mockQuery.with(page), SaleRow.class)).thenReturn(userSales);
        when(mongoTemplate.count(Mockito.any(Query.class), eq(SaleRow.class))).thenReturn((long) userSales.size());

        List<SaleRowDTO> result = getSalesByCriteria(page, user.getUsername()).getSaleRows();

        assertFalse(result.isEmpty());
        for (SaleRowDTO saleRowDTO : result) {
            assertTrue(user.getPosIds().contains(saleRowDTO.getPosId()));
        }
    }

    @Test
    public void getUserSalesWrongPos() throws NoPosIdAssignedToUserException, ParseException {
        User user = erpTestHelper.createUser();
        user.getRoles().add(erpTestHelper.createRole(UserRole.PARTNER_EMPLOYEE.getRoleCode()));
        List<SaleRow> userSales = new ArrayList<>();
        SaleRow saleRow = erpTestHelper.createSaleRow();
        saleRow.setPosId(ErpTestHelper.POS_ID_2);
        userSales.add(saleRow);
        PageRequest page = erpTestHelper.getPage();
        Query mockQuery = new Query();
        mockQuery.addCriteria(Criteria.where("posId").in(
                new ArrayList<String>() {{
                    add("wrong");
                }}
        ));

        when(userRepository.findByUsername(Mockito.anyString())).thenReturn(user);
        when(roleService.isUserAdmin(Mockito.any(User.class))).thenReturn(false);
        when(roleService.isUserPartnerAdmin(Mockito.any(User.class))).thenReturn(false);
        when(mongoTemplate.find(mockQuery.with(page), SaleRow.class)).thenReturn(userSales);
        when(mongoTemplate.count(Mockito.any(Query.class), eq(SaleRow.class))).thenReturn((long) userSales.size());

        List<SaleRowDTO> result = getSalesByCriteria(page, user.getUsername()).getSaleRows();

        assertTrue(result.isEmpty());
    }

    @Test(expected = NoPosIdAssignedToUserException.class)
    public void getUserSalesNoPosException() throws NoPosIdAssignedToUserException, ParseException {
        User user = erpTestHelper.createUser();
        user.setPosIds(new ArrayList<>());
        PageRequest page = erpTestHelper.getPage();

        Query mockQuery = new Query();
        mockQuery.addCriteria(Criteria.where("posId").in(user.getPosIds()));

        when(userRepository.findByUsername(Mockito.anyString())).thenReturn(user);
        when(roleService.isUserAdmin(Mockito.any(User.class))).thenReturn(false);
        when(roleService.isUserPartnerAdmin(Mockito.any(User.class))).thenReturn(false);
        when(mongoTemplate.find(mockQuery.with(page), SaleRow.class)).thenReturn(new ArrayList<>());
        when(mongoTemplate.count(Mockito.any(Query.class), eq(SaleRow.class))).thenReturn((long) new ArrayList<>().size());

        getSalesByCriteria(page, user.getUsername());

    }

    @Test(expected = UsernameNotFoundException.class)
    public void getUserSalesUsernameNotFoundException() throws NoPosIdAssignedToUserException, ParseException {
        PageRequest page = erpTestHelper.getPage();

        when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);

        getSalesByCriteria(page, ErpTestHelper.USERNAME);

    }

    @Test
    public void testGetSalesByCriteria() throws ParseException, NoPosIdAssignedToUserException {
        User user = erpTestHelper.createUser();
        Query query = new Query();
        List<Criteria> criteriaList = mockAllCriteriaList();

        addCriteriaListToQuery(query, criteriaList);

        when(roleService.isUserPartnerAdmin(user)).thenReturn(true);
        when(userRepository.findByUsername(Mockito.anyString())).thenReturn(user);
        PageRequest pageRequest = erpTestHelper.getPage();

        SaleRow saleRow = erpTestHelper.createSaleRow();
        List<SaleRow> saleRowList = new ArrayList<>(1);
        saleRowList.add(saleRow);

        PaginatedRowsResponse expectedResponse = mockGetPaginationResponse(saleRow, false);

        when(mongoTemplate.find(query.with(pageRequest), SaleRow.class)).thenReturn(saleRowList);

        PaginatedRowsResponse response = saleReportService.getSalesByCriteria(
                erpTestHelper.modelFormatter.parse(ErpTestHelper.VALIDITY_START_DATE),
                erpTestHelper.modelFormatter.parse(ErpTestHelper.VALIDITY_END_DATE),
                ErpTestHelper.LPN,
                ErpTestHelper.PARTNER_ID,
                ErpTestHelper.POS_ID,
                ErpTestHelper.VIGNETTE_ID,
                ErpTestHelper.SALE_ID,
                ErpTestHelper.VEHICLE_ID,
                ErpTestHelper.USER_ID,
                ErpTestHelper.PARTNER_NAME,
                ErpTestHelper.POS_NAME,
                ErpTestHelper.USERNAME,
                ErpTestHelper.VIGNETTE_VALIDITY_TYPE,
                ErpTestHelper.EMAIL,
                ErpTestHelper.ACTIVE,
                erpTestHelper.modelFormatter.parse(ErpTestHelper.CREATED_ON),
                erpTestHelper.formatter.parse(ErpTestHelper.FROM_DATE),
                erpTestHelper.formatter.parse(ErpTestHelper.TO_DATE),
                erpTestHelper.formatter.parse(ErpTestHelper.FROM_DATE),
                erpTestHelper.formatter.parse(ErpTestHelper.TO_DATE),
                null,
                pageRequest,
                ErpTestHelper.SHOW_TOTAL_SUM,
                ErpTestHelper.NO_CATEGORY,
                ErpTestHelper.USERNAME);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testCategoryFilterSuccess() throws ParseException, NoPosIdAssignedToUserException {
        User user = erpTestHelper.createUser();
        Query query = new Query();
        List<Criteria> criteriaList = mockAllCriteriaList();
        criteriaList.add(Criteria.where("kapschProperties.product.vehicleType").is(ErpTestHelper.VEHICLE_TYPE_HGVN3));

        addCriteriaListToQuery(query, criteriaList);

        when(roleService.isUserPartnerAdmin(user)).thenReturn(true);
        when(userRepository.findByUsername(Mockito.anyString())).thenReturn(user);
        PageRequest pageRequest = erpTestHelper.getPage();

        SaleRow saleRow = erpTestHelper.createSaleRow();
        List<SaleRow> saleRowList = new ArrayList<>(1);
        saleRowList.add(saleRow);

        PaginatedRowsResponse expectedResponse = mockGetPaginationResponse(saleRow, false);
        mockGroupAggregation(criteriaList);
        when(mongoTemplate.find(query.with(pageRequest), SaleRow.class)).thenReturn(saleRowList);

        PaginatedRowsResponse response = saleReportService.getSalesByCriteria(erpTestHelper.modelFormatter.parse(ErpTestHelper.VALIDITY_START_DATE),
                erpTestHelper.modelFormatter.parse(ErpTestHelper.VALIDITY_END_DATE),
                ErpTestHelper.LPN,
                ErpTestHelper.PARTNER_ID,
                ErpTestHelper.POS_ID,
                ErpTestHelper.VIGNETTE_ID,
                ErpTestHelper.SALE_ID,
                ErpTestHelper.VEHICLE_ID,
                ErpTestHelper.USER_ID,
                ErpTestHelper.PARTNER_NAME,
                ErpTestHelper.POS_NAME,
                ErpTestHelper.USERNAME,
                ErpTestHelper.VIGNETTE_VALIDITY_TYPE,
                ErpTestHelper.EMAIL,
                ErpTestHelper.ACTIVE,
                erpTestHelper.modelFormatter.parse(ErpTestHelper.CREATED_ON),
                erpTestHelper.formatter.parse(ErpTestHelper.FROM_DATE),
                erpTestHelper.formatter.parse(ErpTestHelper.TO_DATE),
                erpTestHelper.formatter.parse(ErpTestHelper.FROM_DATE),
                erpTestHelper.formatter.parse(ErpTestHelper.TO_DATE),
                null,
                pageRequest,
                ErpTestHelper.SHOW_TOTAL_SUM,
                ErpTestHelper.CATEGORY_1,
                ErpTestHelper.USERNAME);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testCategoryFilterNoResult() throws ParseException, NoPosIdAssignedToUserException {
        User user = erpTestHelper.createUser();
        Query query = new Query();
        List<Criteria> criteriaList = mockAllCriteriaList();
        criteriaList.add(Criteria.where("kapschProperties.product.vehicleType").is(ErpTestHelper.VEHICLE_TYPE_HGVN3));

        addCriteriaListToQuery(query, criteriaList);

        when(roleService.isUserPartnerAdmin(user)).thenReturn(true);
        when(userRepository.findByUsername(Mockito.anyString())).thenReturn(user);
        PageRequest pageRequest = erpTestHelper.getPage();

        SaleRow saleRow = erpTestHelper.createSaleRow();
        List<SaleRow> saleRowList = new ArrayList<>(1);
        saleRowList.add(saleRow);

        mockGroupAggregation(criteriaList);
        when(mongoTemplate.find(query.with(pageRequest), SaleRow.class)).thenReturn(saleRowList);

        PaginatedRowsResponse response = saleReportService.getSalesByCriteria(erpTestHelper.modelFormatter.parse(ErpTestHelper.VALIDITY_START_DATE),
                erpTestHelper.modelFormatter.parse(ErpTestHelper.VALIDITY_END_DATE),
                ErpTestHelper.LPN,
                ErpTestHelper.PARTNER_ID,
                ErpTestHelper.POS_ID,
                ErpTestHelper.VIGNETTE_ID,
                ErpTestHelper.SALE_ID,
                ErpTestHelper.VEHICLE_ID,
                ErpTestHelper.USER_ID,
                ErpTestHelper.PARTNER_NAME,
                ErpTestHelper.POS_NAME,
                ErpTestHelper.USERNAME,
                ErpTestHelper.VIGNETTE_VALIDITY_TYPE,
                ErpTestHelper.EMAIL,
                ErpTestHelper.ACTIVE,
                erpTestHelper.modelFormatter.parse(ErpTestHelper.CREATED_ON),
                erpTestHelper.formatter.parse(ErpTestHelper.FROM_DATE),
                erpTestHelper.formatter.parse(ErpTestHelper.TO_DATE),
                erpTestHelper.formatter.parse(ErpTestHelper.FROM_DATE),
                erpTestHelper.formatter.parse(ErpTestHelper.TO_DATE),
                null,
                pageRequest,
                ErpTestHelper.SHOW_TOTAL_SUM,
                ErpTestHelper.CATEGORY_3,
                ErpTestHelper.USERNAME);

        assertTrue(response.getSaleRows().isEmpty());
    }

    @Test
    public void testGetSalesByCriteriaWithTotalSum() throws ParseException, NoPosIdAssignedToUserException {
        User user = erpTestHelper.createUser();
        Query query = new Query();
        List<Criteria> criteriaList = mockAllCriteriaList();

        when(roleService.isUserPartnerAdmin(user)).thenReturn(true);

        addCriteriaListToQuery(query, criteriaList);
        when(userRepository.findByUsername(Mockito.anyString())).thenReturn(user);
        PageRequest pageRequest = erpTestHelper.getPage();

        SaleRow saleRow = erpTestHelper.createSaleRow();
        List<SaleRow> saleRowList = new ArrayList<>(1);
        saleRowList.add(saleRow);

        mockGroupAggregation(criteriaList);
        PaginatedRowsResponse expectedResponse = mockGetPaginationResponse(saleRow, true);
        when(mongoTemplate.find(query.with(pageRequest), SaleRow.class)).thenReturn(saleRowList);

        PaginatedRowsResponse response = saleReportService.getSalesByCriteria(
                erpTestHelper.modelFormatter.parse(ErpTestHelper.VALIDITY_START_DATE),
                erpTestHelper.modelFormatter.parse(ErpTestHelper.VALIDITY_END_DATE),
                ErpTestHelper.LPN,
                ErpTestHelper.PARTNER_ID,
                ErpTestHelper.POS_ID,
                ErpTestHelper.VIGNETTE_ID,
                ErpTestHelper.SALE_ID,
                ErpTestHelper.VEHICLE_ID,
                ErpTestHelper.USER_ID,
                ErpTestHelper.PARTNER_NAME,
                ErpTestHelper.POS_NAME,
                ErpTestHelper.USERNAME,
                ErpTestHelper.VIGNETTE_VALIDITY_TYPE,
                ErpTestHelper.EMAIL,
                ErpTestHelper.ACTIVE,
                erpTestHelper.modelFormatter.parse(ErpTestHelper.CREATED_ON),
                erpTestHelper.formatter.parse(ErpTestHelper.FROM_DATE),
                erpTestHelper.formatter.parse(ErpTestHelper.TO_DATE),
                erpTestHelper.formatter.parse(ErpTestHelper.FROM_DATE),
                erpTestHelper.formatter.parse(ErpTestHelper.TO_DATE),
                null,
                pageRequest,
                true,
                ErpTestHelper.NO_CATEGORY,
                ErpTestHelper.USERNAME);

        assertEquals(expectedResponse.getTotalSum(), response.getTotalSum());
    }

    private void addCriteriaListToQuery(Query query, List<Criteria> criteriaList) {
        for (Criteria criteria : criteriaList) {
            query.addCriteria(criteria);
        }
    }

    private List<Criteria> mockAllCriteriaList() throws ParseException {

        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("kapschProperties.validity.validityStartDateTimeUTC")
                .gte(DateTimeUtil.getStartOfDay(erpTestHelper.modelFormatter
                        .parse(ErpTestHelper.VALIDITY_START_DATE)))
                .lt(DateTimeUtil.getEndOfDay(erpTestHelper.modelFormatter.parse(ErpTestHelper.VALIDITY_END_DATE))));
        criteriaList.add(Criteria.where("createdOn")
                .gte(erpTestHelper.formatter.parse(ErpTestHelper.FROM_DATE))
                .lte(erpTestHelper.formatter.parse(ErpTestHelper.TO_DATE)));
        criteriaList.add(Criteria.where("kapschProperties.purchase.purchaseDateTimeUTC")
                .exists(true)
                .gte(erpTestHelper.formatter.parse(ErpTestHelper.FROM_DATE))
                .lte(erpTestHelper.formatter.parse(ErpTestHelper.TO_DATE)));
        criteriaList.add(Criteria.where("partnerId").is(ErpTestHelper.PARTNER_ID));
        criteriaList.add(Criteria.where("posId").is(ErpTestHelper.POS_ID));
        criteriaList.add(Criteria.where("vignetteId").is(ErpTestHelper.VIGNETTE_ID));
        criteriaList.add(Criteria.where("saleId").is(ErpTestHelper.SALE_ID));
        criteriaList.add(Criteria.where("vehicleId").is(ErpTestHelper.VEHICLE_ID));
        criteriaList.add(Criteria.where("userId").is(ErpTestHelper.USER_ID));
        criteriaList.add(Criteria.where("partnerName").regex(ErpTestHelper.PARTNER_NAME, "i"));
        criteriaList.add(Criteria.where("posName").regex(ErpTestHelper.POS_NAME, "i"));
        criteriaList.add(Criteria.where("userName").regex(ErpTestHelper.USERNAME, "i"));
        criteriaList.add(Criteria.where("lpn").regex(ErpTestHelper.LPN, "i"));
        criteriaList.add(Criteria.where("email").regex(ErpTestHelper.EMAIL, "i"));
        criteriaList.add(Criteria.where("validityType").is(ErpTestHelper.VIGNETTE_VALIDITY_TYPE.name()));
        criteriaList.add(Criteria.where("active").is(ErpTestHelper.ACTIVE));
        return criteriaList;
    }

    private void mockGroupAggregation(List<Criteria> criteriaList) {
        List<SaleAggregationDTO> aggregatedResultList = new ArrayList<>(1);

        AggregatedResult aggregatedResult = new AggregatedResult();
        aggregatedResult.setTotalAmount(new Decimal128(AMOUNT));
        aggregatedResult.setCount(COUNT);
        aggregatedResult.setKapschProductId(ErpTestHelper.KAPSCH_PRODUCT_ID);

        aggregatedResultList.add(new SaleAggregationDTO(aggregatedResult));
        AggregatedResults results = new AggregatedResults();
        results.setResults(aggregatedResultList);
        results.setTotalElements((long) aggregatedResultList.size());
        results.setTotalPages(1);
        results.setTotalCount(COUNT);
        results.setTotalSum(AMOUNT);

        when(aggregationService.getAggregationForAmount(eq(new String[]{}), eq(criteriaList), eq(SaleRow.class), eq(DateGroupingBases.MONTHLY)))
                .thenReturn(results);
    }

    @Test
    public void testGetSaleRowsForUser() throws ParseException {
        PageRequest pageRequest = erpTestHelper.getPage();

        SaleRow saleRow = erpTestHelper.createSaleRow();

        PaginatedRowsResponse expectedResponse = mockGetPaginationResponse(saleRow, false);

        List<SaleRow> saleRowList = new ArrayList<>(1);
        saleRowList.add(saleRow);

        Page<SaleRow> saleRowsPage = new PageImpl<>(saleRowList);
        when(saleRowRepository.findByUserNameAndActive(ErpTestHelper.USERNAME, true, pageRequest))
                .thenReturn(saleRowsPage);

        PaginatedRowsResponse response = saleReportService.getSaleRowsForUser(ErpTestHelper.USERNAME, pageRequest);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testGetSaleByEmail() throws ParseException {
        PageRequest pageRequest = erpTestHelper.getPage();

        SaleRow saleRow = erpTestHelper.createSaleRow();

        PaginatedRowsResponse expectedResponse = mockGetPaginationResponse(saleRow, false);

        List<SaleRow> saleRowList = new ArrayList<>(1);
        saleRowList.add(saleRow);

        Page<SaleRow> saleRowsPage = new PageImpl<>(saleRowList);

        when(saleRowRepository.findByEmailAndActive(ErpTestHelper.EMAIL, true, pageRequest)).thenReturn(saleRowsPage);

        PaginatedRowsResponse response = saleReportService.getSaleByEmail(ErpTestHelper.EMAIL, pageRequest);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testGetSalePages() throws ParseException {
        PageRequest pageRequest = erpTestHelper.getPage();

        SaleRow saleRow = erpTestHelper.createSaleRow();

        PaginatedRowsResponse expectedResponse = mockGetPaginationResponse(saleRow, false);

        List<SaleRow> saleRowList = new ArrayList<>(1);
        saleRowList.add(saleRow);

        Page<SaleRow> saleRowsPage = new PageImpl<>(saleRowList);

        when(saleRowRepository.findAllByActive(true, pageRequest)).thenReturn(saleRowsPage);


        PaginatedRowsResponse response = saleReportService.getSalePages(pageRequest);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testGetTranslatedProductDescription() throws ParseException {
        SaleRowDTO saleRowDTO = erpTestHelper.createSaleRowDTO();

        EVignetteProduct product = saleRowDTO.getKapschProperties().getProduct();
        ProductsResponse expectedResponse = erpTestHelper.createProductResponse();

        when(translationComponent.translateProduct(product, null)).thenReturn(expectedResponse);

        ProductsResponse response = saleReportService.getTranslatedProductDescription(saleRowDTO, null);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testGeneratePdf() throws SaleRowIncompleteDataException, ExpiredAuthTokenException, IOException, ParseException {
        SaleRow row = erpTestHelper.createSaleRow();
        VignetteRegistrationResponse searchResponse = erpTestHelper.createVignetteRegistrationResponse();
        when(saleRowRepository.findOneByVignetteId(ErpTestHelper.VIGNETTE_ID)).thenReturn(row);
        SaleRowDTO expected = new SaleRowDTO(row);
        HttpSession httpSession = mock(HttpSession.class);
        when(kapschService.vignetteInfo(row.getVignetteId(), httpSession)).thenReturn(searchResponse);
        User user = erpTestHelper.createUser();

        when(userRepository.findOneById(ErpTestHelper.USER_ID)).thenReturn(user);

        List<Partner> partners = new ArrayList<>();
        partners.add(erpTestHelper.createPartnerDTO());

        when(partnerRepository.findAll()).thenReturn(partners);

        ProductsResponse response = erpTestHelper.createProductResponse();

        expected.setProductsResponse(response);
        when(translationComponent.translateProduct(row.getKapschProperties().getProduct(), null))
                .thenReturn(response);

        saleReportService.generatePdf(ErpTestHelper.VIGNETTE_ID, httpSession);

        verify(pdfComponent).generatePdfForSaleRow(null, expected);
    }

    private PaginatedRowsResponse mockGetPaginationResponse(SaleRow saleRow, boolean showTotalSum) throws ParseException {
        List<SaleRowDTO> saleRowDTOList = new ArrayList<>(1);

        SaleRowDTO saleRowDTO = erpTestHelper.createSaleRowDTO();
        saleRowDTO.setVehicle(null);

        saleRowDTOList.add(saleRowDTO);

        PaginatedRowsResponse expectedResponse = new PaginatedRowsResponse();
        expectedResponse.setTotalElements(ErpTestHelper.TOTAL_ELEMENTS);
        expectedResponse.setTotalPages(ErpTestHelper.TOTAL_PAGES);
        if (showTotalSum) {
            expectedResponse.setTotalSum(AMOUNT);
        }
        expectedResponse.setSaleRows(saleRowDTOList);

        ProductsResponse productsResponse = erpTestHelper.createProductResponse();

        when(translationComponent.translateProduct(saleRow.getKapschProperties().getProduct(), null))
                .thenReturn(productsResponse);

        return expectedResponse;
    }

    private PaginatedRowsResponse getSalesByCriteria(PageRequest page, String username) throws NoPosIdAssignedToUserException {
        return saleReportService.getSalesByCriteria(null, null, null, null,
                null, null, null, null, null, null, null,
                null, null, null, null, null, null, null,
                null, null, null,
                page, ErpTestHelper.SHOW_TOTAL_SUM, 0, username
        );
    }

}
