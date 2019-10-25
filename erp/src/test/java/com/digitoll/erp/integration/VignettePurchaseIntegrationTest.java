package com.digitoll.erp.integration;

import com.digitoll.commons.model.Partner;
import com.digitoll.commons.model.Pos;
import com.digitoll.commons.model.Role;
import com.digitoll.commons.model.User;
import com.digitoll.commons.model.Vehicle;
import com.digitoll.commons.request.AuthenticationRequest;
import com.digitoll.commons.request.SaleRequest;
import com.digitoll.commons.request.SaleRowRequest;
import com.digitoll.commons.response.SaleDTO;
import com.digitoll.commons.response.SaleRowDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

@RunWith(SpringRunner.class)
@SpringBootTest
//@SpringBootConfiguration
@AutoConfigureMockMvc
@TestPropertySource(
  locations = "classpath:integrationtest.properties")
public class VignettePurchaseIntegrationTest {
    
    private static final Logger log = LoggerFactory.getLogger(VignettePurchaseIntegrationTest.class);
    
    final int VIGNETTE_STATUS_INACTIVE = 1;
    final int VIGNETTE_STATUS_ACTIVE = 2;    
    
    final int weeklyCarVignetteKapschId = 101;
    final int monthlyCarVignetteKapschId = 102;
    
    final String API_AUTH_URL = "/user/authenticate";
    final String API_REGISTER_URL = "/sale"; 
    final String API_ACTIVATE_URL = "/sale/activate/";
    
    final String RESPONSE_MESSAGE_BAD_CREDENTIALS = "Bad credentials";
    final String RESPONSE_INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
    
    @Value("${itest.userEmail}")
    private String userEmail;    
    
    @Value("${itest.userPassword}")
    private String userPassword;
    
    @Value("${itest.userPasswordHash}")
    private String userPasswordHash;    
    
    @Value("${itest.firstName}")
    private String firstName;     

    @Value("${itest.lastName}")
    private String lastName;  
    
    @Value("${itest.orderEmail}")
    private String orderEmail;    
    
    @Value("${itest.c2RoleCode}")
    private String c2RoleCode;      
    
    @Value("${itest.noPurchaseAllowedRoleCode}")
    private String noPurchaseAllowedrole;      
    
    @Value("${itest.partnerName}")
    private String testPartnerName;
    
    @Value("${itest.posName}")
    private String testPosName;         
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    private long testDbEntrySuffix = System.currentTimeMillis();
    
    @After
    public void initDb() {
        deleteTestUserPartnerPos(userEmail + testDbEntrySuffix, testPartnerName + testDbEntrySuffix, testPosName + testDbEntrySuffix);
    }
    
    private Partner createTestPartner(String partnerName) {
        Partner partner = new Partner();
        
        partner.setName(partnerName + testDbEntrySuffix);
        partner.setKapschPartnerId("12345");
        return mongoTemplate.save(partner);
    }    
    
    private Pos createTestPos(String partnerId, String posName) {
        Pos pos = new Pos();
        
        pos.setName(posName + testDbEntrySuffix);
        pos.setPartnerId(partnerId);
        return mongoTemplate.save(pos);        
    }
    
    private TestUserPartnerPos createTestUserPartnerPosWithC2() {
        
        Query query = new Query();
        query.addCriteria(Criteria.where("code").is(c2RoleCode));
        List<Role> roleList = mongoTemplate.find(query, Role.class);
        
        return this.createTestUserPartnerPos(roleList);
    }
    
    private TestUserPartnerPos createTestUserPartnerPosWithNoPurchaseAccess() {
        Query query = new Query();
        query.addCriteria(Criteria.where("code").is(noPurchaseAllowedrole));
        List<Role> roleList = mongoTemplate.find(query, Role.class);
        
        return this.createTestUserPartnerPos(roleList);        
    }
    
    private TestUserPartnerPos createTestUserPartnerPos(List<Role> roles) {
        
        User user;
        Pos pos;
        Partner partner;
        TestUserPartnerPos result;
        List<String> posIds = new ArrayList<>();

        partner = this.createTestPartner(testPartnerName);
        pos = this.createTestPos(partner.getId(), testPosName);
        posIds.add(pos.getId());
        user = this.createTestUser(roles, posIds, partner.getId());
        
        result = new TestUserPartnerPos(pos, partner, user);
        
        return result;
    }
    
