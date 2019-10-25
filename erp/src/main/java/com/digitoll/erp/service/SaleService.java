package com.digitoll.erp.service;

import com.digitoll.commons.dto.VignetteIdDTO;
import com.digitoll.commons.exception.*;
import com.digitoll.commons.kapsch.classes.EVignetteInventoryProduct;
import com.digitoll.commons.kapsch.request.BatchActivationRequest;
import com.digitoll.commons.kapsch.request.VignetteActivationRequest;
import com.digitoll.commons.kapsch.request.VignetteRegistrationRequest;
import com.digitoll.commons.kapsch.response.BatchActivationResponse;
import com.digitoll.commons.kapsch.response.VignetteRegistrationResponse;
import com.digitoll.commons.kapsch.response.VignetteRegistrationResponseContent;
import com.digitoll.commons.model.*;
import com.digitoll.commons.request.SaleRequest;
import com.digitoll.commons.response.SaleDTO;
import com.digitoll.commons.response.SaleRowDTO;
import com.digitoll.commons.util.BasicUtils;
import com.digitoll.erp.component.EmailComponent;
import com.digitoll.erp.component.PdfComponent;
import com.digitoll.commons.exception.ResourceNotFoundException;
import com.digitoll.erp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;

import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SaleService {

    private static final Logger logger = LoggerFactory.getLogger(SaleService.class);

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleRowRepository saleRowRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private PosRepository posRepository;

    @Autowired
    private EmailComponent emailComponent;

    @Autowired
    private KapschProductRepository kapschProductRepository;

    @Autowired
    private KapschService kapschService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SequenceGeneratorService sequenceGenerator;
    
    @Autowired
    private SaleReportService saleReportService;

    @Autowired
    private PdfComponent pdfComponent;

    @Autowired
    private RoleService roleService;

    @Value("${web.site.partner.id}")
    private String sitePartnerId;

    @Value("${web.site.pos.id}")
    private String sitePosId;

    @Value("${web.site.user.id}")
    private String siteUserId;

    @Value("${digitoll.erp.disableEmail:false}")
    private Boolean disableEmail;

    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> SAVE DATA <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    private SaleDTO activateSale(Sale sale, HttpSession session) throws ExpiredAuthTokenException, SaleRowIncompleteDataException, SaleIncompleteDataException, IOException {
        
        if (sale.getId() == null) {
            throw new SaleIncompleteDataException("Null element in sale:" + sale.toString());
        }
        sale.setActive(true);
        SaleDTO result = new SaleDTO(sale);
        List<SaleRow> saleRows = saleRowRepository.findBySaleId(sale.getId());
        File pdfAttachment = null;

        BatchActivationRequest batchAuthRequest = new BatchActivationRequest();
        for (SaleRow saleRow : saleRows) {
            batchAuthRequest.addId(saleRow.getVignetteId());
        }
        BatchActivationResponse batchAuthenticationResponse = null;
        try {
            batchAuthenticationResponse = kapschService.activateBatch(batchAuthRequest, session);
        } catch (ExpiredAuthTokenException e) {
            // handle failure
            throw e;
        }
        if (batchAuthenticationResponse == null) {
            logger.error("activateSale({}) NO response from Kapsch for ",sale);
            return null;
        }

        Map<String, VignetteRegistrationResponseContent> evignetteMap = new HashMap<>();

        for (VignetteRegistrationResponseContent vrr : batchAuthenticationResponse.geteVignettes()) {
            evignetteMap.put(vrr.getId(), vrr);
        }

        for (SaleRow saleRow : saleRows) {

            saleRow.setKapschProperties(evignetteMap.get(saleRow.getVignetteId()));
            saleRow.setActive(true);
            saleRow.updateDatesFromKapsch();
            saleRowRepository.save(saleRow);
            SaleRowDTO row = new SaleRowDTO(saleRow);
            if (row.getKapschProperties() != null) {
                row.setProductsResponse(saleReportService.getTranslatedProductDescription(row, null));
            }
            result.addRow(row);
        }
        
        SaleRowDTO[] eVignetteInvoicesBulk = result.getSaleRows().stream().toArray(SaleRowDTO[]::new);

        	pdfAttachment = pdfComponent.generatePdfForSaleRow(sale.getLanguage(), eVignetteInvoicesBulk);
        
        saleRepository.save(sale);
        try {
            emailComponent.sendEmail(result, pdfAttachment);
        } catch (MessagingException e) {
            logger.info("FAILED: sending email for " + result);
        }

        return result;
    }

    /**
     * Activate sale based on bank transaction id ( called from website )
     */
    public SaleDTO activateSaleByTransactionId(String transactionId, HttpSession session)
            throws ExpiredAuthTokenException, SaleIncompleteDataException, SaleRowIncompleteDataException, MessagingException, IOException {
        // TODO mongo transaction start
        
        Sale sale = Optional.ofNullable(saleRepository.findOneByBankTransactionId(transactionId))
                .orElseThrow(() -> new ResourceNotFoundException("Sale for transactionId not found: " + transactionId));
        
        return activateSale(sale, session);
    }

    /**
     * vignetteIdDTO contains PosId from partners database.
     *
     * @param vignetteIdDTO
     * @param session
     * @return
     * @throws ExpiredAuthTokenException
     */
    public SaleRowDTO activateSaleByVignetteId(@Valid VignetteIdDTO vignetteIdDTO, HttpSession session)
            throws HttpStatusCodeException, ExpiredAuthTokenException, SaleRowNotFoundException, SaleRowIncompleteDataException, MessagingException, IOException {

        logger.info("activateSaleByVignetteId({})",vignetteIdDTO);

        SaleRow saleRow = saleRowRepository.findOneByVignetteId(vignetteIdDTO.getVignetteId());

        if (saleRow == null) {
            throw new SaleRowNotFoundException("SaleRow containing vignetteId " + vignetteIdDTO.getVignetteId() + " not found");
        }

        Sale sale = saleRepository.findOneById(saleRow.getSaleId());
        Pos digitollPos = null;

        if(vignetteIdDTO.getPosId() != null) {
            digitollPos = posRepository.findOneByPosIdInPartnersDb(vignetteIdDTO.getPosId());
        }

        String digitollPosId;
        String digitollPosName = "";

        if (digitollPos == null) {
            digitollPosId = vignetteIdDTO.getPosId();
        } else {
            digitollPosId = digitollPos.getId();
            digitollPosName = digitollPos.getName();
        }

        sale.setPosId(digitollPosId);
        sale.setPosName(digitollPosName);
        saleRow.setPosId(digitollPosId);
        saleRow.setPosName(digitollPosName);
        saleRow.setPos(digitollPos);
        User user = userRepository.findOneById(siteUserId);

        saleRow.setActive(true);

        VignetteActivationRequest vignetteActivationRequest = new VignetteActivationRequest();
        vignetteActivationRequest.setId(Long.valueOf(saleRow.getVignetteId()));

        try {
            VignetteRegistrationResponse vignetteRegistrationResponse = kapschService
                    .activateVignette(vignetteActivationRequest, sale.getPosId(), session);
            saleRow.setKapschProperties(vignetteRegistrationResponse.geteVignette());
            saleRow.updateDatesFromKapsch();
        } catch (ExpiredAuthTokenException | HttpStatusCodeException e) {

            logger.info("activateSaleByVignetteId({}) failed",vignetteIdDTO);

            if (!saleRow.isActive()) {
                saleRow.setFailedKapschTrans(true);
            }
            if (e instanceof HttpStatusCodeException) {
                saleRow.setFailureMessage(((HttpStatusCodeException) e).getResponseBodyAsString());
            }
            throw e;
        }

        saleRepository.save(sale);
        saleRowRepository.save(saleRow);
        SaleRowDTO result = new SaleRowDTO(saleRow);

        if (!disableEmail && saleRow.isFailedKapschTrans() == false && saleRow.getEmail() != null) {
            emailComponent.sendEmail(result, pdfComponent.generatePdfForSaleRow(null, result));
        }

        logger.info("activateSaleByVignetteId({}) complete",vignetteIdDTO);

        return result;
    }


    /**
     * Activate sale based on sale id ( called from erp )
     */
    public SaleDTO activateSaleBySaleId(String saleId, HttpSession session) throws ExpiredAuthTokenException, SaleIncompleteDataException, SaleRowIncompleteDataException, MessagingException, IOException {
        // TODO mongo transaction start
        
        Sale sale = Optional.ofNullable(saleRepository.findOneById(saleId))
                .orElseThrow(() -> new ResourceNotFoundException("SaleId not found: " + saleId));        
        
        return activateSale(sale, session);
    }

    // TODO Refactor this once we fix the DB refs ( Pepsa 25.07.2019 )
    private void denormalizeSaleRows(Sale sale, List<SaleRowDTO> saleRows,
            User user) {
        Pos pos = posRepository.findOneById(sale.getPosId());

        // TODO temporary fix to have default posId if not found in our database
        Partner partner = null;
        if (user != null) {
            partner = partnerRepository.findOneById(user.getPartnerId());
        }

        // Do we need to throw error if there is no partner, related to pos?
        //in case of some Partners they send us pos and partner information on activation
        if (user != null) {
            sale.setUserName(user.getUsername());
        }
        if (partner != null) {
            sale.setPartnerName(partner.getName());
            sale.setPartnerId(partner.getId());
        }
        if (pos != null) {
            sale.setPosName(pos.getName());
        }
        for(SaleRow saleRow : saleRows){
            saleRow.setUser(new UserCache(user));
            saleRow.setPartner(partner);
            saleRow.setPos(pos);
            saleRow.setSale(sale);
        }

    }


    /**
     * Save base sale ( a collection of vignette sales, batched in 1 transaction )
     */
    private Sale saveBaseSale(SaleDTO saleDTO, User user) {

        Sale sale = new Sale(saleDTO);
        sale.setSaleSeq(sequenceGenerator.generateSequence(Sale.SEQUENCE_NAME));
        sale.setTotal(new BigDecimal("0.00"));

        denormalizeSaleRows(sale, saleDTO.getSaleRows(), user);

        // the sale should always be new even if an id is sent
        saleDTO.setId(null);
        
        //Calc total and save main sale
        for (SaleRowDTO saleRowDTO : saleDTO.getSaleRows()) {

            EVignetteInventoryProduct product = kapschProductRepository.findOneById(saleRowDTO.getKapschProductId());
            saleRowDTO.addRowPropertiesFromKapschProduct(product);
            saleRowDTO.calculateValidityDates();
            saleRowDTO.addReportPropertiesFromSale(sale,user);
            if (saleRowDTO.getPrice() == null) {
//             handle price differences
//             continue;
            }
            sale.setTotal(sale.getTotal().add(saleRowDTO.getPrice().getAmount()));
            saleRowDTO.setSaleSequence(sale.getSaleSeq());
        }
        // Get proper mongo id for sure
        return saleRepository.insert(sale);
    }

    public SaleDTO populateSiteUserAndCreateSale(SaleDTO saleDTO, HttpSession session) throws Exception {

        saleDTO.setPosId(sitePosId);
        saleDTO.setPartnerId(sitePartnerId);
        saleDTO.setUserId(siteUserId);
        User user = userRepository.findOneById(siteUserId);
        //za vseki slu4ai
        saleDTO.getSaleRows().forEach(sr -> {
            sr.setPartnerId(sitePartnerId);
            sr.setPosId(sitePosId);
            sr.setUserId(siteUserId);
        });
        return createSale(saleDTO, user, session);

    }

    public SaleDTO createSaleWithPartnersPos(SaleRequest saleRequest, User user,
                                             HttpSession session)
            throws Exception {

        Pos digitollPos = posRepository.findOneByPosIdInPartnersDb(saleRequest.getPosId());

        // TODO temporary fix to have default posId if not found in our database
        String digitollPosId = digitollPos == null ? ("not-found-" + saleRequest.getPosId()) : digitollPos.getId();

        saleRequest.setPosId(digitollPosId);

        SaleDTO saleDTO = new SaleDTO(saleRequest);
        return createSale(saleDTO, user, session);
    }

    @Transactional
    public SaleDTO createSale(SaleDTO saleDTO, User user, HttpSession session) throws Exception {

        logger.info("createSale({})", saleDTO);
        
        // TODO - mongo transaction
        // Get proper mongo id for sure
        // We need to calculate total and save sale, so we can set sale id to rows

        if (!roleService.isUserAdmin(user) && !roleService.isUserPartnerAdmin(user) && !roleService.isNoPosUser(user)
                && user.getPosIds() != null && user.getPosIds().size() == 1 && user.getPosIds().get(0) != null) {
            saleDTO.setPosId(user.getPosIds().get(0));
        }

        Sale sale = saveBaseSale(saleDTO, user);

        try {

            for (SaleRowDTO saleRowDTO : saleDTO.getSaleRows()) {

                registerVignette(sale, saleRowDTO, session);
            }
        } catch (Exception ex) {

            logger.info("createSale({}) failed", saleDTO);
            throw ex;
        }

        logger.info("createSale({}) complete",saleDTO);

        //make sure we return the exact same data, we recorded in the db
        BasicUtils.copyNonNullProps(sale, saleDTO);

        return saleDTO;
    }

    private void registerVignette(Sale sale, SaleRowDTO saleRowDTO, HttpSession session)
            throws ExpiredAuthTokenException, MessagingException {

        // TODO get Kapsch ID and save vehicle and data in Kapsch
        VignetteRegistrationResponse kapschResponse = null;

        kapschResponse = registerVignetteToKapsch(saleRowDTO, session);

        if (kapschResponse.geteVignette() == null) {
            throw new MessagingException("No kapsch response");
        }

        saleRowDTO.setVignetteId(kapschResponse.geteVignette().getId());
        saleRowDTO.setKapschProperties(kapschResponse.geteVignette());
        if (saleRowDTO.getKapschProperties() != null) {
            //saleRowDTO = this.addTranlsatedDescription(saleRowDTO);
            saleRowDTO.setProductsResponse(saleReportService.getTranslatedProductDescription(saleRowDTO, null));
        }

        saleRowDTO.setLpn(kapschResponse.geteVignette().getVehicle().getLpn());
        saleRowDTO.setSaleId(sale.getId());
        saleRowDTO.setSaleSequence(sale.getSaleSeq());
        saveVehicle(saleRowDTO);

        SaleRow saleRow = new SaleRow(saleRowDTO);
        saleRowRepository.insert(saleReportService.getSaleRowWithDescription(saleRow));
    }

    public long fillMissedSaleSequences() {
        long counter = 0;
        for (Sale sale : saleRepository.findAll()) {
            if (sale.getSaleSeq() == 0) {
                sale.setSaleSeq(sequenceGenerator.generateSequence(Sale.SEQUENCE_NAME));
                saleRepository.save(sale);
                for (SaleRow salerow : saleRowRepository.findBySaleId(sale.getId())) {
                    salerow.setSaleSequence(sale.getSaleSeq());
                    saleRowRepository.save(salerow);
                }
                counter++;
            }
        }

        return counter;
    }

    @Autowired
    private MongoTemplate mongoTemplate;
    /**
     * Simple, but volatile
     * make a backup before using
     */
    public void fillDenormalizedData() throws InterruptedException {
        List<Partner> partners = partnerRepository.findAll();
        HashMap<String,Partner> partnerMap = new HashMap<>();
        for(Partner p : partners){
            partnerMap.put(p.getId(),p);
        }

        List<Pos> posI = posRepository.findAll();
        HashMap<String,Pos> posMap = new HashMap<>();
        for(Pos p : posI){
            posMap.put(p.getId(),p);
        }

        List<User> users = userRepository.findAll();
        HashMap<String,User> userMap = new HashMap<>();
        for(User p : users){
            userMap.put(p.getId(),p);
        }

        List<Sale> sales = saleRepository.findAll();
        HashMap<String,Sale> salesMap = new HashMap<>();
        for(Sale s : sales){
            salesMap.put(s.getId(),s);
        }
        long start = System.currentTimeMillis();
        Query query = new Query();
        query.addCriteria(Criteria.where("kapschProperties.purchase.purchaseDateTimeUTC").exists(true));
        List<SaleRow> rows = mongoTemplate.find(query, SaleRow.class);
        long end1 = System.currentTimeMillis();
        logger.info("All metadata db trans: "+(end1 - start)/1000F/60F + " minutes");
        int index = 0;
        int cnt = rows.size();
        for (SaleRow salerow : rows) {
            if(salerow.getPosId() != null){
                salerow.setPos(posMap.get(salerow.getPosId()));
            }
            if(salerow.getSaleId() != null) {
                salerow.setSale(salesMap.get(salerow.getSaleId()));
            }
            if(salerow.getPartnerId() != null) {
                salerow.setPartner(partnerMap.get(salerow.getPartnerId()));
            }
            if(salerow.getUserId() != null && userMap.get(salerow.getUserId()) != null) {
                salerow.setUser(new UserCache(userMap.get(salerow.getUserId())));
            }

            salerow.cachePurchaseDates();
            saleRowRepository.save(salerow);
            end1 = System.currentTimeMillis();
            if(index % 100 == 0){
                logger.info("Processed: "+index +" / "+cnt+" || current time:"+  (end1 - start)/1000F/60F);
            }
            Thread.sleep(100);
            index++;
        }
        end1 = System.currentTimeMillis();
        System.out.println("All rows processed: " + (end1 - start)/1000F/60F + " minutes");
    }

    private VignetteRegistrationResponse registerVignetteToKapsch(SaleRowDTO saleRowDTO, HttpSession session) throws ExpiredAuthTokenException {

        VignetteRegistrationRequest request = new VignetteRegistrationRequest()
                .withProductId(saleRowDTO.getKapschProductId())
                .withDate(saleRowDTO.getValidityStartDate())
                .withVehicle(saleRowDTO.getVehicle());
        return kapschService.registerVignette(request, session);
    }

    //Todo : Create separate vehicle object in Sale ROw
    private void saveVehicle(SaleRowDTO saleRowDTO) {
        Vehicle vehicle = saleRowDTO.getVehicle();

        if (vehicle != null) {
            vehicle = vehicleRepository.save(vehicle);
        }
        if (vehicle.getId() == null) {
            return;
        }
        saleRowDTO.setVehicleId(vehicle.getId());
    }

    public Sale updateSaleTransactionId(String saleId, String transactionId) {
        Optional<Sale> sale = saleRepository.findById(saleId);

        sale.orElseThrow(NoSuchElementException::new);

        Sale aSale = sale.get();

        aSale.setBankTransactionId(transactionId);

        List<SaleRow> saleRows = saleRowRepository.findBySaleId(aSale.getId());

        for(SaleRow saleRow : saleRows){
            saleRow.setSale(aSale);
            saleRowRepository.save(saleRow);
        }

        return saleRepository.save(aSale);
    }
 
//    public List<SaleDTO> getSales(String username, PageRequest page) {
//
//        ArrayList<SaleDTO> result = new ArrayList<>();
//
//        Page<Sale> salePage = saleRepository.findByUserName(username, page);
//        List<Sale> sales = salePage.getContent();
//        for (Sale sale : sales) {
//            SaleDTO saleDto = new SaleDTO();
//            List<SaleRow> saleRows = saleRowRepository.findBySaleId(sale.getId());
//
//            for (SaleRow saleRow : saleRows) {
//                SaleRowDTO row = new SaleRowDTO(saleRow);
//                EVignetteInventoryProduct product = kapschProductRepository.findOneById(row.getKapschProductId());
//                row.setProductsResponse(translationComponent.translateProduct(product, null));
//
//                saleDto.addRow(row);
//            }
//            result.add(saleDto);
//        }
//
//
//        return result;
//    }
}

