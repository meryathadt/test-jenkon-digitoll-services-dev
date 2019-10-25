package com.digitoll.erp.service;

import com.digitoll.commons.aggregation.AggregatedResults;
import com.digitoll.commons.enumeration.DateGroupingBases;
import com.digitoll.commons.enumeration.VehicleType;
import com.digitoll.commons.enumeration.VignetteValidityType;
import com.digitoll.commons.exception.ExpiredAuthTokenException;
import com.digitoll.commons.exception.NoPosIdAssignedToUserException;
import com.digitoll.commons.exception.ResourceNotFoundException;
import com.digitoll.commons.exception.SaleRowIncompleteDataException;
import com.digitoll.commons.kapsch.classes.EVignetteProduct;
import com.digitoll.commons.kapsch.response.VignetteRegistrationResponse;
import com.digitoll.commons.kapsch.response.VignetteRegistrationResponseContent;
import com.digitoll.commons.kapsch.response.c9.SearchResponse;
import com.digitoll.commons.kapsch.response.c9.SearchVignette;
import com.digitoll.commons.model.*;
import com.digitoll.commons.request.AggregationRequest;
import com.digitoll.commons.response.PaginatedRowsResponse;
import com.digitoll.commons.response.ProductsResponse;
import com.digitoll.commons.response.SaleDTO;
import com.digitoll.commons.response.SaleRowDTO;
import com.digitoll.commons.util.DateTimeUtil;
import com.digitoll.erp.component.PdfComponent;
import com.digitoll.erp.component.TranslationComponent;
import com.digitoll.erp.repository.*;
import com.mongodb.lang.NonNull;
import com.mongodb.lang.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;


@Service
public class SaleReportService {

    private static final Logger logger = LoggerFactory.getLogger(SaleReportService.class);

    private static final int CATEGORY_3 = 3;
    private static final int CATEGORY_2 = 2;
    private static final int CATEGORY_1 = 1;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    AggregationService aggregationService;

    @Autowired
    private SaleRowRepository saleRowRepository;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private TranslationComponent translationComponent;

    @Autowired
    private PdfComponent pdfComponent;

    @Autowired
    private KapschService kapschService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    private MongoTemplate mongoTemplate;

    private HashMap<String, Partner> _mPartners = new HashMap<>();

    @Autowired
    public SaleReportService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public PaginatedRowsResponse getSalesByCriteria(@Nullable Date validityStartDate,
                                                    @Nullable Date validityEndDate,
                                                    @Nullable String lpn,
                                                    @Nullable String partnerId,
                                                    @Nullable String posId,
                                                    @Nullable String vignetteId,
                                                    @Nullable String saleId,
                                                    @Nullable String vehicleId,
                                                    @Nullable String userId,
                                                    @Nullable String partnerName,
                                                    @Nullable String posName,
                                                    @Nullable String userName,
                                                    @Nullable VignetteValidityType validityType,
                                                    @Nullable String email,
                                                    @Nullable Boolean active,
                                                    @Nullable Date createdOn,
                                                    @Nullable Date fromRegistrationDate,
                                                    @Nullable Date toRegistrationDate,
                                                    @Nullable Date fromActivationDate,
                                                    @Nullable Date toActivationDate,
                                                    @Nullable String remoteClientId,
                                                    @NonNull PageRequest page,
                                                    @NonNull Boolean showTotalSum,
                                                    @Nullable Integer category,
                                                    @NonNull String currentUsername) throws NoPosIdAssignedToUserException {
        Query query = new Query();
        BigDecimal totalSum = null;

        User currentUser = Optional.ofNullable(userRepository.findByUsername(currentUsername))
                .orElseThrow(() -> new UsernameNotFoundException("Username not found: " + currentUsername));

        List<Criteria> criteriaList = addCriteria(validityStartDate, validityEndDate, lpn, partnerId, posId, vignetteId, saleId, vehicleId,
                userId, partnerName, posName, userName, validityType, email, active, createdOn, fromRegistrationDate,
                toRegistrationDate, fromActivationDate, toActivationDate, remoteClientId, category, currentUser);

        if (showTotalSum) {
            totalSum = aggregationService.getAggregationForAmount(new String[]{}, criteriaList, SaleRow.class, DateGroupingBases.MONTHLY).getTotalSum();
        }

        for (Criteria criteria : criteriaList) {
            query.addCriteria(criteria);
        }

        List<SaleRow> list = mongoTemplate.find(query.with(page), SaleRow.class);
        PaginatedRowsResponse response = getPaginationResponse(new PageImpl<>(list, page, mongoTemplate.count(query, SaleRow.class)));
        response.setTotalSum(totalSum);
        return response;
    }

