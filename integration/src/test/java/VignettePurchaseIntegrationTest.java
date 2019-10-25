import com.digitoll.commons.model.DigitollUser;
import com.digitoll.commons.model.DigitollUserDetails;
import com.digitoll.commons.model.Role;
import com.digitoll.commons.model.User;
import com.digitoll.commons.model.Vehicle;
import com.digitoll.commons.request.AuthenticationRequest;
import com.digitoll.commons.response.AuthenticationResponse;
import com.digitoll.commons.response.SaleDTO;
import com.digitoll.commons.response.SaleRowDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import config.TestConfig;
import io.restassured.response.ValidatableResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.platform.commons.util.StringUtils;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class VignettePurchaseIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(VignettePurchaseIntegrationTest.class);

    final int VIGNETTE_STATUS_INACTIVE = 1;
    final int VIGNETTE_STATUS_ACTIVE = 2;

    final int weeklyCarVignetteKapschId = 101;
    final int monthlyCarVignetteKapschId = 102;

    @Value("${itest.userEmail}")
    private String test;

    @Value("${api.login.url}")
    private String API_LOGIN_URL;

    @Value("${api.auth.url}")
    private String API_AUTH_URL;

    @Value("${api.purchase.url}")
    private String API_PURCHASE_URL;

    @Value("${api.register.url}")
    private String API_REGISTER_URL;

    @Value("${api.get.sale.url}")
    private String API_GET_SALE_URL;

    @Value("${itest.user.password}")
    private String userPassword;

    @Value("${itest.user.names}")
    private String names;

    @Value("${itest.order.email}")
    private String orderEmail;

    @Value("${itest.user.role.id}")
    private String userRoleId;

    @Value("${itest.user.role.code}")
    private String userRoleCode;

    @Value("${itest.user.role.name}")
    private String userRoleName;

    @Value("${web.site.partner.id}")
    private String websitePartnerId;

    @Value("${web.site.pos.id}")
    private String websitePosId;

    @Value("${web.site.user.id}")
    private String websiteUserId;

    @Value("${itest.wrong.user.name}")
    private String wrongUserName;

    @Value("${itest.wrong.user.password}")
    private String wrongUserPassword;

    @Value("${itest.auth.fail.mesasge}")
    private String failMessage;

    @Value("${itest.kapsch.invalid.weekend.status}")
    private String invalidCode;

    @Value("${itest.kapsch.invalid.weekend.message}")
    private String invalidMessage;

    private RestTemplate restTemplate = new RestTemplate();

    @Before
    public void init(){

    }

    private void deleteTestUser(){

    }

    private void createTestUser() {

        DigitollUser user = new DigitollUser();
        Role role = new Role();
        role.setId(userRoleId);
        role.setCode(userRoleCode);
        role.setName(userRoleName);

        List<Role> roleList = new ArrayList<>();
        roleList.add(role);
        Set<Role> roleSet = new HashSet<Role>(roleList);
        user.setUsername(orderEmail);
        user.setPassword(userPassword);
        user.setNames(names);
        user.setRoles(roleSet);
        HttpEntity<DigitollUser> request = new HttpEntity<>(user);
        User response = restTemplate.postForObject(API_REGISTER_URL, request, User.class);
    }