    private User createTestUser(List<Role> roles, List<String> posId, String partnerId) {
        
        User user = new User();
        
        user.setUsername(userEmail + testDbEntrySuffix);
        user.setPassword(userPasswordHash);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPosIds(posId);
        user.setRoles(roles);
        user.setPartnerId(partnerId);
        
        return mongoTemplate.save(user);        
    }
    
    private void deleteTestUserPartnerPos(String testUserEmail, String testPartnerName, String testPosName) {
        
        Query query = new Query();
        query.addCriteria(Criteria.where("username").is(testUserEmail));
        mongoTemplate.remove(query, User.class);
        
        query = new Query();
        query.addCriteria(Criteria.where("name").is(testPartnerName));
        mongoTemplate.remove(query, Partner.class);

        query = new Query();
        query.addCriteria(Criteria.where("name").is(testPosName));
        mongoTemplate.remove(query, Pos.class);
    }    
    
    private String generateValidRandomLpn() {
        String lpn;
        Random random = new Random();     
        
        lpn = "CM" + String.valueOf(random.nextInt((9999 - 1111) + 1) + 1111) + "PP";
        
        return lpn;
    }
    
    private String doAuth() throws Exception {
        
        AuthenticationRequest payload = new AuthenticationRequest();
        payload.setUserName(userEmail + testDbEntrySuffix);
        payload.setPassword(userPassword);
        String responseContent;
        String token;
        
        String jsonBody = new ObjectMapper().writeValueAsString(payload);

        MvcResult results = this.mockMvc.perform(post(API_AUTH_URL)
                .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody)).andReturn();
        responseContent = results.getResponse().getContentAsString();
        
        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseJson = mapper.readTree(responseContent);

        token = responseJson.get("token").asText();
        
        return token;
    }
    
    private String registerVignettes(String token, List<SaleRowRequest> saleRows, String posId, LocalDateTime activationDate,
            String userId, HttpStatus expectedStatus) throws Exception {
        
        String result = null;
        ObjectMapper mapper = new ObjectMapper();
		
        SaleRequest saleRequest = new SaleRequest();
        
        String jsonBody;
        MvcResult response = null;

        saleRequest.setPosId(posId);
        saleRequest.setSaleRows(saleRows);
        saleRequest.setUserId(userId);          
        
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);        
        
        jsonBody = mapper.writeValueAsString(saleRequest);
        
        log.debug("register request: " + jsonBody);
        
        if (expectedStatus.is2xxSuccessful()) {
            response = this.mockMvc.perform(post(API_REGISTER_URL).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))/*.andDo(print())*/.andExpect(status().isOk()).andReturn();        
        }
        else if (expectedStatus.is5xxServerError()) {
            response = this.mockMvc.perform(post(API_REGISTER_URL).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody)).andExpect(status().is5xxServerError()).andReturn();
        }
        else if (expectedStatus.is4xxClientError()) {
            response = this.mockMvc.perform(post(API_REGISTER_URL).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody)).andExpect(status().is4xxClientError()).andReturn();
        }
        
        if (response != null) {
            result = response.getResponse().getContentAsString();
        }
        
        return result;
    }
    
    private String activateVignette(String token, String saleId, HttpStatus expectedStatus) throws Exception {
        
        String result = null;
        MvcResult response = null;
        
        if (expectedStatus.is2xxSuccessful()) {
            response = this.mockMvc.perform(post(API_ACTIVATE_URL + saleId).header("Authorization", "Bearer " + token)).andExpect(status().isOk()).andReturn();        
        }
        else if (expectedStatus.is5xxServerError()) {
            this.mockMvc.perform(post(API_ACTIVATE_URL + saleId).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is5xxServerError()).andReturn();
        }
        
        if (response != null) {
            result = response.getResponse().getContentAsString();
        }
        
        return result;
    }
    
    @Test
