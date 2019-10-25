package com.digitoll.erp.utils;

import com.digitoll.commons.aggregation.AggregatedResult;
import com.digitoll.commons.aggregation.AggregatedResults;
import com.digitoll.commons.dto.*;
import com.digitoll.commons.enumeration.EmissionClass;
import com.digitoll.commons.enumeration.VehicleType;
import com.digitoll.commons.enumeration.VignetteValidityType;
import com.digitoll.commons.kapsch.classes.EVignetteInventoryProduct;
import com.digitoll.commons.kapsch.classes.EVignetteProduct;
import com.digitoll.commons.kapsch.classes.VignettePurchase;
import com.digitoll.commons.kapsch.classes.VignetteValidity;
import com.digitoll.commons.kapsch.request.VignetteRegistrationRequest;
import com.digitoll.commons.kapsch.response.BatchActivationResponse;
import com.digitoll.commons.kapsch.response.VignetteRegistrationResponse;
import com.digitoll.commons.kapsch.response.VignetteRegistrationResponseContent;
import com.digitoll.commons.model.*;
import com.digitoll.commons.request.AggregationRequest;
import com.digitoll.commons.request.SaleRequest;
import com.digitoll.commons.request.SaleRowRequest;
import com.digitoll.commons.response.ProductsResponse;
import com.digitoll.commons.response.SaleDTO;
import com.digitoll.commons.response.SaleRowDTO;
import com.digitoll.commons.util.BasicUtils;
import org.bson.types.Decimal128;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

public class ErpTestHelper {

    public static final String CODE = "code";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String USERNAME = "username@mail.bg";
    public static final String PASSWORD = "Password1234";

    public static final String POS_ID = "posId";
    public static final String POS_ID_2 = "posId2";
    public static final String POS_ID_3 = "posId3";
    public static final String PARTNER_ID = "partnerId";
    public static final int SALES_PARTNER_ID = 123;

    public static final int CATEGORY_3 = 3;
    public static final int NO_CATEGORY = 0;
    public static final int CATEGORY_1 = 1;

    public static final String COUNTRY_CODE = "countryCode";
    public static final String LPN = "lpn";
    public static final int KAPSCH_PRODUCT_ID = 2;
    public static final String EMAIL = "mail@mail.bg";

    public static final String PRINCIPAL_NAME = "principalName";
    public static final String USER_ID = "userId";

    public static final String BANK_TRANSACTION_ID = "bankTransactionID";
    public static final String COMPANY_CITY = "companyCity";
    public static final String COMPANY_COUNTRY = "companyCountry";
    public static final String COMPANY_ID_NUMBER = "companyIdNumber";
    public static final String COMPANY_NAME = "companyName";
    public static final String COMPANY_STREET = "companyStreet";
    public static final boolean FAILED = false;
    public static final boolean ACTIVE = true;
    public static final String FAILURE_MESSAGE = "failureMessage";
    public static final String LANGUAGE = "en";
    public static final String NAMES = "names";
    public static final String PARTNER_NAME = "partnerName";
    public static final String POS_NAME = "posName";
    public static final long SALE_SEQ = 3;
    public static final BigDecimal TOTAL = new BigDecimal("4.1");
    public static final String VAT_ID = "vatId";
    public static final String VIGNETTE_ID = "2342342423";
    public static final String VIGNETTE_ID_2 = "9843948938";
    public static final String CATEGORY_DESCRIPTION = "categoryDescription";
    public static final String EMISSION_CLASS_TEXT = "emissionClassText";
    public static final String VIGNETTE_VALIDITY_TYPE_TEXT = "vignetteValidityTypeText";
    public static final String VIGNETTE_VEHICLE_TYPE_TEXT = "vignetteVehicleTypeText";
    public static final String SORT_PROPERTY = "vignetteId";
    public static final boolean SHOW_TOTAL_SUM = false;
    public static final EmissionClass EMISSION_CLASS = EmissionClass.eur0;
    public static final String VEHICLE_ID = "vehicleId";
    public static final VehicleType VEHICLE_TYPE_HGVN3 = VehicleType.hgvn3;
    public static final VehicleType VEHICLE_TYPE_CAR = VehicleType.car;
    public static final boolean FAILED_KAPCSH_TRANS = false;
    public static final int PRODUCT_ID = 6;
    public static final BigDecimal PRODUCT_PRICE = new BigDecimal("10.0");
    public static final Double VIGNETTE_SEARCH_PRICE = 10.0;
    public static final Integer VIGNETTE_STATUS_ACTIVE = 2;