//
    private String generateValidRandomLpn() {
        String lpn;
        Random random = new Random();

        lpn = "CM" + String.valueOf(random.nextInt((9999 - 1111) + 1) + 1111) + "PP";
        return lpn;
    }

    private AuthenticationResponse getToken(){
        AuthenticationRequest payload = new AuthenticationRequest();
        payload.setUserName(orderEmail);
        payload.setPassword(userPassword);

        HttpEntity<AuthenticationRequest> request = new HttpEntity<>(payload);
        AuthenticationResponse response = restTemplate.postForObject(API_AUTH_URL, request, AuthenticationResponse.class);
        return response;
    }

    private String doAuth() throws Exception {
        AuthenticationResponse response = getToken();

        if (response == null || response.getToken() == null){
            try {
                createTestUser();
                response = getToken();
            } catch (HttpServerErrorException e) {
                //user already exists
            }
        }

        return response.getToken();
    }

    private String registerVignettes(
            String token,
            List<SaleRowDTO> saleRows,
            LocalDateTime activationDate,
            HttpStatus expectedStatus) throws Exception {
        return registerVignettes( token,
                saleRows,
                activationDate,
                expectedStatus, null, null );
    }

    private String registerVignettes(
            String token,
            List<SaleRowDTO> saleRows,
            LocalDateTime activationDate,
            HttpStatus expectedStatus,
            String errorCode,
            String errorMesasge
    ) throws Exception {

        String result = null;
        ObjectMapper mapper = new ObjectMapper();

        SaleDTO saleRequest = new SaleDTO();
        String bankTransId = "testBankTrans"+ UUID.randomUUID().toString();
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setBearerAuth(token);
        HttpEntity<Object> request = new HttpEntity<>(requestHeaders);
        DigitollUserDetails userDetails = restTemplate.postForObject(API_LOGIN_URL, request, DigitollUserDetails.class);

        saleRequest.setSaleRows(saleRows);
        saleRequest.setUserId(userDetails.getId());
        saleRequest.setBankTransactionId(bankTransId);

        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);



        requestHeaders = new HttpHeaders();
        requestHeaders.setBearerAuth(token);
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        SaleDTO saleDTO = null;
        switch (expectedStatus){
            case OK:
                given()
                        .urlEncodingEnabled(true)
                        .headers(requestHeaders)
                        .body(saleRequest)
                        .post(API_PURCHASE_URL)
                        .then().statusCode(200)
                        .extract();

                saleDTO = restTemplate.getForObject(API_GET_SALE_URL + "?trans_id=" + bankTransId, SaleDTO.class);
                assertEquals(saleDTO.getPartnerId(),websitePartnerId);
                assertEquals(saleDTO.getPosId(),websitePosId);
                assertEquals(saleDTO.getUserId(),websiteUserId);
                assertNotNull(saleDTO.getSaleRows());
                assertThat(saleDTO.getSaleRows().size()).isGreaterThan(0);
                assertEquals(saleDTO.getSaleRows().get(0).getPartnerId(),websitePartnerId);
                assertEquals(saleDTO.getSaleRows().get(0).getPosId(),websitePosId);
                assertEquals(saleDTO.getSaleRows().get(0).getUserId(),websiteUserId);
                assertEquals(saleDTO.getSaleRows().get(0).getEmail(),orderEmail);
                assertNotNull(saleDTO.getSaleRows().get(0).getKapschProductId());
                assertNotNull(saleDTO.getSaleRows().get(0).getKapschProperties());
                // Add this code ( remove // ) when code with cache is deployed / a jenkins job for Integration Tests
                // is added
//                assertNotNull(saleDTO.getSaleRows().get(0).getPartner());
//                assertNotNull(saleDTO.getSaleRows().get(0).getUser());
//                assertNotNull(saleDTO.getSaleRows().get(0).getPos());
//                assertEquals(saleDTO.getSaleRows().get(0).getPartner().getId(),websitePartnerId);
//                assertEquals(saleDTO.getSaleRows().get(0).getUser().getId(),websiteUserId);
//                assertEquals(saleDTO.getSaleRows().get(0).getPos().getId(),websitePosId);
//                assertNotNull(saleDTO.getSaleRows().get(0).getPartner().getName());
//                assertNotNull(saleDTO.getSaleRows().get(0).getUser().getUsername());
//                assertNotNull(saleDTO.getSaleRows().get(0).getPos().getName());
//                assertNotNull(saleDTO.getSaleRows().get(0).getSale());
//                assertEquals(saleDTO.getSaleRows().get(0).getSale().getUserId(),websiteUserId);
                break;
            case INTERNAL_SERVER_ERROR:
//            {"status":"INTERNAL_SERVER_ERROR","code":500,"message":"500 ","errors":[{"code":"202","message":"Invalid Requested Date for Weekend Vignettes"}]}
                ValidatableResponse resp = given()
                        .urlEncodingEnabled(true)
                        .headers(requestHeaders)
                        .body(saleRequest)
                        .post(API_PURCHASE_URL)
                        .then().statusCode(500);
                        if(!StringUtils.isBlank(errorCode) && !StringUtils.isBlank(errorMesasge)){
                            resp.assertThat()
                                    .body("errors[0].code", equalTo(errorCode))
                                    .body("errors[0].message", equalTo(errorMesasge));
                        }

                break;
            default:
        }

        return result;
    }

    private Vehicle getVehicle(String lpn){
        Vehicle vehicle = new Vehicle();
        vehicle.setLpn(lpn);
        vehicle.setCountryCode("BG");
        return vehicle;
    }


    //
    @Test
