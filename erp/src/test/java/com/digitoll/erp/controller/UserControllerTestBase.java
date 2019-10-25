package com.digitoll.erp.controller;

import com.digitoll.erp.service.UserService;
import com.digitoll.erp.utils.ErpTestHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.security.Principal;
import java.text.SimpleDateFormat;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static com.digitoll.erp.utils.ErpTestHelper.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerTestBase {
    protected static final String USERNAME = "username@mail.bg";
    protected static final String PASSWORD = "Password1234";
    protected static final String CODE = "code";
    protected static final String NAME = "name";

    protected static final boolean ACTIVE = true;
    protected static final String FIRST_NAME = "firstName";
    protected static final String LAST_NAME = "lastName";

    protected static final String USER_ID = "userId";
    protected static final String POS_ID = "posId";
    protected static final String PRINCIPAL_NAME = "principalName";

    protected static final String PARTNER_ID = "partnerId";

    protected static final String POS_ID2 = "posID2";
    protected static final String LAST_NAME2 = "lastName2";

    protected static final String NEW_PASSWORD = "newPassword1234";

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    protected static final String PARTNER_NAME = "partnerName";
    protected static final String POS_NAME = "posName";

    @MockBean
    protected UserService userService;

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected ErpTestHelper erpTestHelper = new ErpTestHelper();

    @Mock
    protected Principal principal;

    protected void verifyUser(ResultActions resultActions,
                            String lastName,
                            String posId) throws Exception {
        resultActions
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(USER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.username")
                        .value(USERNAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$.active")
                        .value(ACTIVE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt")
                        .value(ErpTestHelper.CREATED_AT))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName")
                        .value(FIRST_NAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName")
                        .value(lastName))
                .andExpect(MockMvcResultMatchers.jsonPath("$.partnerId")
                        .value(PARTNER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.posIds[0]")
                        .value(posId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles", hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[0].name")
                        .value(NAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[0].id")
                        .value(ErpTestHelper.ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[0].createdAt")
                        .value(ErpTestHelper.CREATED_AT))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[0].code")
                        .value(CODE));
    }

    public void verifyUsers(ResultActions resultActions) throws Exception {
        resultActions.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(USER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].username")
                        .value(USERNAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].active")
                        .value(ACTIVE))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].createdAt")
                        .value(ErpTestHelper.CREATED_AT))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].firstName")
                        .value(FIRST_NAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].lastName")
                        .value(LAST_NAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].partnerId")
                        .value(PARTNER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].posIds[0]")
                        .value(POS_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].roles", hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].roles[0].name")
                        .value(NAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].roles[0].id")
                        .value(ErpTestHelper.ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].roles[0].createdAt")
                        .value(ErpTestHelper.CREATED_AT))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].roles[0].code")
                        .value(CODE));
    }
}