    public static final VignetteValidityType VIGNETTE_VALIDITY_TYPE = VignetteValidityType.month;

    public static final long SALE_SEQUENCE = 5;
    public static final long TOTAL_ELEMENTS = 1;
    public static final int TOTAL_PAGES = 1;
    public static final int PAGE_NUMBER = 0;
    public static final int PAGE_SIZE = 10;
    public static final Integer AGGREGATED_COUNT = 3;
    public static final Decimal128 AMOUNT = new Decimal128(100);
    public static final String DESCRIPTION = "Product description";
    public static final String PURCHASE_DATE = "26-09-2019";
    public static final int ACTIVE_STATUS = 2;

    public SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    public SimpleDateFormat modelFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    public static final String CREATED_AT = "2015-09-26T09:30:00.000+0000";
    public static final String CREATED_ON = "2015-09-26T08:30:00+0000";
    public static final String VALIDITY_START_DATE = "2015-08-26T08:30:00+0000";
    public static final String ACTIVATION_DATE_STRING = "2015-08-26T08:30:00+0000";
    public static final String VALIDITY_END_DATE = "2015-09-26T08:30:00+0000";

    public static final String CREATED_ON_PARAM = "2015-09-26T08:30:00.000+0000";
    public static final String VALIDITY_START_DATE_PARAM = "2015-08-26T08:30:00.000+0000";
    public static final String VALIDITY_END_DATE_PARAM = "2015-09-26T08:30:00.000+0000";
    public static final String SALE_ID = "saleId";
    public static final String TRANSACTION_ID = "transactionId";

    public static final String FROM_DATE = "2015-09-21T09:30:00.000+0000";
    public static final String TO_DATE = "2015-09-26T09:30:00.000+0000";

    public static final String SEQUENCE_NAME = "sales_sequence";

    public static final String REMOTE_CLIENT_ID = "remote_client_id";

    public static final int PAGE = 1;
    public static final int SIZE = 1;
    public static final Sort.Direction SORTING_DIRECTION = Sort.Direction.DESC;

    public static final LocalDateTime ACTIVATION_DATE =
            LocalDateTime.of(2016, Month.JULY, 29, 19, 30, 40);

    public static final Currency CURRENCY = Currency.getInstance("EUR");
    public static final String SEARCH_CURRENCY = "EUR";

    public static final String POS_TERMINAL_ADDRESS = "address";
    public static final String POS_TERMINAL_LAT = "lat";
    public static final String POS_TERMINAL_LNG = "lat";
    public static final String POS_TERMINAL_KEY = "key";

    public Principal createPrincipal() {
        return () -> PRINCIPAL_NAME;
    }

    public SaleDTO createSaleDTO() throws ParseException {
        SaleDTO saleDTO = new SaleDTO();

        saleDTO.setActive(ACTIVE);
        saleDTO.setBankTransactionId(BANK_TRANSACTION_ID);
        saleDTO.setCompanyCity(COMPANY_CITY);
        saleDTO.setCompanyCountry(COMPANY_COUNTRY);
        saleDTO.setCompanyIdNumber(COMPANY_ID_NUMBER);
        saleDTO.setCompanyName(COMPANY_NAME);
        saleDTO.setCompanyStreet(COMPANY_STREET);
        saleDTO.setCreatedOn(modelFormatter.parse(CREATED_ON));
        saleDTO.setEmail(EMAIL);
        saleDTO.setFailed(FAILED);
        saleDTO.setFailureMessage(FAILURE_MESSAGE);
        saleDTO.setId(SALE_ID);
        saleDTO.setLanguage(LANGUAGE);
        saleDTO.setNames(NAMES);
        saleDTO.setPartnerName(PARTNER_NAME);
        saleDTO.setPartnerId(PARTNER_ID);
        saleDTO.setPosName(POS_NAME);
        saleDTO.setSaleSeq(SALE_SEQ);
        saleDTO.setTotal(TOTAL);
        saleDTO.setUserName(USERNAME);
        saleDTO.setUserId(USER_ID);
        saleDTO.setVatId(VAT_ID);
        saleDTO.setPosId(POS_ID);
//        VignettePrice vignettePrice = new VignettePrice();
//        vignettePrice.setAmount(TOTAL);
//        vignettePrice.setCurrency(CURRENCY);

        List<SaleRowDTO> saleRowRequests = new ArrayList<>(1);

        SaleRowDTO saleRowDTO = createSaleRowDTO();

        saleRowRequests.add(saleRowDTO);
        saleDTO.setSaleRows(saleRowRequests);

        return saleDTO;
    }