//    @EnabledIfSystemProperty(named = "digitoll-env", matches = "dev")
    public void testRegisterVignetteSuccess() throws Exception {

        String authToken;
        LocalDateTime activationDate = LocalDateTime.now().plusDays(4);
        String lpn;

        HttpStatus returnStatus = HttpStatus.OK;

        List<SaleRowDTO> saleRows = new ArrayList<>();

        log.info("testRegisterVignetteSuccess");

        lpn = generateValidRandomLpn();

        authToken = doAuth();

        SaleRowDTO sr = new SaleRowDTO();
        sr.setActivationDate(activationDate);
        sr.setEmail(orderEmail);
        sr.setKapschProductId(weeklyCarVignetteKapschId);

        sr.setVehicle(getVehicle(lpn));
        saleRows.add(sr);
//
        registerVignettes(authToken, saleRows, activationDate,  returnStatus);
    }



    @Test
    public void testAuthFailure() throws Exception {

        AuthenticationRequest payload = new AuthenticationRequest();

        log.info("testAuthFailure");
        payload.setUserName(wrongUserName);
        payload.setPassword(wrongUserPassword);


        HttpEntity<AuthenticationRequest> request = new HttpEntity<>(payload);

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        // do we want this to be 500 ?

        given()
                .urlEncodingEnabled(true)
                .headers(requestHeaders)
                .body(request)
                .post(API_AUTH_URL)
                .then().statusCode(401)
                .assertThat().body("message", equalTo(failMessage));

    }

    @Test
    public void testRegisterInvalidWeekendVignetteDateFailure() throws Exception {

        String authToken;
        LocalDateTime activationDate = LocalDateTime.now().plusHours(1);
        String lpn;
        int weekendCarVignetteKapschId = 105;
        HttpStatus returnStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        List<SaleRowDTO> saleRows = new ArrayList<>();

        log.info("testRegisterInvalidWeekendVignetteDateFailure");

        lpn = generateValidRandomLpn();

        if (activationDate.getDayOfWeek() == DayOfWeek.FRIDAY ||
                activationDate.getDayOfWeek() == DayOfWeek.SATURDAY ||
                activationDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            activationDate = activationDate.plusDays(3);
        }
        authToken = this.doAuth();

        SaleRowDTO sr = new SaleRowDTO();
        sr.setActivationDate(activationDate);
        sr.setEmail(orderEmail);
        sr.setKapschProductId(weekendCarVignetteKapschId);
        sr.setVehicle(getVehicle(lpn));
        saleRows.add(sr);

        this.registerVignettes(authToken, saleRows, activationDate, returnStatus, invalidCode, invalidMessage);
    }

    @Test
    public void testRegisterInvalidLpnFailure() throws Exception {

        String authToken;
        LocalDateTime activationDate = LocalDateTime.now().plusDays(6);
        String lpn;
        HttpStatus returnStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        List<SaleRowDTO> saleRows = new ArrayList<>();

        log.info("testRegisterInvalidLpnFailure");

        lpn = "AAAAAAAAAAAAAAAAAAAAEBATIGRESHNIQNOMEEERRR";

        authToken = this.doAuth();

        SaleRowDTO sr = new SaleRowDTO();
        sr.setActivationDate(activationDate);
        sr.setEmail(orderEmail);
        sr.setKapschProductId(weeklyCarVignetteKapschId);
        sr.setVehicle(getVehicle(lpn));
        saleRows.add(sr);

        this.registerVignettes(authToken, saleRows, activationDate,  returnStatus);
    }
}