//    @EnabledIfSystemProperty(named = "digitoll-env", matches = "prod")
    public void testRegisterVignetteSuccess() throws Exception {
        
        String authToken;
        LocalDateTime activationDate = LocalDateTime.now().plusDays(4);
        String lpn;
        TestUserPartnerPos testUserPartnerPos = null;
           
        HttpStatus returnStatus = HttpStatus.OK;
        ObjectMapper mapper;
        String jsonResult;
        SaleDTO saleDtoResponse;
        String saleId;
        List<SaleRowRequest> saleRows = new ArrayList<>();
        
        log.info("testRegisterVignetteSuccess");
        
        lpn = generateValidRandomLpn();

        testUserPartnerPos = this.createTestUserPartnerPosWithC2();
        authToken = this.doAuth(); 

        SaleRowRequest sr = new SaleRowRequest();
        sr.setActivationDate(activationDate);
        sr.setEmail(orderEmail);
        sr.setKapschProductId(weeklyCarVignetteKapschId);
        sr.setVehicle(new Vehicle.KapschVehicle(lpn, "BG"));
        saleRows.add(sr);            

        jsonResult = this.registerVignettes(authToken, saleRows, testUserPartnerPos.getPos().getId(), activationDate, testUserPartnerPos.getUser().getId(), returnStatus); 

        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        saleDtoResponse = mapper.readValue(jsonResult, SaleDTO.class);
        saleId = saleDtoResponse.getId();
        assertNotNull(saleId);
        log.info("saleId: " + saleId);
    }
    
    @Test
//    @EnabledIfSystemProperty(named = "digitoll-env", matches = "dev")
    public void testSinglePurchaseSuccess() throws Exception {
        
        String authToken;
        TestUserPartnerPos testUserPartnerPos = null;
        LocalDateTime activationDate = LocalDateTime.now().plusDays(4);
        String lpn;
        List<SaleRowRequest> saleRows = new ArrayList<>();

        List<SaleRowDTO> vignetteRegistrationResponse;
        int vignetteStatus;         
        HttpStatus returnStatus = HttpStatus.OK;
        ObjectMapper mapper;
        SaleDTO saleDtoResponse;
        String activationResponse;
        String saleId;
        
        String jsonResult;
        
        log.info("testSinglePurchaseSuccess");
        
        testUserPartnerPos = this.createTestUserPartnerPosWithC2();
        authToken = this.doAuth(); 

        lpn = generateValidRandomLpn();

        SaleRowRequest sr = new SaleRowRequest();
        sr.setActivationDate(activationDate);
        sr.setEmail(orderEmail);
        sr.setKapschProductId(weeklyCarVignetteKapschId);
        sr.setVehicle(new Vehicle.KapschVehicle(lpn, "BG"));
        saleRows.add(sr);                 

        jsonResult = this.registerVignettes(authToken, saleRows, testUserPartnerPos.getPos().getId(), activationDate, testUserPartnerPos.getUser().getId(), returnStatus);             

        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        saleDtoResponse = mapper.readValue(jsonResult, SaleDTO.class);
        vignetteRegistrationResponse = saleDtoResponse.getSaleRows();              

        if (vignetteRegistrationResponse.isEmpty()) {
            fail("no response content from server");
        }

        vignetteStatus = vignetteRegistrationResponse.get(0).getKapschProperties().getStatus();
        assertThat(vignetteStatus).isEqualTo(VIGNETTE_STATUS_INACTIVE);
        saleId = vignetteRegistrationResponse.get(0).getSaleId();

        activationResponse = this.activateVignette(authToken, saleId, HttpStatus.OK);

        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        SaleDTO saleResp = mapper.readValue(activationResponse, SaleDTO.class);
        SaleRowDTO saleRowDTO = saleResp.getSaleRows().get(0);
        checkResultValidity(saleRowDTO);
    }
    
    
    @Test