    public Role createRole(String code) throws ParseException {
        Role role = new Role();
        role.setCode(code);
        role.setCreatedAt(formatter.parse(CREATED_AT));
        role.setId(ID);
        role.setName(NAME);
        return role;
    }

    public PartnerDTO createPartnerDTO() {
        PartnerDTO partner = new PartnerDTO();
        partner.setId(PARTNER_ID);
        partner.setName(PARTNER_NAME);
        partner.setKapschPartnerId(PARTNER_ID);
        return partner;
    }

    public Partner createPartner() {
        return new Partner(createPartnerDTO());
    }

    public Sale createSale() throws ParseException {
        Sale sale = new Sale();
        SaleDTO saleDto = this.createSaleDTO();
        BasicUtils.copyNonNullProps(saleDto, sale);

        return sale;
    }

    public Vehicle createVehicle() {

        Vehicle vehicle = new Vehicle();
        vehicle.setCountryCode(COUNTRY_CODE);
        vehicle.setEmissionClass(EMISSION_CLASS);
        vehicle.setId(VEHICLE_ID);
        vehicle.setLpn(LPN);
        vehicle.setType(VEHICLE_TYPE_HGVN3);
        vehicle.setUsername(USERNAME);

        return vehicle;
    }

    public VignettePrice createVignettePrice() {

        VignettePrice vignettePrice = new VignettePrice();
        vignettePrice.setAmount(TOTAL);
        vignettePrice.setCurrency(CURRENCY);

        return vignettePrice;
    }

    public EVignetteProduct createEvignetteProduct() {
        EVignetteProduct product = new EVignetteProduct();
        product.setEmissionClass(EMISSION_CLASS);
        product.setId(PRODUCT_ID);
        product.setValidityType(VIGNETTE_VALIDITY_TYPE);
        product.setVehicleType(VEHICLE_TYPE_HGVN3);

        return product;
    }

    public EVignetteInventoryProduct createEVignetteInventoryProduct() {
        EVignetteInventoryProduct invProduct = new EVignetteInventoryProduct();
        EVignetteProduct product = this.createEvignetteProduct();
        VignettePrice vignettePrice = this.createVignettePrice();

        BasicUtils.copyNonNullProps(product, invProduct);

        invProduct.setPrice(vignettePrice);

        return invProduct;
    }

    public SaleRowDTO createSaleRowDTO() throws ParseException {
        SaleRowDTO saleRowDTO = new SaleRowDTO();

        Vehicle vehicle = this.createVehicle();
        VignettePrice vignettePrice = this.createVignettePrice();
        ProductsResponse productsResponse = this.createProductResponse();

        VignetteRegistrationResponseContent registrationResponseContent = createVignetteRegistrationResponseContent(VIGNETTE_ID);

        saleRowDTO.setProductsResponse(productsResponse);
        saleRowDTO.setVehicle(vehicle);
        saleRowDTO.setActive(ACTIVE);
        saleRowDTO.setCreatedOn(modelFormatter.parse(CREATED_ON));
        saleRowDTO.setEmail(EMAIL);
        saleRowDTO.setFailedKapschTrans(FAILED_KAPCSH_TRANS);
        saleRowDTO.setFailureMessage(FAILURE_MESSAGE);
        saleRowDTO.setKapschProductId(KAPSCH_PRODUCT_ID);
        saleRowDTO.setKapschProperties(registrationResponseContent);
        saleRowDTO.setLpn(LPN);
        saleRowDTO.setPartnerId(PARTNER_ID);
        saleRowDTO.setPosId(POS_ID);
        saleRowDTO.setPartnerName(PARTNER_NAME);
        saleRowDTO.setPrice(vignettePrice);
        saleRowDTO.setActivationDate(ACTIVATION_DATE);
        saleRowDTO.setSaleId(SALE_ID);
        saleRowDTO.setSaleSequence(SALE_SEQUENCE);
        saleRowDTO.setUserName(USERNAME);
        saleRowDTO.setVignetteId(VIGNETTE_ID);
        saleRowDTO.setValidityType(VIGNETTE_VALIDITY_TYPE);
        saleRowDTO.setValidityStartDate(modelFormatter.parse(VALIDITY_START_DATE));
        saleRowDTO.setValidityEndDate(modelFormatter.parse(VALIDITY_END_DATE));
        saleRowDTO.setUserId(USER_ID);
        saleRowDTO.setPosName(POS_NAME);

        return saleRowDTO;
    }

