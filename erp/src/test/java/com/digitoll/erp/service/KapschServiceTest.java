package com.digitoll.erp.service;

import com.digitoll.commons.exception.ExpiredAuthTokenException;
import com.digitoll.commons.kapsch.classes.Api;
import com.digitoll.commons.kapsch.classes.EVignetteInventoryProduct;
import com.digitoll.commons.kapsch.request.BatchActivationRequest;
import com.digitoll.commons.kapsch.request.VignetteActivationRequest;
import com.digitoll.commons.kapsch.request.VignetteRegistrationRequest;
import com.digitoll.commons.kapsch.response.*;
import com.digitoll.commons.kapsch.response.c9.*;
import com.digitoll.commons.kapsch.request.AuthenticationRequest;
import com.digitoll.erp.utils.ErpTestHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import javax.servlet.http.HttpSession;
import org.junit.Test;

import static com.digitoll.erp.utils.ErpTestHelper.SALES_PARTNER_ID;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@TestPropertySource("classpath:test.properties")
public class KapschServiceTest {
    
    @Value("${cbo.api.endpoint.c2}")
    private String endpointC2;

    @Value("${cbo.api.version.c2}")
    private String apiVersionC2;

    @Value("${cbo.api.endpoint.c9}")
    private String endpointC9;

    @Value("${cbo.api.version.c9}")
    private String apiVersionC9;    
    
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HttpSession session;
    
    @InjectMocks
    private KapschService kapschService;
            
    private ErpTestHelper testHelper;

    private String authToken = "token";

    @Before
    public void init() {

        testHelper = new ErpTestHelper();

        ReflectionTestUtils.setField(kapschService, "apiUsername", ErpTestHelper.USERNAME);
        ReflectionTestUtils.setField(kapschService, "apiPassword", ErpTestHelper.PASSWORD);
        ReflectionTestUtils.setField(kapschService, "authToken", authToken);
        ReflectionTestUtils.setField(kapschService, "endpointC2", endpointC2);
        ReflectionTestUtils.setField(kapschService, "apiVersionC2", apiVersionC2);
        ReflectionTestUtils.setField(kapschService, "endpointC9", endpointC9);
        ReflectionTestUtils.setField(kapschService, "apiVersionC9", apiVersionC9);        
    }

    @Test
    public void testGetVersion() {

        ReflectionTestUtils.setField(kapschService, "authToken", null);

        when(restTemplate.getForObject(endpointC2 + "/" + apiVersionC2, ApiVersionResponse.class))
                .thenReturn(new ApiVersionResponse());

        ApiVersionResponse response = kapschService.getVersion();

        assertEquals(response, new ApiVersionResponse());
    }

