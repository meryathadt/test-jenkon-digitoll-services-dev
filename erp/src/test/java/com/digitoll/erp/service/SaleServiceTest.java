package com.digitoll.erp.service;

import com.digitoll.commons.exception.ResourceNotFoundException;
import com.digitoll.commons.dto.VignetteIdDTO;
import com.digitoll.commons.exception.ExpiredAuthTokenException;
import com.digitoll.commons.exception.SaleIncompleteDataException;
import com.digitoll.commons.exception.SaleRowIncompleteDataException;
import com.digitoll.commons.exception.SaleRowNotFoundException;
import com.digitoll.commons.kapsch.classes.EVignetteInventoryProduct;
import com.digitoll.commons.kapsch.classes.EVignetteProduct;
import com.digitoll.commons.kapsch.request.BatchActivationRequest;
import com.digitoll.commons.kapsch.request.VignetteActivationRequest;
import com.digitoll.commons.kapsch.request.VignetteRegistrationRequest;
import com.digitoll.commons.kapsch.response.BatchActivationResponse;
import com.digitoll.commons.kapsch.response.VignetteRegistrationResponse;
import com.digitoll.commons.model.*;
import com.digitoll.commons.request.SaleRequest;
import com.digitoll.commons.response.ProductsResponse;
import com.digitoll.commons.response.SaleDTO;
import com.digitoll.commons.response.SaleRowDTO;
import com.digitoll.erp.component.EmailComponent;
import com.digitoll.erp.component.PdfComponent;
import com.digitoll.erp.component.TranslationComponent;
import com.digitoll.erp.repository.*;
import com.digitoll.erp.utils.ErpTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpStatusCodeException;

import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.Assert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@ContextConfiguration(classes = SaleService.class)
@RunWith(SpringRunner.class)
public class SaleServiceTest {

    @Value("${web.site.partner.id}")
    private String sitePartnerId;

    @Value("${web.site.pos.id}")
    private String sitePosId;

    @Value("${web.site.user.id}")
    private String siteUserId;

    @MockBean
    private SaleRepository saleRepository;

    @MockBean
    private SaleReportService saleReportService;

    @MockBean
    private SaleRowRepository saleRowRepository;

    @MockBean
    private VehicleRepository vehicleRepository;

    @MockBean
    private PartnerRepository partnerRepository;

    @MockBean
    private PosRepository posRepository;

    @MockBean
    private EmailComponent emailComponent;

    @MockBean
    private TranslationComponent translationComponent;

    @MockBean
    private KapschProductRepository kapschProductRepository;

    @MockBean
    private KapschService kapschService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private SequenceGeneratorService sequenceGenerator;

    @MockBean
    private MongoTemplate mongoTemplate;

    @MockBean
    private PdfComponent pdfComponent;

    @MockBean
    private RoleService roleService;

    @Autowired
    private SaleService saleService;
    
    private ErpTestHelper testHelper;

    @Before
    public void init() {
        testHelper = new ErpTestHelper();
    }    

    @Test
    public void activateSaleBySaleIdTest() throws ExpiredAuthTokenException, SaleIncompleteDataException, SaleRowIncompleteDataException, MessagingException, ParseException, IOException {
        System.out.println("activateSaleBySaleId");
        Sale saleMockedResult = testHelper.createSale();
        SaleRow saleRowMockedResult = testHelper.createSaleRow();
        BatchActivationResponse batchActivationResponseMockedResult = testHelper.createBatchActivationResponse();

        when(saleRepository.findOneById(ErpTestHelper.SALE_ID)).thenReturn(saleMockedResult);
        when(saleRowRepository.findBySaleId(ErpTestHelper.SALE_ID)).thenReturn(Arrays.asList(saleRowMockedResult));
        when(kapschService.activateBatch(Mockito.any(BatchActivationRequest.class), Mockito.any(HttpSession.class))).
                thenReturn(batchActivationResponseMockedResult);

        SaleDTO result = saleService.activateSaleBySaleId(ErpTestHelper.SALE_ID, new MockHttpSession());

        SaleRowDTO saleRowDto = result.getSaleRows().get(0);

        assertEquals(saleRowDto.getVignetteId(), ErpTestHelper.VIGNETTE_ID);
        assertEquals(saleRowDto.getKapschProperties().getStatus(), ErpTestHelper.VIGNETTE_STATUS_ACTIVE);
    }
    