    public TransactionIdDTO createTransactionIdDTO() {
        TransactionIdDTO transactionIdDTO = new TransactionIdDTO();
        transactionIdDTO.setSaleId(SALE_ID);
        transactionIdDTO.setTransactionId(TRANSACTION_ID);
        return transactionIdDTO;
    }

    public PageRequest getPage() {
        return PageRequest.of(PAGE_NUMBER, PAGE_SIZE);
    }

    public User createUser() throws ParseException {
        return createUser(USERNAME,FIRST_NAME,LAST_NAME,PASSWORD,USER_ID );
    }

     public User createUser ( String username , String firstName, String lastName, String password, String userId) throws ParseException {
         List<Role> roles = new ArrayList<>(1);
         roles.add(createRole(CODE));
         List<String> posIds = new ArrayList<>();
         posIds.add(POS_ID);

         User user = new User();
         user.setActive(ACTIVE);
         user.setCreatedAt(formatter.parse(CREATED_AT));
         user.setId(userId);
         user.setFirstName(firstName);
         user.setLastName(lastName);
         user.setPartnerId(PARTNER_ID);
         user.setPassword(password);
         user.setPosIds(posIds);
         user.setRoles(roles);
         user.setUsername(username);
         return  user;
     }

    public SaleRequest createSaleRequest() {
        SaleRowRequest saleRowRequest = new SaleRowRequest();
        Vehicle.KapschVehicle kapschVehicle = new Vehicle.KapschVehicle(LPN, COUNTRY_CODE);

        saleRowRequest.setActivationDate(ACTIVATION_DATE);
        saleRowRequest.setEmail(EMAIL);
        saleRowRequest.setKapschProductId(KAPSCH_PRODUCT_ID);
        saleRowRequest.setVehicle(kapschVehicle);

        List<SaleRowRequest> saleRowRequests = new ArrayList<>(1);
        saleRowRequests.add(saleRowRequest);
        SaleRequest saleRequest = new SaleRequest();
        saleRequest.setSaleRows(saleRowRequests);
        saleRequest.setPosId(POS_ID);

        return saleRequest;
    }

    public VignetteValidity createVignetteValidity() throws ParseException {
        VignetteValidity validity = new VignetteValidity();

        validity.setRequestedValidityStartDate(modelFormatter.parse(VALIDITY_START_DATE));
        validity.setValidityStartDateTimeEET(modelFormatter.parse(VALIDITY_START_DATE));
        validity.setValidityEndDateTimeEET(modelFormatter.parse(VALIDITY_START_DATE));
        validity.setValidityEndDateTimeUTC(modelFormatter.parse(VALIDITY_START_DATE));
        validity.setValidityStartDateTimeUTC(modelFormatter.parse(VALIDITY_START_DATE));

        return validity;
    }

    public VignettePurchase createVignettePurchase() throws ParseException {
        VignettePurchase purchase = new VignettePurchase();
        purchase.setPurchaseDateTimeUTC(modelFormatter.parse(VALIDITY_START_DATE));

        return purchase;
    }

    public BatchActivationResponse createBatchActivationResponse() throws ParseException {

        BatchActivationResponse response = new BatchActivationResponse();

        VignetteRegistrationResponseContent content1, content2;
        content1 = this.createVignetteRegistrationResponseContent(VIGNETTE_ID);
        content2 = this.createVignetteRegistrationResponseContent(VIGNETTE_ID_2);
        response.seteVignettes(Arrays.asList(content1, content2));

        return response;
    }

    public VignetteRegistrationResponseContent createVignetteRegistrationResponseContent(String vignetteId) throws ParseException {

        VignettePrice price = this.createVignettePrice();
        Vehicle vehicle = this.createVehicle();
        EVignetteProduct product = this.createEvignetteProduct();
        VignetteValidity validity = this.createVignetteValidity();
        VignettePurchase vignettePurchase = this.createVignettePurchase();

        VignetteRegistrationResponseContent respContent = new VignetteRegistrationResponseContent();
        respContent.setId(vignetteId);
        respContent.setPrice(price);
        respContent.setVehicle(vehicle);
        respContent.setProduct(product);
        respContent.setValidity(validity);
        respContent.setStatus(VIGNETTE_STATUS_ACTIVE);
        respContent.setPurchase(vignettePurchase);

        return respContent;
    }