    private List<Criteria> addCriteria(@Nullable Date validityStartDate, @Nullable Date validityEndDate, @Nullable String lpn,
                                       @Nullable String partnerId, @Nullable String posId, @Nullable String vignetteId,
                                       @Nullable String saleId, @Nullable String vehicleId, @Nullable String userId,
                                       @Nullable String partnerName, @Nullable String posName, @Nullable String userName,
                                       @Nullable VignetteValidityType validityType, @Nullable String email, @Nullable Boolean active,
                                       @Nullable Date createdOn, @Nullable Date fromRegistrationDate, @Nullable Date toRegistrationDate,
                                       @Nullable Date fromActivationDate, @Nullable Date toActivationDate, @Nullable String remoteClientId,
                                       @Nullable Integer category, User currentUser) throws NoPosIdAssignedToUserException {
        List<Criteria> criteriaList = new ArrayList<>();
        if (validityStartDate != null && validityEndDate != null) {
//            //Search range in all validity Ranges
//            criteriaList.add(Criteria.where("validityStartDate").lte(validityEndDate)
//                    .and("validityEndDate").gte(validityStartDate));

            criteriaList.add(Criteria.where("kapschProperties.validity.validityStartDateTimeUTC")
                    .gte(DateTimeUtil.getStartOfDay(validityStartDate)).lt(DateTimeUtil.getEndOfDay(validityEndDate)));
        }

        if (fromRegistrationDate != null && toRegistrationDate != null) {
            criteriaList.add(Criteria.where("createdOn").gte(fromRegistrationDate).lte(toRegistrationDate));
        }


        if (fromActivationDate != null && toActivationDate != null) {
            criteriaList.add(Criteria.where("kapschProperties.purchase.purchaseDateTimeUTC").exists(true).gte(fromActivationDate).lte(toActivationDate));
        }

        if (createdOn != null && fromActivationDate == null) {
            Calendar c = Calendar.getInstance();
            c.setTime(createdOn);
            c.add(Calendar.DATE, 1);
            criteriaList.add(Criteria.where("kapschProperties.purchase.purchaseDateTimeUTC").exists(true).gte(createdOn).lt(c.getTime()));
        }

        if (roleService.isUserAdmin(currentUser) && partnerId != null) {
            criteriaList.add(Criteria.where("partnerId").is(partnerId));
        } else if (roleService.isUserPartnerAdmin(currentUser)) {
            criteriaList.add(Criteria.where("partnerId").is(currentUser.getPartnerId()));
        }

        if (roleService.isUserPartnerAdmin(currentUser) || roleService.isUserAdmin(currentUser)) {
            if (posId != null) {
                criteriaList.add(Criteria.where("posId").is(posId));
            }
        } else {
            if (currentUser.getPosIds() != null && !currentUser.getPosIds().isEmpty()) {
                criteriaList.add(Criteria.where("posId").in(currentUser.getPosIds()));
            } else {
                throw new NoPosIdAssignedToUserException("No POS id assigned to user: " + currentUser.toString());
            }
        }

        if (vignetteId != null) {
            criteriaList.add(Criteria.where("vignetteId").is(vignetteId));
        }

        if (saleId != null) {
            criteriaList.add(Criteria.where("saleId").is(saleId));
        }
        if (vehicleId != null) {
            criteriaList.add(Criteria.where("vehicleId").is(vehicleId));
        }

        if (userId != null) {
            criteriaList.add(Criteria.where("userId").is(userId));
        }

        if (partnerName != null) {
            criteriaList.add(Criteria.where("partnerName").regex(partnerName, "i"));
        }
        if (posName != null) {
            criteriaList.add(Criteria.where("posName").regex(posName, "i"));
        }
        if (userName != null) {
            criteriaList.add(Criteria.where("userName").regex(userName, "i"));
        }
        if (lpn != null) {
            criteriaList.add(Criteria.where("lpn").regex(lpn, "i"));
        }
        if (email != null) {
            criteriaList.add(Criteria.where("email").regex(email, "i"));
        }
        if (validityType != null) {
            criteriaList.add(Criteria.where("validityType").is(validityType.name()));
        }
        if (active != null) {
            criteriaList.add(Criteria.where("active").is(active));
        }
        if (remoteClientId != null) {
            criteriaList.add(Criteria.where("remoteClientId").is(remoteClientId));
        }

        if (category != null) {
            switch (category) {
                case CATEGORY_3:
                    criteriaList.add(new Criteria().orOperator(Criteria.where("kapschProperties.product.vehicleType").is(VehicleType.car),
                            Criteria.where("kapschProperties.product.vehicleType").is(VehicleType.trailer)));
                    break;
                case CATEGORY_2:
                    criteriaList.add(Criteria.where("kapschProperties.product.vehicleType").is(VehicleType.hgvn2));
                    break;
                case CATEGORY_1:
                    criteriaList.add(Criteria.where("kapschProperties.product.vehicleType").is(VehicleType.hgvn3));
                    break;
            }
        }
        return criteriaList;
    }