//    @EnabledIfSystemProperty(named = "digitoll-env", matches = "dev")
    public void testMultiplePurchaseSuccess() throws Exception {
        
        String authToken;
        TestUserPartnerPos testUserPartnerPos = null;
        LocalDateTime activationDate = LocalDateTime.now().plusDays(4);
        String lpn;
        List<SaleRowRequest> saleRows = new ArrayList<>();

        List<SaleRowDTO> vignetteRegistrationResponse;
        int vignetteStatus;         
        HttpStatus returnStatus = HttpStatus.OK;
        ObjectMapper mapper;
        SaleDTO saleDtoResponse;
        String activationResponse;
        String saleId;
        SaleRowRequest sr;
        
        String jsonResult;
        
        log.info("testMultiplePurchaseSuccess");
        
        testUserPartnerPos = this.createTestUserPartnerPosWithC2();
        authToken = this.doAuth(); 

        lpn = generateValidRandomLpn();

        sr = new SaleRowRequest();
        sr.setActivationDate(activationDate);
        sr.setEmail(orderEmail);
        sr.setKapschProductId(weeklyCarVignetteKapschId);
        sr.setVehicle(new Vehicle.KapschVehicle(lpn, "BG"));
        saleRows.add(sr);   

        lpn = generateValidRandomLpn();
        sr = new SaleRowRequest();
        sr.setActivationDate(activationDate);
        sr.setEmail(orderEmail);
        sr.setKapschProductId(monthlyCarVignetteKapschId);
        sr.setVehicle(new Vehicle.KapschVehicle(lpn, "BG"));
        saleRows.add(sr);               

        jsonResult = this.registerVignettes(authToken, saleRows, testUserPartnerPos.getPos().getId(), activationDate, testUserPartnerPos.getUser().getId(), returnStatus);             

        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        saleDtoResponse = mapper.readValue(jsonResult, SaleDTO.class);
        vignetteRegistrationResponse = saleDtoResponse.getSaleRows();              

        if (vignetteRegistrationResponse.isEmpty()) {
            fail("no response content from server");
        }

        vignetteStatus = vignetteRegistrationResponse.get(0).getKapschProperties().getStatus();
        assertThat(vignetteStatus).isEqualTo(VIGNETTE_STATUS_INACTIVE);
        saleId = vignetteRegistrationResponse.get(0).getSaleId();

        activationResponse = this.activateVignette(authToken, saleId, HttpStatus.OK);

        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        SaleDTO saleResp = mapper.readValue(activationResponse, SaleDTO.class);

        for (SaleRowDTO saleRowDTO: saleResp.getSaleRows()) {
            checkResultValidity(saleRowDTO);
        }
    }    
    
    private void checkResultValidity(SaleRowDTO resp) {
        
        int vignetteStatus;      

        vignetteStatus = resp.getKapschProperties().getStatus();

        //Assert that purchaseDateTimeUTC is properly set
        Date purchaseDate = resp.getKapschProperties().getPurchase().getPurchaseDateTimeUTC();
        LocalDateTime loc = purchaseDate.toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime();
        assertTrue(loc.isAfter(loc.minusMinutes(1)) && loc.isBefore(loc.plusMinutes(1)));

        assertThat(vignetteStatus).isEqualTo(VIGNETTE_STATUS_ACTIVE); 

        assertNotNull(resp.getValidityStartDate());
        assertNotNull(resp.getValidityEndDate());
        assertTrue(resp.getLpn().length() > 3);
        assertNotNull(resp.getVignetteId());
        assertNotNull(resp.getSaleId());
        assertNotNull(resp.getUserId());

        assertEquals(resp.getEmail(), orderEmail);
        assertEquals(resp.getUserName(), userEmail + testDbEntrySuffix);
        assertNotNull(resp.getValidityType());
        assertTrue(resp.isActive());
        assertNotNull(resp.getCreatedOn());
        assertNotNull(resp.getPosId());
        assertNotNull(resp.getPosName());
        assertNotNull(resp.getPartnerId());
        assertNotNull(resp.getPartnerName());        
    }
    
    @Test
    public void testAuthFailure() throws Exception {
        
        AuthenticationRequest payload = new AuthenticationRequest();
        String responseContent;
        String responseStatus;
        String responseMessage;
        
        log.info("testAuthFailure");
        
        payload.setUserName("some name");
        payload.setPassword("some password");
        
        String jsonBody = new ObjectMapper().writeValueAsString(payload);

        MvcResult results = this.mockMvc.perform(post(API_AUTH_URL)
                .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody)).andExpect(status().is(HttpStatus.UNAUTHORIZED.value())).andReturn();        
        
        responseContent = results.getResponse().getContentAsString();
        
        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseJson = mapper.readTree(responseContent);
        
        responseStatus = responseJson.get("status").asText();
        //assertThat(responseStatus).isEqualTo(RESPONSE_INTERNAL_SERVER_ERROR);
        assertThat(responseStatus).isEqualTo("UNAUTHORIZED");
        
        responseMessage = responseJson.get("message").asText();
        assertThat(responseMessage).isEqualTo(RESPONSE_MESSAGE_BAD_CREDENTIALS);
    }
    
    @Test
    public void testRegisterPastDateFailure() throws Exception {
        
        String authToken;
        TestUserPartnerPos testUserPartnerPos = null;
        LocalDateTime activationDate = LocalDateTime.now().minusDays(6);
        String lpn;
        Random r = new Random();
        HttpStatus returnStatus = HttpStatus.BAD_REQUEST;
        List<SaleRowRequest> saleRows = new ArrayList<>();
        
        log.info("testRegisterPastDateFailure");
        
        lpn = "CM" + String.valueOf(r.nextInt((9999 - 1111) + 1) + 1111) + "PP";

        SaleRowRequest sr = new SaleRowRequest();
        sr.setActivationDate(activationDate);
        sr.setEmail(orderEmail);
        sr.setKapschProductId(weeklyCarVignetteKapschId);
        sr.setVehicle(new Vehicle.KapschVehicle(lpn, "BG"));
        saleRows.add(sr);                

        testUserPartnerPos = this.createTestUserPartnerPosWithC2();
        authToken = this.doAuth(); 

        this.registerVignettes(authToken, saleRows, testUserPartnerPos.getPos().getId(), activationDate, testUserPartnerPos.getUser().getId(), returnStatus);        
    }
    
    @Test
    public void testRegisterFutureDateFailure() throws Exception {
        
        String authToken;
        TestUserPartnerPos testUserPartnerPos = null;
        LocalDateTime activationDate = LocalDateTime.now().plusMonths(2);
        String lpn;
        HttpStatus returnStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        List<SaleRowRequest> saleRows = new ArrayList<>();
        
        log.info("testRegisterFutureDateFailure");
        
        lpn = generateValidRandomLpn();

        testUserPartnerPos = this.createTestUserPartnerPosWithC2();
        authToken = this.doAuth(); 

        SaleRowRequest sr = new SaleRowRequest();
        sr.setActivationDate(activationDate);
        sr.setEmail(orderEmail);
        sr.setKapschProductId(weeklyCarVignetteKapschId);
        sr.setVehicle(new Vehicle.KapschVehicle(lpn, "BG"));
        saleRows.add(sr);            

        this.registerVignettes(authToken, saleRows, testUserPartnerPos.getPos().getId(), activationDate, testUserPartnerPos.getUser().getId(), returnStatus);      
    }    
    
    @Test
    public void testRegisterInvalidWeekendVignetteDateFailure() throws Exception {
        
        String authToken;
        TestUserPartnerPos testUserPartnerPos = null;
        LocalDateTime activationDate = LocalDateTime.now().plusHours(1);
        String lpn;
        int weekendCarVignetteKapschId = 105;   
        HttpStatus returnStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        List<SaleRowRequest> saleRows = new ArrayList<>();
        
        log.info("testRegisterInvalidWeekendVignetteDateFailure");
        
        lpn = generateValidRandomLpn();

        if (activationDate.getDayOfWeek() == DayOfWeek.FRIDAY || 
                activationDate.getDayOfWeek() == DayOfWeek.SATURDAY ||
                    activationDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            activationDate = activationDate.plusDays(3);
        }

        testUserPartnerPos = this.createTestUserPartnerPosWithC2();
        authToken = this.doAuth(); 

        SaleRowRequest sr = new SaleRowRequest();
        sr.setActivationDate(activationDate);
        sr.setEmail(orderEmail);
        sr.setKapschProductId(weekendCarVignetteKapschId);
        sr.setVehicle(new Vehicle.KapschVehicle(lpn, "BG"));
        saleRows.add(sr);             

        this.registerVignettes(authToken, saleRows, testUserPartnerPos.getPos().getId(), activationDate, testUserPartnerPos.getUser().getId(), returnStatus);      
    }        
    
    @Test
    public void testRegisterInvalidLpnFailure() throws Exception {
        
        String authToken;
        TestUserPartnerPos testUserPartnerPos = null;
        LocalDateTime activationDate = LocalDateTime.now().plusDays(6);
        String lpn;
        HttpStatus returnStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        List<SaleRowRequest> saleRows = new ArrayList<>();
        
        log.info("testRegisterInvalidLpnFailure");
        
        lpn = "AAAAAAAAAAAAAAAAAAAA";
            
        testUserPartnerPos = this.createTestUserPartnerPosWithC2();
        authToken = this.doAuth(); 

        SaleRowRequest sr = new SaleRowRequest();
        sr.setActivationDate(activationDate);
        sr.setEmail(orderEmail);
        sr.setKapschProductId(weeklyCarVignetteKapschId);
        sr.setVehicle(new Vehicle.KapschVehicle(lpn, "BG"));
        saleRows.add(sr);             

        this.registerVignettes(authToken, saleRows, testUserPartnerPos.getPos().getId(), activationDate, testUserPartnerPos.getUser().getId(), returnStatus);        
    }
    
    @Test
    public void testRegisterAccessDenied() throws Exception {
        
        String authToken;
        TestUserPartnerPos testUserPartnerPos = null;
        LocalDateTime activationDate = LocalDateTime.now().plusDays(4);
        String lpn;
        HttpStatus returnStatus = HttpStatus.FORBIDDEN;
        List<SaleRowRequest> saleRows = new ArrayList<>();
        
        log.info("testRegisterAccessDenied");
        
        lpn = generateValidRandomLpn();
        testUserPartnerPos = this.createTestUserPartnerPosWithNoPurchaseAccess();
        authToken = this.doAuth(); 

        SaleRowRequest sr = new SaleRowRequest();
        sr.setActivationDate(activationDate);
        sr.setEmail(orderEmail);
        sr.setKapschProductId(weeklyCarVignetteKapschId);
        sr.setVehicle(new Vehicle.KapschVehicle(lpn, "BG"));
        saleRows.add(sr);              

        this.registerVignettes(authToken, saleRows, testUserPartnerPos.getPos().getId(), activationDate, testUserPartnerPos.getUser().getId(), returnStatus); 
    }
    
    @Test