    public VignetteRegistrationRequest createVignetteRegistrationRequest() throws ParseException {
        VignetteRegistrationRequest request = new VignetteRegistrationRequest();
        VignetteRegistrationRequest.VignetteRegistrationValidity validity =
                new VignetteRegistrationRequest.VignetteRegistrationValidity();
        validity.setRequestedValidityStartDate(modelFormatter.parse(ACTIVATION_DATE_STRING));
        Vehicle vehicle = this.createVehicle();

        request.setProductId(KAPSCH_PRODUCT_ID);
        request.setValidity(validity);
        request.setVehicle(vehicle);

        return request;
    }

    public VignetteRegistrationResponse createVignetteRegistrationResponse() throws ParseException {
        VignetteRegistrationResponse response = new VignetteRegistrationResponse();
        VignetteRegistrationResponseContent content = this.createVignetteRegistrationResponseContent(VIGNETTE_ID);

        response.seteVignette(content);

        return response;
    }

    public SaleRow createSaleRow() throws ParseException {

        SaleRowDTO saleRowDTO = this.createSaleRowDTO();
        SaleRow saleRow = new SaleRow();

        BasicUtils.copyPropsSkip(saleRowDTO, saleRow, Arrays.asList("vehicle", "productsResponse"));

        return saleRow;
    }

    public AggregatedResult createAggregatedResult() {
        AggregatedResult aggregatedResult = new AggregatedResult();
        aggregatedResult.setKapschProductId(KAPSCH_PRODUCT_ID);
        aggregatedResult.setPartnerId(PARTNER_ID);
        aggregatedResult.setPartnerName(PARTNER_NAME);
        aggregatedResult.setCount(AGGREGATED_COUNT);
        aggregatedResult.setTotalAmount(AMOUNT);
        aggregatedResult.setStatus(ACTIVE_STATUS);
        aggregatedResult.setPosId(POS_ID);
        aggregatedResult.setPosName(POS_NAME);
        aggregatedResult.setProductName(DESCRIPTION);
        aggregatedResult.setPurchaseDate(PURCHASE_DATE);
        aggregatedResult.setRegistrationDate(PURCHASE_DATE);
        return aggregatedResult;
    }

    public AggregatedResults createAggregatedResults() {
        List<AggregatedResult> aggregatedResultList = new ArrayList<>(1);
        aggregatedResultList.add(createAggregatedResult());

        Page<AggregatedResult> paginatedResult = findPaginated(PageRequest.of(0, 10), aggregatedResultList);
        AggregatedResults aggregatedResults = new AggregatedResults();
        List<SaleAggregationDTO> aggregationDTOList = paginatedResult.getContent()
                .stream().map(SaleAggregationDTO::new)
                .collect(Collectors.toList());
        aggregatedResults.setResults(aggregationDTOList);
        aggregatedResults.setTotalElements(paginatedResult.getTotalElements());
        aggregatedResults.setTotalPages(paginatedResult.getTotalPages());
        BigDecimal totalSum = new BigDecimal(0);
        int totalCount = 0;
        if (!aggregatedResultList.isEmpty()) {
            for (AggregatedResult row : aggregatedResultList) {
                if (row.getTotalAmount() != null) {
                    totalSum = totalSum.add(row.getTotalAmount());
                }

                if (row.getCount() != null) {
                    totalCount += row.getCount();
                }
            }
        }
        aggregatedResults.setTotalCount(totalCount);
        aggregatedResults.setTotalSum(totalSum);
        return aggregatedResults;
    }

    public Page<SaleRow> createSaleRowPage() throws ParseException {
        List<SaleRow> saleRows = new ArrayList<>();
        saleRows.add(createSaleRow());
        return findPaginated(PageRequest.of(0, 10), saleRows);
    }