    public AggregatedResults aggregateReport(AggregationRequest aggregationRequest,
                                            PageRequest page,
                                             String currentUsername,
                                             String[] sortingParameters,
                                             Sort.Direction sortingDirection,
                                             DateGroupingBases dateGroupingBases) throws NoPosIdAssignedToUserException {

        User currentUser = Optional.ofNullable(userRepository.findByUsername(currentUsername))
                .orElseThrow(() -> new UsernameNotFoundException("Username not found: " + currentUsername));

        List<Criteria> criteriaList =
                addCriteria(aggregationRequest.getValidityStartDate(), aggregationRequest.getValidityEndDate(), aggregationRequest.getLpn(),
                        aggregationRequest.getPartnerId(), aggregationRequest.getPosId(), aggregationRequest.getVignetteId(), aggregationRequest.getSaleId(),
                        aggregationRequest.getVehicleId(), aggregationRequest.getUserId(), aggregationRequest.getPartnerName(), aggregationRequest.getPosName(),
                        aggregationRequest.getUserName(), aggregationRequest.getValidityType(), aggregationRequest.getEmail(), aggregationRequest.getActive(),
                        aggregationRequest.getCreatedOn(), aggregationRequest.getFromRegistrationDate(), aggregationRequest.getToRegistrationDate(),
                        aggregationRequest.getFromActivationDate(), aggregationRequest.getToActivationDate(), aggregationRequest.getRemoteClientId(),
                        aggregationRequest.getCategory(), currentUser);

        return aggregationService.getPagedAggregationForAmount(aggregationRequest.getGroupingFields(), criteriaList, page,
                sortingDirection, sortingParameters, SaleRow.class, dateGroupingBases);
    }

    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> RETURN DATA <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    public SaleDTO getSaleByTransactionId(String transactionId, HttpSession session) throws ExpiredAuthTokenException {
        Sale sale = saleRepository.findOneByBankTransactionId(transactionId);
        return getSaleRows(sale, session);
    }

    public SaleDTO getSaleById(String saleId, HttpSession session) throws ExpiredAuthTokenException {
        Sale sale = saleRepository.findOneById(saleId);

        if (sale == null) {
            throw new ResourceNotFoundException(String.format("Sale with ID %s not found", saleId));
        }

        return getSaleRows(sale, session);
    }

    public SaleRow getSaleRowWithDescription(SaleRow saleRow) {
        ProductsResponse translatedProduct = getTranslatedProductDescription(saleRow, "bg");
        if (translatedProduct != null)
            saleRow.getKapschProperties().getProduct().setDescription(translatedProduct.getValidityTypeText() + ", "
                    + translatedProduct.getCategoryDescriptionText() + ", "
                    + translatedProduct.getVehicleTypeText() +
                    (StringUtils.isEmpty(translatedProduct.getEmissionClassText()) ? "" : ", " + translatedProduct.getEmissionClassText())
            );
        return saleRow;
    }

    public SaleRowDTO getSaleRowByVignetteId(String vignetteId, HttpSession session) throws ExpiredAuthTokenException {
        SaleRow row = saleRowRepository.findOneByVignetteId(vignetteId);

        if (row == null) {
            throw new ResourceNotFoundException(String.format("Vignette with ID %s not found", vignetteId));
        }

        SaleRowDTO result = new SaleRowDTO(row);
        VignetteRegistrationResponse searchResponse = kapschService.vignetteInfo(row.getVignetteId(), session);
        VignetteRegistrationResponseContent searchVignette = searchResponse.geteVignette();
        populatePartnerProps(result);
        if (searchVignette.getStatus() == 2) {
            row.setActive(true);
        } else {
            row.setActive(false);
        }

        if (result.getKapschProperties() != null) {
            result.setProductsResponse(getTranslatedProductDescription(result, null));
        }

        return result;
    }

