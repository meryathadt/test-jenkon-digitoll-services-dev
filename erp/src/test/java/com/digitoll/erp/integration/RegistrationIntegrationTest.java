package com.digitoll.erp.integration;

import com.digitoll.commons.dto.UserDetailsDTO;
import com.digitoll.commons.model.User;
import com.digitoll.commons.request.AuthenticationRequest;
import com.digitoll.erp.utils.ErpTestHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@RunWith(SpringRunner.class)
@SpringBootTest
//@SpringBootConfiguration
@AutoConfigureMockMvc

@TestPropertySource(
        locations = "classpath:integrationtest.properties")

public class RegistrationIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(VignettePurchaseIntegrationTest.class);
    private final String API_REGISTER_URL = "/user/register";
    private final String API_AUTH_URL = "/user/authenticate";
    private final String API_DELETE_URL = "/user/delete/";
    private final String API_USER_URL = "/user/";

    @Autowired
    private MockMvc mockMvc;

    private Long testDbEntrySuffix = System.currentTimeMillis();

    private String USER_NAME=testDbEntrySuffix+"@mailinator.com";
    private String USER_FIRSTNAME="IVAN";
    private String USER_LASTNAME="PETROV";
    private String USER_NAME_SECOND=testDbEntrySuffix+"@dve.com";
    private String SECOND_USER_FIRSTNAME="SECONDIVAN";
    private String SECOND_USER_LASTNAME="SECONDPETROV";
    private String PASSWORD = "123456aA";
    private String ADMIN_USERNAME = "aaaa1@abv.bg";
    private ErpTestHelper erpTestHelper =new ErpTestHelper();
    private ObjectMapper  objectMapper = new ObjectMapper();

    private String doAuth() throws Exception {

        AuthenticationRequest payload = new AuthenticationRequest();
        payload.setUserName(ADMIN_USERNAME);
        payload.setPassword(PASSWORD);
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

    @Test
    public void createUserWithUsedID() throws Exception {

        String token;

        log.info("createUserWithUsedID");

        token = doAuth();



        User newUser = erpTestHelper.createUser( USER_NAME,USER_FIRSTNAME,USER_LASTNAME,PASSWORD,null);

        MvcResult resultReg = this.mockMvc.perform(post(API_REGISTER_URL)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser))).andReturn();

        String responseString = resultReg.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        UserDetailsDTO responseUser = mapper.readValue(responseString, UserDetailsDTO.class);
        String newUserID = responseUser.getId();

        validateUser(responseUser, USER_NAME, USER_FIRSTNAME, USER_LASTNAME);


        User newUser1 = erpTestHelper.createUser(USER_NAME_SECOND,SECOND_USER_FIRSTNAME,SECOND_USER_LASTNAME,PASSWORD,newUserID);


        MvcResult resultRegSecond = this.mockMvc.perform(post(API_REGISTER_URL)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser1))).andReturn();

        String responseStringSecond = resultRegSecond.getResponse().getContentAsString();

        UserDetailsDTO userPostSecond = mapper.readValue(responseStringSecond, UserDetailsDTO.class);
        String newUserID1 = userPostSecond.getId();
        validateUser(userPostSecond, USER_NAME_SECOND, SECOND_USER_FIRSTNAME, SECOND_USER_LASTNAME);
        assertNotEquals(newUserID1,newUserID);

        MvcResult getUserDetails = this.mockMvc.perform(get(API_USER_URL + newUserID)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseUserDetailsString = getUserDetails.getResponse().getContentAsString();
        User userFirst = mapper.readValue(responseUserDetailsString, User.class);
        validateUser(userFirst, USER_NAME, USER_FIRSTNAME, USER_LASTNAME);


        MvcResult getUser1Details = this.mockMvc.perform(get(API_USER_URL + newUserID1)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseUserDetails1String = getUser1Details.getResponse().getContentAsString();

        User userGetSecond = mapper.readValue(responseUserDetails1String, User.class);
        validateUser(userGetSecond,USER_NAME_SECOND, SECOND_USER_FIRSTNAME, SECOND_USER_LASTNAME);


        this.mockMvc.perform(delete(API_DELETE_URL + newUserID)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        this.mockMvc.perform(delete(API_DELETE_URL + newUserID1)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    private void validateUser(User user, String expectedUserName, String expectedFirstName, String expectedLastName) {
        assertEquals(user.getUsername(), expectedUserName);
        assertEquals(user.getFirstName(), expectedFirstName);
        assertEquals(user.getLastName(), expectedLastName);
    }

    private void validateUser(UserDetailsDTO user, String expectedUserName, String expectedFirstName, String expectedLastName) {
        assertEquals(user.getUsername(), expectedUserName);
        assertEquals(user.getFirstName(), expectedFirstName);
        assertEquals(user.getLastName(), expectedLastName);
    }

}