    public AggregationRequest createAggregationRequest() throws ParseException {
        AggregationRequest aggregationRequest = new AggregationRequest();
        aggregationRequest.setGroupingFields(new String[]{
                "kapschProductId",
                "registrationDate",
                "purchaseDate",
                "partnerId",
                "posId",
                "status"
        });
        aggregationRequest.setFromActivationDate(modelFormatter.parse(VALIDITY_START_DATE));
        aggregationRequest.setToActivationDate(modelFormatter.parse(VALIDITY_END_DATE));


        return aggregationRequest;
    }

    private <T> Page<T> findPaginated(Pageable pageable, List<T> results) {
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<T> list;

        if (results.size() < startItem) {
            list = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, results.size());
            list = results.subList(startItem, toIndex);
        }

        return new PageImpl<>(list, PageRequest.of(currentPage, pageSize), results.size());
    }

    public Pos createPos() {
        Pos pos = new Pos();

        pos.setCode(CODE);
        pos.setKapschPosId(POS_ID);
        pos.setId(POS_ID);
        pos.setName(POS_NAME);
        pos.setPartnerId(PARTNER_ID);
        pos.setPosIdInPartnersDb(POS_ID);

        return pos;
    }

    public PosDTO createPosDTO() {
        PosDTO posDTO = new PosDTO();

        posDTO.setCode(CODE);
        posDTO.setKapschPosId(POS_ID);
        posDTO.setName(NAME);
        posDTO.setPartnerId(PARTNER_ID);
        posDTO.setPosIdInPartnersDb(POS_ID);

        return posDTO;
    }

    public CashTerminalPosDTO createCashterminalPosDTO(String key, String address) {
        CashTerminalPosDTO cashTerminalPosDTO = new CashTerminalPosDTO();
        CashTerminalPos cashTerminalPos = new CashTerminalPos();
        cashTerminalPos.setAddress(address);
        cashTerminalPos.setLat(POS_TERMINAL_LAT);
        cashTerminalPos.setLng(POS_TERMINAL_LNG);

        ArrayList<HashMap<String, CashTerminalPos>> terminals = new ArrayList<>();
        HashMap<String, CashTerminalPos> terminalPosMap = new HashMap<>();
        terminalPosMap.put(key, cashTerminalPos);
        terminals.add(terminalPosMap);
        cashTerminalPosDTO.setTerminals(terminals);

        return cashTerminalPosDTO;
    }

    public ProductsResponse createProductResponse() {
        ProductsResponse productsResponse = new ProductsResponse();
        VignettePrice vignettePrice = this.createVignettePrice();

        productsResponse.setCategoryDescriptionText(CATEGORY_DESCRIPTION);
        productsResponse.setEmissionClassText(EMISSION_CLASS_TEXT);
        productsResponse.setValidityTypeText(VIGNETTE_VALIDITY_TYPE_TEXT);
        productsResponse.setValidityType(VIGNETTE_VALIDITY_TYPE);
        productsResponse.setEmissionClass(EMISSION_CLASS);
        productsResponse.setId(PRODUCT_ID);
        productsResponse.setPrice(vignettePrice);

        return productsResponse;
    }

    public VignetteIdDTO createVignetteIdDto() {

        VignetteIdDTO vignetteIdDto = new VignetteIdDTO();
        vignetteIdDto.setPosId(ErpTestHelper.POS_ID);
        vignetteIdDto.setVignetteId(ErpTestHelper.VIGNETTE_ID);

        return vignetteIdDto;
    }

    public UserDetailsDTO createUserDetailsDTO() throws ParseException {
        List<Role> roles = new ArrayList<>(1);

        Role role = new Role();
        role.setName(NAME);
        role.setId(ErpTestHelper.ID);
        role.setCreatedAt(formatter.parse(ErpTestHelper.CREATED_AT));
        role.setCode(CODE);

        roles.add(role);

        List<String> posIds = new LinkedList<>();
        posIds.add(POS_ID);

        UserDetailsDTO userDetailsDTO = new UserDetailsDTO();
        userDetailsDTO.setActive(ACTIVE);
        userDetailsDTO.setCreatedAt(formatter.parse(ErpTestHelper.CREATED_AT));
        userDetailsDTO.setFirstName(FIRST_NAME);
        userDetailsDTO.setLastName(LAST_NAME);
        userDetailsDTO.setPartnerId(PARTNER_ID);
        userDetailsDTO.setId(USER_ID);
        userDetailsDTO.setPosIds(posIds);
        userDetailsDTO.setUsername(USERNAME);
        userDetailsDTO.setRoles(roles);

        return userDetailsDTO;
    }
}
