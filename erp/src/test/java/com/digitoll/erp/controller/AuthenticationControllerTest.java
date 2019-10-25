package com.digitoll.erp.controller;

import com.digitoll.commons.request.AuthenticationRequest;
import com.digitoll.commons.response.AuthenticationResponse;
import com.digitoll.commons.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest( secure = false)
@ContextConfiguration(classes = {
        AuthenticationController.class
})
public class AuthenticationControllerTest {
    @MockBean
    private AuthenticationService authenticationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String TOKEN = "token";

    @Test
    public void testAuthenticate() throws Exception {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setUserName(USERNAME);
        authenticationRequest.setPassword(PASSWORD);
        AuthenticationResponse expectedResponse = new AuthenticationResponse();
        expectedResponse.setToken(TOKEN);
        when(authenticationService.authenticate(USERNAME, PASSWORD)).thenReturn(expectedResponse);
        mvc.perform(post("/user/authenticate").content(objectMapper.writeValueAsString(authenticationRequest))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.token").value(TOKEN));
    }

    @Test
    public void testAuthenticateStatusIsBadRequest() throws Exception {

        mvc.perform(post("/user/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @Ignore
    public void testFailedAuthentication() {
        // ToDO: implement after failed login error is no longer 500
    }
}