    @Test
    public void testGetInventory() throws Exception {

        VignetteInventoryResponse vignetteInventoryResponse = this.createVignetteInventoryResponse();

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(VignetteInventoryResponse.class)))
                .thenReturn(new ResponseEntity<>(vignetteInventoryResponse, HttpStatus.OK));

        VignetteInventoryResponse result = kapschService.getInventory(session);
        String url = endpointC2 + "/" + apiVersionC2 + "/evstore/inventory";
        
        EVignetteInventoryProduct product = result.getProducts().get(0);

        assertEquals(product.getEmissionClass(), ErpTestHelper.EMISSION_CLASS);
        assertEquals((int)product.getId(), ErpTestHelper.PRODUCT_ID);
        
        verify(restTemplate).exchange(
            url,
            HttpMethod.GET,
            new HttpEntity(null, this.createHeadersWithToken(authToken)),
            VignetteInventoryResponse.class
        );
    }

    @Test
    public void testRegisterVignette() throws Exception {

        VignetteRegistrationRequest request = testHelper.createVignetteRegistrationRequest();
        VignetteRegistrationResponse response = testHelper.createVignetteRegistrationResponse();

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(VignetteRegistrationResponse.class)))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        VignetteRegistrationResponse result = kapschService.registerVignette(request, session);
        String url = endpointC2 + "/" + apiVersionC2 + "/evstore/evignettes";
        
        assertEquals(result.geteVignette().getId(), ErpTestHelper.VIGNETTE_ID);
        
        verify(restTemplate).exchange(
            url,
            HttpMethod.POST,
            new HttpEntity(request, this.createHeadersWithToken(authToken)),
            VignetteRegistrationResponse.class
        );
    }

    @Test
    public void testActivateVignette() throws Exception {

        VignetteActivationRequest request = new VignetteActivationRequest();
        request.setId(Long.parseLong(ErpTestHelper.VIGNETTE_ID));
        
        VignetteRegistrationResponse response = testHelper.createVignetteRegistrationResponse();

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(VignetteRegistrationResponse.class)))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        VignetteRegistrationResponse result = kapschService.activateVignette(request, ErpTestHelper.POS_ID, session);
        String url = endpointC2 + "/" + apiVersionC2 + "/evstore/evignettes/" + ErpTestHelper.VIGNETTE_ID + 
                "/activate";
        
        assertEquals(result.geteVignette().getId(), ErpTestHelper.VIGNETTE_ID);
        
        verify(restTemplate).exchange(
            url,
            HttpMethod.POST,
            new HttpEntity(request, this.createHeadersWithToken(authToken)),
            VignetteRegistrationResponse.class
        );
    }

    @Test
    public void testActivateBatch() throws Exception {

        BatchActivationRequest request = new BatchActivationRequest();
        request.addId(ErpTestHelper.VIGNETTE_ID);
        request.addId(ErpTestHelper.VIGNETTE_ID_2);
        
        BatchActivationResponse response = testHelper.createBatchActivationResponse();
        
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(BatchActivationResponse.class)))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        BatchActivationResponse result = kapschService.activateBatch(request, session);
        String url = endpointC2 + "/" + apiVersionC2 + "/evstore/evignettes/batch_activate";
        
        assertEquals(result.geteVignettes().get(0).getId(), ErpTestHelper.VIGNETTE_ID);
        assertEquals(result.geteVignettes().get(1).getId(), ErpTestHelper.VIGNETTE_ID_2);
        
        verify(restTemplate).exchange(
            url,
            HttpMethod.POST,
            new HttpEntity(request, this.createHeadersWithToken(authToken)),
            BatchActivationResponse.class
        );
    }

    @Test
    public void testGetDailySales() throws Exception {

        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        SaleResponse saleResponse = this.createSaleResponse();
        AggregatedSaleResponse aggrSaleResponse = new AggregatedSaleResponse();
        
        aggrSaleResponse.setSales(Arrays.asList(saleResponse));
        aggrSaleResponse.setSalesPartnerID(SALES_PARTNER_ID);
        
        AggregatedSalesResponse aggrSalesResponse = new AggregatedSalesResponse();
        aggrSalesResponse.setSalesPerPartners(Arrays.asList(aggrSaleResponse));

        String url = endpointC9 + "/" +
                apiVersionC9 + "/" +
                "evstore/aggregatedsales" +
                "?date=" + sdf.format(date);        
        
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(AggregatedSalesResponse.class)))
                .thenReturn(new ResponseEntity<>(aggrSalesResponse, HttpStatus.OK));
        
        AggregatedSalesResponse response = kapschService.getDailySales(date, session);

        assertEquals((int)response.getSalesPerPartners().get(0).getSalesPartnerID(), 
                SALES_PARTNER_ID);
        
        assertEquals((int)response.getSalesPerPartners().get(0).getSales().get(0).getProductID(), ErpTestHelper.PRODUCT_ID);
        
        verify(restTemplate).exchange(
            url,
            HttpMethod.GET,
            new HttpEntity(null, this.createHeadersWithToken(authToken)),
            AggregatedSalesResponse.class
        );
    }

    @Test
    public void testGetVignetteStates() throws Exception {

        final String STATUS_DESCRIPTION_ACTIVE = "active";
        
        VignetteStateResponse stateResponse = new VignetteStateResponse();
        stateResponse.setDescription(STATUS_DESCRIPTION_ACTIVE);
        stateResponse.setId(ErpTestHelper.VIGNETTE_STATUS_ACTIVE);
        VignetteStatesResponse response = new VignetteStatesResponse();
        response.setStates(Arrays.asList(stateResponse));
        
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(VignetteStatesResponse.class)))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        VignetteStatesResponse result = kapschService.getVignetteStates(session);
        String url = endpointC9 + "/" + apiVersionC9 + "/evstore/regdata/states";

        assertEquals(result.getStates().get(0).getDescription(), STATUS_DESCRIPTION_ACTIVE);
        assertEquals(result.getStates().get(0).getId(), (int)ErpTestHelper.VIGNETTE_STATUS_ACTIVE);
        
        verify(restTemplate).exchange(
            url,
            HttpMethod.GET,
            new HttpEntity(null, this.createHeadersWithToken(authToken)),
            VignetteStatesResponse.class
        );        
    }

    @Test
    public void testGetInventoryC9() throws Exception {

        VignetteInventoryResponse vignetteInventoryResponse = this.createVignetteInventoryResponse();

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(VignetteInventoryResponse.class)))
                .thenReturn(new ResponseEntity<>(vignetteInventoryResponse, HttpStatus.OK));

        VignetteInventoryResponse result = kapschService.getInventoryC9(session);
        String url = endpointC9 + "/" + apiVersionC9 + "/evstore/inventory";
        
        EVignetteInventoryProduct product = result.getProducts().get(0);

        assertEquals(product.getEmissionClass(), ErpTestHelper.EMISSION_CLASS);
        assertEquals((int)product.getId(), ErpTestHelper.PRODUCT_ID);
        
        verify(restTemplate).exchange(
            url,
            HttpMethod.GET,
            new HttpEntity(null, this.createHeadersWithToken(authToken)),
            VignetteInventoryResponse.class
        );
    }

    @Test
    public void testVignetteSearch() throws Exception {
        SearchResponse searchResponse = this.createSearchResponse();
        
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(SearchResponse.class)))
                .thenReturn(new ResponseEntity<>(searchResponse, HttpStatus.OK));                

        String queryString =
                "recordsFrom=" +
                        "&lastUpdateFrom=" +
                        "&lastUpdateTo=" +
                        "&eVignetteID=" + ErpTestHelper.VIGNETTE_ID +
                        "&salesPartnerID=" +
                        "&productID=" +
                        "&lpn=" +
                        "&countryCode=" +
                        "&status=";
        
        String url = String.format("%s/%s/%s?%s", endpointC9, apiVersionC9,
                "evstore/search",
                queryString
        );        
        
        SearchResponse result = kapschService.vignetteSearch(ErpTestHelper.VIGNETTE_ID, session);
        
        assertEquals(result.geteVignetteList().get(0).getId(), ErpTestHelper.VIGNETTE_ID);
        assertEquals(result.geteVignetteList().get(0).getPosID(), ErpTestHelper.POS_ID);
        
        verify(restTemplate).exchange(
            url,
            HttpMethod.GET,
            new HttpEntity(null, this.createHeadersWithToken(authToken)),
            SearchResponse.class
        );        
    }

    @Test
    public void testVignetteInfo() throws Exception {
        VignetteRegistrationResponse infoResponse = testHelper.createVignetteRegistrationResponse();
        
        String url = endpointC2 + "/" + apiVersionC2 + "/" + "evstore/evignettes" +
                "/" + ErpTestHelper.VIGNETTE_ID + "/" + "info";        
        
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(VignetteRegistrationResponse.class)))
                .thenReturn(new ResponseEntity<>(infoResponse, HttpStatus.OK));            
        
        VignetteRegistrationResponse result = kapschService.vignetteInfo(ErpTestHelper.VIGNETTE_ID, session);
        assertEquals(result.geteVignette().getId(), ErpTestHelper.VIGNETTE_ID);
        
        verify(restTemplate).exchange(
            url,
            HttpMethod.GET,
            new HttpEntity(null, this.createHeadersWithToken(authToken)),
            VignetteRegistrationResponse.class
        );         
    }

    @Test
    public void testVignetteSearch_14args() throws Exception {

        String sortingParameter = ErpTestHelper.SORT_PROPERTY;
        Sort.Direction sortingDirection = ErpTestHelper.SORTING_DIRECTION;
        int lastRecord = 1;
        String eVignetteID = ErpTestHelper.VIGNETTE_ID;
        Integer salesPartnerID = SALES_PARTNER_ID;
        Integer productID = ErpTestHelper.PRODUCT_ID;
        String lpn = ErpTestHelper.LPN;
        String countryCode = ErpTestHelper.COUNTRY_CODE;
        String recordsFrom = "0";
        Integer status = ErpTestHelper.VIGNETTE_STATUS_ACTIVE;
        PageRequest page = new PageRequest(0, 1);
        PaginatedKapshSearchResponse paginatedResponse = new PaginatedKapshSearchResponse(); 
        
        SearchResponse tempResponse = this.createSearchResponse();
        paginatedResponse.setHasMoreRecords(false);
        paginatedResponse.setLastRecord(1);
        paginatedResponse.seteVignetteList(tempResponse.geteVignetteList());
        
        String queryString =
                "recordsFrom=" + recordsFrom +
                        "&lastUpdateFrom=" +
                        "&lastUpdateTo=" +
                        "&eVignetteID=" + eVignetteID +
                        "&salesPartnerID=" + salesPartnerID +
                        "&productID=" + productID +
                        "&lpn=" + lpn +
                        "&countryCode=" + countryCode +
                        "&status=" + status;

        String url = String.format("%s/%s/%s?%s", endpointC9, apiVersionC9,
                "evstore/search",
                queryString
        );        
        
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(SearchResponse.class)))
                .thenReturn(new ResponseEntity<>(tempResponse, HttpStatus.OK));              
        
        PaginatedKapshSearchResponse result = kapschService.vignetteSearch(sortingParameter, sortingDirection,
                lastRecord, eVignetteID, salesPartnerID, productID, lpn, countryCode, recordsFrom,
                status, null, null, session, page);
        
        assertEquals(result.geteVignetteList().get(0).getId(), ErpTestHelper.VIGNETTE_ID);
        assertEquals(result.geteVignetteList().get(0).getPosID(), ErpTestHelper.POS_ID);
        
        verify(restTemplate).exchange(
            url,
            HttpMethod.GET,
            new HttpEntity(null, this.createHeadersWithToken(authToken)),
            SearchResponse.class
        );          
    }

    @Test
    public void testGetPeriodSales() throws Exception {
        Integer salesPartnerID = SALES_PARTNER_ID;
        Integer productID = ErpTestHelper.PRODUCT_ID;
        String recordsFrom = "0";
        String dateFrom = "2015-09-21T09:30:00.000Z";
        String dateTo = "2015-09-24T09:30:00.000Z";
        PeriodSalesResponse response = this.createPeriodSalesResponse();
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        String queryString =
                "recordsFrom=" + recordsFrom +
                        "&purchaseFromDate=" + dateFrom +
                        "&purchaseToDate=" + dateTo +
                        "&salesPartnerID=" + salesPartnerID +
                        "&productID=" + productID;

        String url = String.format("%s/%s/%s?%s", endpointC9, apiVersionC9,
                "evstore/evsales",
                queryString
        );        
        
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(PeriodSalesResponse.class)))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));         
        
        PeriodSalesResponse result = kapschService.getPeriodSales(salesPartnerID, productID, recordsFrom, sdf.parse(dateFrom), sdf.parse(dateTo), session);
        
        assertEquals(result.getSingleSales().get(0).geteVignetteID(), ErpTestHelper.VIGNETTE_ID);
        assertEquals(result.getSingleSales().get(0).getSalesPartner(), ErpTestHelper.PARTNER_NAME);

        verify(restTemplate).exchange(
            url,
            HttpMethod.GET,
            new HttpEntity(null, this.createHeadersWithToken(authToken)),
            PeriodSalesResponse.class
        );           
    }
    
    @Test
    public void testGetInventoryNoAuthToken() {
        
        authTokenMock();

        try {
            kapschService.getInventory(session);
        } catch (ExpiredAuthTokenException e) {

            authTokenCheck(endpointC2 + "/" + apiVersionC2);
        }
    }

    @Test
    public void testRegisterVignetteNoAuth() {

        authTokenMock();

        try {
            kapschService.registerVignette(new VignetteRegistrationRequest(), session);
        } catch (ExpiredAuthTokenException e) {

            authTokenCheck(endpointC2 + "/" + apiVersionC2);
        }
    }

    @Test
    public void testActivateVignetteNoAuth() {

        authTokenMock();

        try {
            kapschService.activateVignette(new VignetteActivationRequest(), ErpTestHelper.POS_ID, session);
        } catch (ExpiredAuthTokenException e) {

            authTokenCheck(endpointC2 + "/" + apiVersionC2);
        }
    }

    @Test
    public void testActivateBatchNoAuth() {

        authTokenMock();

        try {
            kapschService.activateBatch(new BatchActivationRequest(), session);
        } catch (ExpiredAuthTokenException e) {

            authTokenCheck(endpointC2 + "/" + apiVersionC2);
        }
    }

    @Test
    public void testGetDailySalesNoAuth() {

        authTokenMock();

        try {
            kapschService.getDailySales(new Date(), session);
        } catch (ExpiredAuthTokenException e) {

            authTokenCheck(endpointC9 + "/" + apiVersionC9);
        }
    }

    @Test
    public void testGetVignetteStatesNoAuth() {

        authTokenMock();

        try {
            kapschService.getVignetteStates(session);
        } catch (ExpiredAuthTokenException e) {

            authTokenCheck(endpointC9 + "/" + apiVersionC9);
        }
    }

    @Test
    public void testGetInventoryC9NoAuth() {

        authTokenMock();

        try {
            kapschService.getInventoryC9(session);
        } catch (ExpiredAuthTokenException e) {

            authTokenCheck(endpointC9 + "/" + apiVersionC9);
        }
    }

    @Test
    public void testVignetteSearchNoAuth() {

        authTokenMock();

        try {
            kapschService.vignetteSearch(ErpTestHelper.VIGNETTE_ID, session);
        } catch (ExpiredAuthTokenException e) {

            authTokenCheck(endpointC9 + "/" + apiVersionC9);
        }
    }

    @Test
    public void testVignetteInfoNoAuth() {

        authTokenMock();

        try {
            kapschService.vignetteInfo(ErpTestHelper.VIGNETTE_ID, session);
        } catch (ExpiredAuthTokenException e) {

            authTokenCheck(endpointC2 + "/" + apiVersionC2);
        }
    }

    @Test
    public void testVignetteSearch_14argsNoAuth() {

        authTokenMock();

        try {

            PageRequest page = new PageRequest(0, 1);

            kapschService.vignetteSearch(ErpTestHelper.SORT_PROPERTY, ErpTestHelper.SORTING_DIRECTION, 1,
                    ErpTestHelper.VIGNETTE_ID, SALES_PARTNER_ID,
                    ErpTestHelper.PRODUCT_ID, ErpTestHelper.LPN, ErpTestHelper.COUNTRY_CODE, "0",
                    ErpTestHelper.VIGNETTE_STATUS_ACTIVE, null, null, session, page);
        } catch (ExpiredAuthTokenException e) {

            authTokenCheck(endpointC9 + "/" + apiVersionC9);
        }
    }

    @Test
    public void testGetPeriodSalesNoAuth() throws ParseException {

        authTokenMock();

        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

            kapschService.getPeriodSales(SALES_PARTNER_ID, ErpTestHelper.PRODUCT_ID,
                    "0", sdf.parse("2015-09-21T09:30:00.000Z"), sdf.parse("2015-09-24T09:30:00.000Z"), session);
        } catch (ExpiredAuthTokenException e) {

            authTokenCheck(endpointC9 + "/" + apiVersionC9);
        }
    }


    /** Helper functions **/
    
    private VignetteInventoryResponse createVignetteInventoryResponse() {
        VignetteInventoryResponse response = new VignetteInventoryResponse();
        EVignetteInventoryProduct product = testHelper.createEVignetteInventoryProduct();
        
        response.setProducts(Arrays.asList(product));
        
        return response;
    }    
    
    private SaleResponse createSaleResponse() {
        
        SaleResponse saleResponse = new SaleResponse(); 
        saleResponse.setPrice(testHelper.createVignettePrice());
        saleResponse.setTotalSum(testHelper.createVignettePrice());
        saleResponse.setProductID(ErpTestHelper.PRODUCT_ID);
        saleResponse.setQuantity(1L);
        
        return saleResponse;
    }

    private HttpHeaders createHeadersWithToken(String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }
    
    private SearchResponse createSearchResponse() {
        SearchResponse searchResponse = new SearchResponse();
        searchResponse.seteVignetteList(Arrays.asList(this.createSearchVignette()));
        searchResponse.setHasMoreRecords(false);
        searchResponse.setLastRecord(1);
        
        return searchResponse;
    }
    
    private SearchVignette createSearchVignette() {
        
        SearchVignette searchVignette = new SearchVignette();        
        searchVignette.setPrice(this.createSearchVignettePrice());
        searchVignette.setProduct(this.createSearchVignetteProduct());
        searchVignette.setPurchase(this.createSearchVignettePurchase());
        searchVignette.setValidity(this.createSearchVignetteValidity());
        searchVignette.setVehicle(this.createSearchVignetteVehicle());
        
        searchVignette.setId(ErpTestHelper.VIGNETTE_ID);
        searchVignette.setPosID(ErpTestHelper.POS_ID);
        searchVignette.setSalesPartnerID(SALES_PARTNER_ID);
        searchVignette.setSalesPartner(ErpTestHelper.PARTNER_NAME);
        searchVignette.setStatus(ErpTestHelper.VIGNETTE_STATUS_ACTIVE);
                
        return searchVignette;
    }
    
    private SearchVignettePrice createSearchVignettePrice() {
        SearchVignettePrice price = new SearchVignettePrice();
        
        price.setAmount(ErpTestHelper.VIGNETTE_SEARCH_PRICE);
        price.setCurrency(ErpTestHelper.SEARCH_CURRENCY);
        return price;
    }
    
    private SearchVignetteProduct createSearchVignetteProduct() {
        SearchVignetteProduct product = new SearchVignetteProduct();
        
        product.setEmissionClass(ErpTestHelper.EMISSION_CLASS_TEXT);
        product.setValidityType(ErpTestHelper.VIGNETTE_VALIDITY_TYPE_TEXT);
        product.setVehicleType(ErpTestHelper.VIGNETTE_VEHICLE_TYPE_TEXT);
        product.setId(ErpTestHelper.PRODUCT_ID);
        
        return product;
    }
    
    private SearchVignettePurchase createSearchVignettePurchase() {
        SearchVignettePurchase purchase = new SearchVignettePurchase();
        purchase.setPurchaseDateTimeUTC(ErpTestHelper.CREATED_ON);
        return purchase;
    }
    
    private SearchVignetteValidity createSearchVignetteValidity() {
        SearchVignetteValidity validity = new SearchVignetteValidity();
        validity.setRequestedValidityStartDate(ErpTestHelper.VALIDITY_START_DATE);
        validity.setValidityEndDateTimeUTC(ErpTestHelper.VALIDITY_END_DATE);
        validity.setValidityStartDateTimeUTC(ErpTestHelper.VALIDITY_START_DATE);
        
        return validity;
    }
    
    private SearchVignetteVehicle createSearchVignetteVehicle() {
        SearchVignetteVehicle vehicle = new SearchVignetteVehicle();
        vehicle.setCountryCode(ErpTestHelper.COUNTRY_CODE);
        vehicle.setLpn(ErpTestHelper.LPN);
        return vehicle;
    }
    
    private VignetteSingleSalePurchase createVignetteSingleSalePurchase() {
        VignetteSingleSalePurchase purchase = new VignetteSingleSalePurchase();
        purchase.setPurchaseDateTimeUTC(ErpTestHelper.CREATED_ON);
        return purchase;
    }
    
    private VignetteSingleSale createVignetteSingleSale() {
        VignetteSingleSale sale = new VignetteSingleSale();
        
        sale.setProductID(ErpTestHelper.PRODUCT_ID);
        sale.setPurchase(this.createVignetteSingleSalePurchase());
        sale.setSalesPartner(ErpTestHelper.PARTNER_NAME);
        sale.setSalesPartnerID(SALES_PARTNER_ID);
        sale.seteVignetteID(ErpTestHelper.VIGNETTE_ID);
        
        return sale;
    }
    
    private PeriodSalesResponse createPeriodSalesResponse() {
        PeriodSalesResponse response = new PeriodSalesResponse();
        
        response.setHasMoreRecords(false);
        response.setLastRecord("0");
        response.setSingleSales(Arrays.asList(this.createVignetteSingleSale()));
                
        return response;
    }
    
    private AuthenticationResponse anAuthenticationResponse() {

        AuthenticationResponse authenticationResponse = new AuthenticationResponse();
        authenticationResponse.setToken(authToken);

        return authenticationResponse;
    }  
    
    private AuthenticationRequest anAuthenticationRequest() {

        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        
        Api api = new Api();

        api.setUserName(ErpTestHelper.USERNAME);
        api.setPassword(ErpTestHelper.PASSWORD);
        
        authenticationRequest.setApi(api);
        authenticationRequest.setPosId(null);

        return authenticationRequest;
    }

    private void authTokenMock() {

        ReflectionTestUtils.setField(kapschService, "authToken", null);

        when(restTemplate.postForObject(anyString(), any(), any())).thenReturn(anAuthenticationResponse());
    }
    
    private void authTokenCheck(String kapschEndpoint) {
        
        verify(restTemplate).postForObject(
                kapschEndpoint + "/auth",
                anAuthenticationRequest(),
                AuthenticationResponse.class
        );

        assertNotNull(ReflectionTestUtils.getField(kapschService, "authToken"));
    }    
}