//    @EnabledIfSystemProperty(named = "digitoll-env", matches = "dev")
    public void testSecondActivationAttemptFailure() throws Exception {
        
        String authToken;
        String saleId;
        TestUserPartnerPos testUserPartnerPos = null;
        LocalDateTime activationDate = LocalDateTime.now().plusDays(4);
        String lpn;
        List<SaleRowDTO> vignetteRegistrationResponse;
        int vignetteStatus;         
        HttpStatus returnStatus = HttpStatus.OK;
        ObjectMapper mapper;
        SaleDTO saleDtoResponse;
        String activationResponse;
        List<SaleRowRequest> saleRows = new ArrayList<>();
        
        String jsonResult;
        
        log.info("testSecondActivationAttemptFailure");
        
        testUserPartnerPos = this.createTestUserPartnerPosWithC2();
        authToken = this.doAuth(); 

        lpn = generateValidRandomLpn();    

        SaleRowRequest sr = new SaleRowRequest();
        sr.setActivationDate(activationDate);
        sr.setEmail(orderEmail);
        sr.setKapschProductId(weeklyCarVignetteKapschId);
        sr.setVehicle(new Vehicle.KapschVehicle(lpn, "BG"));
        saleRows.add(sr);                 

        jsonResult = this.registerVignettes(authToken, saleRows, testUserPartnerPos.getPos().getId(), activationDate, testUserPartnerPos.getUser().getId(), returnStatus); 

        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        saleDtoResponse = mapper.readValue(jsonResult, SaleDTO.class);
        vignetteRegistrationResponse = saleDtoResponse.getSaleRows();              

        if (vignetteRegistrationResponse.isEmpty()) {
            fail("invalid response from server");
        }

        vignetteStatus = vignetteRegistrationResponse.get(0).getKapschProperties().getStatus();
        assertThat(vignetteStatus).isEqualTo(VIGNETTE_STATUS_INACTIVE);
        saleId = vignetteRegistrationResponse.get(0).getSaleId();

        activationResponse = this.activateVignette(authToken, saleId, HttpStatus.OK);

        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);;

        SaleDTO resp = mapper.readValue(activationResponse, SaleDTO.class);

        if (resp.getSaleRows().isEmpty()) {
            fail("invalid response from server");
        }
        vignetteStatus = resp.getSaleRows().get(0).getKapschProperties().getStatus();
        assertThat(vignetteStatus).isEqualTo(VIGNETTE_STATUS_ACTIVE);

        //Do a second activation attempt
        this.activateVignette(authToken, saleId, HttpStatus.INTERNAL_SERVER_ERROR);
    }
        
    @Test
    public void testActivateNonExistingSaleIdFailure() throws Exception {
        
        String authToken;
        TestUserPartnerPos testUserPartnerPos = null;      
        String saleId = "11111111111";
        
        log.info("testActivateNonExistingSaleIdFailure");

        testUserPartnerPos = this.createTestUserPartnerPosWithC2();
        authToken = this.doAuth(); 

        this.activateVignette(authToken, saleId, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    public class TestUserPartnerPos {
        private Pos pos;
        private Partner partner;
        private User user;

        public Pos getPos() {
            return pos;
        }

        public void setPos(Pos pos) {
            this.pos = pos;
        }

        public Partner getPartner() {
            return partner;
        }

        public void setPartner(Partner partner) {
            this.partner = partner;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public TestUserPartnerPos(Pos pos, Partner partner, User user) {
            this.pos = pos;
            this.partner = partner;
            this.user = user;
        }
    }
}