    @Test
    public void activateSaleBySaleIdFail() throws ExpiredAuthTokenException, SaleIncompleteDataException, SaleRowIncompleteDataException, MessagingException, IOException {        
        System.out.println("activateSaleBySaleIdFail");        
        when(saleRepository.findOneById(ErpTestHelper.SALE_ID)).thenReturn(null);
        
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
           saleService.activateSaleBySaleId(ErpTestHelper.SALE_ID, new MockHttpSession());
        });        
    }
    
    @Test
    public void activateSaleBySaleIdIncompleteSaleDataFail() throws ExpiredAuthTokenException, SaleIncompleteDataException, SaleRowIncompleteDataException, MessagingException {
        System.out.println("activateSaleBySaleIdIncompleteSaleDataFail");        
        Sale incompleteSale = new Sale();
        incompleteSale.setId(null);
        when(saleRepository.findOneById(ErpTestHelper.SALE_ID)).thenReturn(incompleteSale);
        Assertions.assertThrows(SaleIncompleteDataException.class, () -> {
            saleService.activateSaleBySaleId(ErpTestHelper.SALE_ID, new MockHttpSession());
        });
    }

    @Test
    public void activateSaleByVignetteIdTest() throws ExpiredAuthTokenException, SaleRowNotFoundException, HttpStatusCodeException, SaleRowIncompleteDataException, MessagingException, ParseException, IOException {
        System.out.println("activateSaleByVignetteId");
        Sale saleMockedResult = testHelper.createSale();
        SaleRow saleRowMockedResult = testHelper.createSaleRow();
        Pos posResultMock = testHelper.createPos();
        User userResultMock = testHelper.createUser();
        VignetteRegistrationResponse registrationResponseMockedResult = testHelper.createVignetteRegistrationResponse();

        VignetteIdDTO vignetteIdDto = testHelper.createVignetteIdDto();

        when(saleRepository.findOneById(ErpTestHelper.SALE_ID)).thenReturn(saleMockedResult);
        when(saleRowRepository.findOneByVignetteId(ErpTestHelper.VIGNETTE_ID)).thenReturn(saleRowMockedResult);
        when(posRepository.findOneByPosIdInPartnersDb(ErpTestHelper.POS_ID)).thenReturn(posResultMock);
        when(userRepository.findOneById(siteUserId)).thenReturn(userResultMock);
        when(kapschService.activateVignette(Mockito.any(VignetteActivationRequest.class), eq(saleMockedResult.getPosId()),
                Mockito.any(HttpSession.class))).thenReturn(registrationResponseMockedResult);

        SaleRowDTO result = saleService.activateSaleByVignetteId(vignetteIdDto, new MockHttpSession());

        assertEquals(result.getVignetteId(), ErpTestHelper.VIGNETTE_ID);
        assertEquals(result.getPosId(), ErpTestHelper.POS_ID);
        assertEquals(result.getKapschProperties().getStatus(), ErpTestHelper.VIGNETTE_STATUS_ACTIVE);
        assertNotNull(result.getPos());
        assertEquals(result.getPos().getId(), ErpTestHelper.POS_ID);
    }
    
    @Test
    public void activateSaleByVignetteIdSaleRowNotFoundFail() throws ExpiredAuthTokenException, SaleRowNotFoundException, HttpStatusCodeException, SaleRowIncompleteDataException, MessagingException, ParseException {
        
        System.out.println("activateSaleByVignetteIdSaleRowNotFoundFail");
        VignetteIdDTO vignetteIdDto = testHelper.createVignetteIdDto();
        
        when(saleRowRepository.findOneByVignetteId(ErpTestHelper.VIGNETTE_ID)).thenReturn(null);
        Assertions.assertThrows(SaleRowNotFoundException.class, () -> {
            saleService.activateSaleByVignetteId(vignetteIdDto, new MockHttpSession());
        });        
    }
    
    @Test
    public void activateSaleByVignetteIdKapschActivationFail() throws ExpiredAuthTokenException, SaleRowNotFoundException, HttpStatusCodeException, SaleRowIncompleteDataException, MessagingException, ParseException {
        
        System.out.println("activateSaleByVignetteIdKapschActivationFail");
        VignetteIdDTO vignetteIdDto = testHelper.createVignetteIdDto();
        SaleRow saleRowMockedResult = testHelper.createSaleRow();
        
        Sale saleMockedResult = testHelper.createSale();       

        when(saleRepository.findOneById(ErpTestHelper.SALE_ID)).thenReturn(saleMockedResult);
        when(saleRowRepository.findOneByVignetteId(ErpTestHelper.VIGNETTE_ID)).thenReturn(saleRowMockedResult);
        when(kapschService.activateVignette(Mockito.any(VignetteActivationRequest.class), eq(saleMockedResult.getPosId()), 
                Mockito.any(HttpSession.class))).thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        
        Assertions.assertThrows(HttpStatusCodeException.class, () -> {
            saleService.activateSaleByVignetteId(vignetteIdDto, new MockHttpSession());
        });        
    }    

    @Test
    public void activateSaleByTransactionIdTest() throws ExpiredAuthTokenException, SaleIncompleteDataException, SaleRowIncompleteDataException, MessagingException, ParseException, IOException {
        System.out.println("activateSaleByTransactionId");
        Sale saleMockedResult = testHelper.createSale();
        SaleRow saleRowMockedResult = testHelper.createSaleRow();
        BatchActivationResponse batchActivationResponseMockedResult = testHelper.createBatchActivationResponse();
        ProductsResponse productsResponse = testHelper.createProductResponse();
        EVignetteProduct productMock = testHelper.createEvignetteProduct();

        when(saleRepository.findOneByBankTransactionId(ErpTestHelper.BANK_TRANSACTION_ID)).thenReturn(saleMockedResult);
        when(saleRowRepository.findBySaleId(ErpTestHelper.SALE_ID)).thenReturn(Arrays.asList(saleRowMockedResult));
        when(kapschService.activateBatch(Mockito.any(BatchActivationRequest.class), Mockito.any(HttpSession.class))).
                thenReturn(batchActivationResponseMockedResult);
        when(translationComponent.translateProduct(productMock, ErpTestHelper.LANGUAGE)).thenReturn(productsResponse);

        SaleDTO result = saleService.activateSaleByTransactionId(ErpTestHelper.BANK_TRANSACTION_ID, new MockHttpSession());
        SaleRowDTO saleRowDto = result.getSaleRows().get(0);

        assertEquals(saleRowDto.getVignetteId(), ErpTestHelper.VIGNETTE_ID);
        assertEquals(saleRowDto.getKapschProperties().getStatus(), ErpTestHelper.VIGNETTE_STATUS_ACTIVE);
        assertEquals(saleRowDto.getPartnerId(), ErpTestHelper.PARTNER_ID);
    }
    
    @Test
    public void activateSaleByTransactionIdIncompleteDataFail() throws ExpiredAuthTokenException, SaleIncompleteDataException, SaleRowIncompleteDataException, MessagingException, ParseException {
        System.out.println("activateSaleByTransactionIdFail");
        Sale sale = new Sale();
        sale.setId(null);
        when(saleRepository.findOneByBankTransactionId(ErpTestHelper.BANK_TRANSACTION_ID)).thenReturn(sale);
        
        Assertions.assertThrows(SaleIncompleteDataException.class, () -> {
            saleService.activateSaleByTransactionId(ErpTestHelper.BANK_TRANSACTION_ID, new MockHttpSession());
        });        
    }

    @Test
    public void createSaleTest() throws Exception {
        System.out.println("createSale");
        SaleDTO saleDto = testHelper.createSaleDTO();
        User user = testHelper.createUser();
        VignetteRegistrationResponse registrationResponseMockedResult = testHelper.createVignetteRegistrationResponse();
        EVignetteProduct productMock = testHelper.createEvignetteProduct();
        EVignetteInventoryProduct kapschProductMock = testHelper.createEVignetteInventoryProduct();
        Sale saleMock = testHelper.createSale();
        Vehicle vehicleMock = testHelper.createVehicle();
        ProductsResponse productsResponse = testHelper.createProductResponse();
        Pos posResultMock = testHelper.createPos();
        Partner partnerMock = testHelper.createPartner();


        when(kapschService.registerVignette(Mockito.any(VignetteRegistrationRequest.class),
                Mockito.any(HttpSession.class))).thenReturn(registrationResponseMockedResult);
        when(translationComponent.translateProduct(productMock, ErpTestHelper.LANGUAGE)).thenReturn(productsResponse);
        when(kapschProductRepository.findOneById(ErpTestHelper.KAPSCH_PRODUCT_ID)).thenReturn(kapschProductMock);
        when(sequenceGenerator.generateSequence(ErpTestHelper.SEQUENCE_NAME)).thenReturn(ErpTestHelper.SALE_SEQUENCE);
        when(saleRepository.insert(Mockito.any(Sale.class))).thenReturn(saleMock);
        when(vehicleRepository.save(Mockito.any(Vehicle.class))).thenReturn(vehicleMock);

        when(posRepository.findOneById(ErpTestHelper.POS_ID)).thenReturn(posResultMock);
        when(partnerRepository.findOneById(ErpTestHelper.PARTNER_ID)).thenReturn(partnerMock);

        SaleDTO result = saleService.createSale(saleDto, user, new MockHttpSession());
        SaleRowDTO saleRowDto = result.getSaleRows().get(0);

        assertEquals(saleRowDto.getVignetteId(), ErpTestHelper.VIGNETTE_ID);

        //In the real-world case, at this stage the vignette has status inactive (1). In our helper routines
        //we use only one status value (active) that we verify in the results.
        assertEquals(saleRowDto.getKapschProperties().getStatus(), ErpTestHelper.VIGNETTE_STATUS_ACTIVE);
        assertEquals(saleRowDto.getPartnerId(), ErpTestHelper.PARTNER_ID);
        // saleMock doesen't contain the partner pos and user, but we can check the sale rows
        assertNotNull(saleRowDto.getPartner());
        assertNotNull(saleRowDto.getPos());
        assertNotNull(saleRowDto.getUser());
        assertEquals(saleRowDto.getPartner().getId(),ErpTestHelper.PARTNER_ID);
        assertEquals(saleRowDto.getPos().getId(),ErpTestHelper.POS_ID);
        assertEquals(saleRowDto.getUser().getId(),ErpTestHelper.USER_ID);
    } 
    
    @Test
    public void createSaleKapschRegistrationFail() throws MessagingException, ExpiredAuthTokenException, ParseException {
        System.out.println("createSaleKapschRegistrationFail");
        
        EVignetteInventoryProduct kapschProductMock = testHelper.createEVignetteInventoryProduct();
        Sale saleMock = testHelper.createSale();        
        SaleDTO saleDto = testHelper.createSaleDTO();
        User user = testHelper.createUser();
        
        when(kapschProductRepository.findOneById(ErpTestHelper.KAPSCH_PRODUCT_ID)).thenReturn(kapschProductMock);
        when(sequenceGenerator.generateSequence(ErpTestHelper.SEQUENCE_NAME)).thenReturn(ErpTestHelper.SALE_SEQUENCE);
        when(saleRepository.insert(Mockito.any(Sale.class))).thenReturn(saleMock);        
        
        when(kapschService.registerVignette(Mockito.any(VignetteRegistrationRequest.class), 
        Mockito.any(HttpSession.class))).thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        
        Assertions.assertThrows(HttpStatusCodeException.class, () -> {
            saleService.createSale(saleDto, user, new MockHttpSession());
        });
    }
    
    @Test
    public void createSaleKapschRegistrationResponseEmptyFail() throws MessagingException, ExpiredAuthTokenException, ParseException {
        System.out.println("createSaleKapschRegistrationResponseEmptyFail");
        
        EVignetteInventoryProduct kapschProductMock = testHelper.createEVignetteInventoryProduct();
        Sale saleMock = testHelper.createSale();        
        SaleDTO saleDto = testHelper.createSaleDTO();
        User user = testHelper.createUser();
        VignetteRegistrationResponse registrationResponseMockedResult = new VignetteRegistrationResponse();
        registrationResponseMockedResult.seteVignette(null);
        
        when(kapschProductRepository.findOneById(ErpTestHelper.KAPSCH_PRODUCT_ID)).thenReturn(kapschProductMock);
        when(sequenceGenerator.generateSequence(ErpTestHelper.SEQUENCE_NAME)).thenReturn(ErpTestHelper.SALE_SEQUENCE);
        when(saleRepository.insert(Mockito.any(Sale.class))).thenReturn(saleMock);        
        
        when(kapschService.registerVignette(Mockito.any(VignetteRegistrationRequest.class), 
                Mockito.any(HttpSession.class))).thenReturn(registrationResponseMockedResult);    
        
        Assertions.assertThrows(MessagingException.class, () -> {
            saleService.createSale(saleDto, user, new MockHttpSession());
        });         
    }    

    @Test
    public void populateSiteUserAndCreateSaleTest() throws Exception {
        System.out.println("populateSiteUserAndCreateSale");
        User userResultMock = testHelper.createUser();
        SaleDTO saleDto = testHelper.createSaleDTO();
        VignetteRegistrationResponse registrationResponseMockedResult = testHelper.createVignetteRegistrationResponse();
        EVignetteProduct productMock = testHelper.createEvignetteProduct();
        EVignetteInventoryProduct kapschProductMock = testHelper.createEVignetteInventoryProduct();
        Sale saleMock = testHelper.createSale();
        Vehicle vehicleMock = testHelper.createVehicle();
//        VignetteRegistrationRequest requestMock = testHelper.createVignetteRegistrationRequest();
        ProductsResponse productsResponse = testHelper.createProductResponse();

        Pos posResultMock = testHelper.createPos();
        Partner partnerMock = testHelper.createPartner();

        when(kapschService.registerVignette(Mockito.any(VignetteRegistrationRequest.class),
                Mockito.any(HttpSession.class))).thenReturn(registrationResponseMockedResult);
        when(translationComponent.translateProduct(productMock, ErpTestHelper.LANGUAGE)).thenReturn(productsResponse);
        when(kapschProductRepository.findOneById(ErpTestHelper.KAPSCH_PRODUCT_ID)).thenReturn(kapschProductMock);
        when(sequenceGenerator.generateSequence(ErpTestHelper.SEQUENCE_NAME)).thenReturn(ErpTestHelper.SALE_SEQUENCE);
        when(saleRepository.insert(Mockito.any(Sale.class))).thenReturn(saleMock);
        when(vehicleRepository.save(Mockito.any(Vehicle.class))).thenReturn(vehicleMock);

        when(userRepository.findOneById(siteUserId)).thenReturn(userResultMock);
        when(posRepository.findOneById(ErpTestHelper.POS_ID)).thenReturn(posResultMock);
        when(partnerRepository.findOneById(ErpTestHelper.PARTNER_ID)).thenReturn(partnerMock);

        SaleDTO result = saleService.populateSiteUserAndCreateSale(saleDto, new MockHttpSession());

        SaleRowDTO saleRowDto = result.getSaleRows().get(0);
        assertEquals(saleRowDto.getVignetteId(), ErpTestHelper.VIGNETTE_ID);

        //In the real-world case, at this stage the vignette has status inactive (1). In our helper routines
        //we use only one status value (active) that we verify in the results.
        assertEquals(saleRowDto.getKapschProperties().getStatus(), ErpTestHelper.VIGNETTE_STATUS_ACTIVE);
        assertEquals(saleRowDto.getPartnerId(), ErpTestHelper.PARTNER_ID);
    }
    
    //Fail cases of saleService.populateSiteUserAndCreateSale are covered by the createSale() fail cases.
  

    @Test
    public void createSaleWithPartnersPosTest() throws Exception {
        System.out.println("createSaleWithPartnersPos");
        Pos posResultMock = testHelper.createPos();
        VignetteRegistrationResponse registrationResponseMockedResult = testHelper.createVignetteRegistrationResponse();
        EVignetteProduct productMock = testHelper.createEvignetteProduct();
        EVignetteInventoryProduct kapschProductMock = testHelper.createEVignetteInventoryProduct();
        Sale saleMock = testHelper.createSale();
        Vehicle vehicleMock = testHelper.createVehicle();
        SaleRequest saleRequest = testHelper.createSaleRequest();
        ProductsResponse productsResponse = testHelper.createProductResponse();
        User userResultMock = testHelper.createUser();
        Partner partnerMock = testHelper.createPartner();

        when(kapschService.registerVignette(Mockito.any(VignetteRegistrationRequest.class),
                Mockito.any(HttpSession.class))).thenReturn(registrationResponseMockedResult);
        when(translationComponent.translateProduct(productMock, ErpTestHelper.LANGUAGE)).thenReturn(productsResponse);
        when(kapschProductRepository.findOneById(ErpTestHelper.KAPSCH_PRODUCT_ID)).thenReturn(kapschProductMock);
        when(sequenceGenerator.generateSequence(ErpTestHelper.SEQUENCE_NAME)).thenReturn(ErpTestHelper.SALE_SEQUENCE);
        when(saleRepository.insert(Mockito.any(Sale.class))).thenReturn(saleMock);
        when(vehicleRepository.save(Mockito.any(Vehicle.class))).thenReturn(vehicleMock);
        when(posRepository.findOneByPosIdInPartnersDb(ErpTestHelper.POS_ID)).thenReturn(posResultMock);
        when(posRepository.findOneById(ErpTestHelper.POS_ID)).thenReturn(posResultMock);
        when(partnerRepository.findOneById(ErpTestHelper.PARTNER_ID)).thenReturn(partnerMock);

        SaleDTO result = saleService.createSaleWithPartnersPos(saleRequest, userResultMock, new MockHttpSession());

        SaleRowDTO saleRowDto = result.getSaleRows().get(0);
        assertEquals(saleRowDto.getVignetteId(), ErpTestHelper.VIGNETTE_ID);

        //In the real-world case, at this stage the vignette has status inactive (1). In our helper routines
        //we use only one status value (active) that we verify in the results.
        assertEquals(saleRowDto.getKapschProperties().getStatus(), ErpTestHelper.VIGNETTE_STATUS_ACTIVE);
        assertEquals(saleRowDto.getPartnerId(), ErpTestHelper.PARTNER_ID);
    }
    
    //Fail cases of saleService.createSaleWithPartnersPos are covered by the createSale() fail cases.

    @Test
    public void fillMissedSaleSequencesWhenAllZeroTest() throws ParseException {
        System.out.println("fillMissedSaleSequencesWhenAllZero");
        
        String SALE_ID_1 = "saleId1";
        String SALE_ID_2 = "saleId2";
        
        Long expectedResult = 2L;
        
        long SALE_SEQ_1 = 0;
        long SALE_SEQ_2 = 0;

        Sale saleMock1 = testHelper.createSale();
        Sale saleMock2 = testHelper.createSale();

        saleMock1.setId(SALE_ID_1);
        saleMock2.setId(SALE_ID_2);

        saleMock1.setSaleSeq(SALE_SEQ_1);
        saleMock2.setSaleSeq(SALE_SEQ_2);

        List<Sale> sales = Arrays.asList(saleMock1, saleMock2);
        SaleRow saleRow1Mock = testHelper.createSaleRow();
        SaleRow saleRow2Mock = testHelper.createSaleRow();

        saleRow1Mock.setSaleId(SALE_ID_1);
        saleRow2Mock.setSaleId(SALE_ID_2);

        when(saleRepository.findAll()).thenReturn(sales);
        when(sequenceGenerator.generateSequence(ErpTestHelper.SEQUENCE_NAME)).thenReturn(ErpTestHelper.SALE_SEQUENCE);
        when(saleRowRepository.findBySaleId(SALE_ID_1)).thenReturn(Arrays.asList(saleRow1Mock));
        when(saleRowRepository.findBySaleId(SALE_ID_2)).thenReturn(Arrays.asList(saleRow2Mock));

        Long result = saleService.fillMissedSaleSequences();

        assertEquals(result, expectedResult);
    }
    
    @Test
    public void fillMissedSaleSequencesWhenAllNonZeroTest() throws ParseException {
        System.out.println("fillMissedSaleSequencesWhenAllNonZero");
        
        long SALE_SEQ_1 = 10;
        long SALE_SEQ_2 = 11;
        
        Sale saleMock1 = testHelper.createSale();
        Sale saleMock2 = testHelper.createSale();
        
        saleMock1.setSaleSeq(SALE_SEQ_1);
        saleMock2.setSaleSeq(SALE_SEQ_2);        
        
        List<Sale> sales = Arrays.asList(saleMock1, saleMock2);
        
        when(saleRepository.findAll()).thenReturn(sales);
        when(sequenceGenerator.generateSequence(ErpTestHelper.SEQUENCE_NAME)).thenReturn(ErpTestHelper.SALE_SEQUENCE);
        
        saleService.fillMissedSaleSequences();
        
        verify(sequenceGenerator, never()).generateSequence(ErpTestHelper.SEQUENCE_NAME);
    }

    @Test
    public void testUpdateSaleTransactionId() throws ParseException {
        System.out.println("updateSaleTransactionId");
        Optional<Sale> saleRepositoryFindMock = Optional.of(testHelper.createSale());
        Sale saleRepositorySaveMock = testHelper.createSale();

        when(saleRepository.findById(ErpTestHelper.SALE_ID)).thenReturn(saleRepositoryFindMock);
        when(saleRepository.save(Mockito.any(Sale.class))).thenReturn(saleRepositorySaveMock);

        Sale result = saleService.updateSaleTransactionId(ErpTestHelper.SALE_ID, ErpTestHelper.BANK_TRANSACTION_ID);

        assertEquals(result.getBankTransactionId(), ErpTestHelper.BANK_TRANSACTION_ID);
        assertEquals(result.getId(), ErpTestHelper.SALE_ID);

    }
    
    @Test
    public void testUpdateSaleTransactionIdNoSuchElementFail() throws ParseException {
        System.out.println("updateSaleTransactionId");
        Optional<Sale> emptySaleMock = Optional.empty();
        when(saleRepository.findById(ErpTestHelper.SALE_ID)).thenReturn(emptySaleMock);
        
        Assertions.assertThrows(NoSuchElementException.class, () -> {
            saleService.updateSaleTransactionId(ErpTestHelper.SALE_ID, ErpTestHelper.BANK_TRANSACTION_ID);
        });           
    }    
    
}