    private SaleDTO getSaleRows(Sale sale, HttpSession session) throws ExpiredAuthTokenException {
        List<SaleRow> saleRows = saleRowRepository.findBySaleId(sale.getId());

        SaleDTO result = new SaleDTO(sale);
        for (SaleRow saleRow : saleRows) {
            SaleRowDTO row = new SaleRowDTO(saleRow);
            SearchResponse searchResponse = kapschService
                    .vignetteSearch(row.getVignetteId(), session);
            SearchVignette searchVignette = searchResponse.geteVignetteList().get(0);

            if (row.getKapschProperties() == null) {
                continue;
            }
            row.setProductsResponse(getTranslatedProductDescription(row, null));

            if (searchVignette.getStatus() == 2) {
                row.setActive(true);
            } else {
                row.setActive(false);
            }

            result.addRow(row);
        }

        return result;
    }


    private ArrayList<SaleRowDTO> getSaleRowsResponse(List<SaleRow> saleRows) {
        ArrayList<SaleRowDTO> result = new ArrayList<>();
        for (SaleRow saleRow : saleRows) {
            SaleRowDTO row = new SaleRowDTO(saleRow);
            //This is needed only for the dev mongoDb since there we may have 
            //missing partnerIds
            //populatePartnerProps(row);
            if (row.getKapschProperties() != null) {
                row.setProductsResponse(getTranslatedProductDescription(row, null));
            }
            result.add(row);
        }
        return result;
    }

    private PaginatedRowsResponse getPaginationResponse(Page<SaleRow> saleRows) {
        PaginatedRowsResponse response = new PaginatedRowsResponse();
        response.setTotalElements(saleRows.getTotalElements());
        response.setTotalPages(saleRows.getTotalPages());
        response.setSaleRows(getSaleRowsResponse(saleRows.getContent()));
        return response;
    }

    // list of vignetes
    public PaginatedRowsResponse getSaleRowsForUser(String username, PageRequest page) {
        Page<SaleRow> saleRows = saleRowRepository.findByUserNameAndActive(username, true, page);
        return getPaginationResponse(saleRows);
    }

    public PaginatedRowsResponse getSaleByEmail(String email, PageRequest page) {
        Page<SaleRow> saleRows = saleRowRepository.findByEmailAndActive(email, true, page);
        return getPaginationResponse(saleRows);
    }

    public PaginatedRowsResponse getSalePages(PageRequest page) {
        Page<SaleRow> saleRows = saleRowRepository.findAllByActive(true, page);
        return getPaginationResponse(saleRows);
    }

    public ProductsResponse getTranslatedProductDescription(SaleRow saleRow, String language) {
        EVignetteProduct product = saleRow.getKapschProperties().getProduct();
        return product != null ? translationComponent.translateProduct(product, language) : null;
    }


    public File generatePdf(String vignetteId, HttpSession session) throws ExpiredAuthTokenException, SaleRowIncompleteDataException, IOException {
        return pdfComponent.generatePdfForSaleRow(null, getSaleRowByVignetteId(vignetteId, session));
    }


    /**
     * Only used when there is no data for pos/partner stored for sale
     * #paterica
     */
    private void populatePartnerProps(SaleRowDTO saleRowDTO) {
        Partner partner = null;
        if (!StringUtils.isEmpty(saleRowDTO.getPartnerName())) {
            return;
        }
        if (saleRowDTO.getPartnerId() == null) {
            partner = getMissingPartnerByUser(saleRowDTO.getUserId());
        } else {
            partner = getMissingPartnerByUser(saleRowDTO.getPartnerId());
        }

        if (partner == null) {
            return;
        }
        //saleRowDTO.setPartnerId(partner.getPartnerId());
        saleRowDTO.setPartnerId(partner.getId());
        saleRowDTO.setPartnerName(partner.getName());
    }

    private Partner getMissingPartnerByUser(String userId) {
        User user = userRepository.findOneById(userId);
        if (user == null) {
            return null;
        }
        return getCachedPartners(user.getPartnerId());
    }


    /**
     * Only used when there is no data for partner stored for sale
     */
    private Partner getCachedPartners(String partnerId) {
        if (partnerId == null) {
            return null;
        }
        if (_mPartners.get(partnerId) == null) {
            cachePartners();
        }
        return _mPartners.get(partnerId);
    }


    /**
     * Only used when there is no data for partner stored for sale
     */
    private void cachePartners() {
        List<Partner> partners = partnerRepository.findAll();
        _mPartners = new HashMap<>();
        for (Partner p : partners) {
            _mPartners.put(p.getId(), p);
        }
    }
}
