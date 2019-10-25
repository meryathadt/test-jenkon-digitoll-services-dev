package com.digitoll.erp.controller;

import com.digitoll.commons.configuration.CustomAuthenticationEntryPoint;
import com.digitoll.commons.dto.PasswordUpdateAdminDTO;
import com.digitoll.commons.dto.UserDetailsDTO;
import com.digitoll.commons.exception.CustomExceptionHandler;
import com.digitoll.commons.model.User;
import com.digitoll.commons.response.UserDetailsResponse;
import com.digitoll.erp.configuration.SecurityConfig;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static com.digitoll.erp.utils.ErpTestHelper.PRINCIPAL_NAME;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {
        AdminController.class,
        CustomExceptionHandler.class,
        SecurityConfig.class,
        CustomAuthenticationEntryPoint.class
})
@WithMockUser(roles = "ADMIN", username = PRINCIPAL_NAME)
public class AdminControllerTest extends UserControllerTestBase {
    @Test
    public void testUpdateAdminIsBadRequest() throws Exception {
        mvc.perform(post("/user/update/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdatePasswordAdmin() throws Exception {
        PasswordUpdateAdminDTO updateDto = new PasswordUpdateAdminDTO();

        updateDto.setPassword(PASSWORD);
        updateDto.setUserId(USER_ID);

        when(principal.getName()).thenReturn(PRINCIPAL_NAME);

        mvc.perform(post("/user/update/password/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .principal(principal)
                .content(objectMapper.writeValueAsString(updateDto))
                .with(csrf()))
                .andExpect(status().isOk());

        verify(userService).updatePasswordAdmin(updateDto, PRINCIPAL_NAME);
    }

    @Test
    public void testUpdatePasswordAdminPasswordValidationFailed() throws Exception {
        PasswordUpdateAdminDTO updateDto = new PasswordUpdateAdminDTO();

        updateDto.setPassword("password");
        updateDto.setUserId(USER_ID);

        when(principal.getName()).thenReturn(PRINCIPAL_NAME);

        mvc.perform(post("/user/update/password/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .principal(principal)
                .content(objectMapper.writeValueAsString(updateDto))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updatePasswordAdmin(updateDto, PRINCIPAL_NAME);
    }

    @Test
    public void testUpdatePasswordAdminPasswordTooLongValidationFailed() throws Exception {
        PasswordUpdateAdminDTO updateDto = new PasswordUpdateAdminDTO();

        updateDto.setPassword(RandomStringUtils.randomAlphabetic(251));
        updateDto.setUserId(USER_ID);

        when(principal.getName()).thenReturn(PRINCIPAL_NAME);

        mvc.perform(post("/user/update/password/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .principal(principal)
                .content(objectMapper.writeValueAsString(updateDto))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updatePasswordAdmin(updateDto, PRINCIPAL_NAME);
    }

    @Test
    public void testUpdateAdmin() throws Exception {
        when(principal.getName()).thenReturn(PRINCIPAL_NAME);
        UserDetailsDTO oldUser = erpTestHelper.createUserDetailsDTO();
        UserDetailsDTO newUser = erpTestHelper.createUserDetailsDTO();

        List<String> posIdList = new ArrayList<>(1);
        posIdList.add(POS_ID2);

        newUser.setLastName(LAST_NAME2);
        newUser.setPosIds(posIdList);

        when(userService.updateUserAdmin(oldUser, PRINCIPAL_NAME))
                .thenReturn(newUser);

        verifyUser(mvc.perform(post("/user/update/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .principal(principal)
                        .content(objectMapper.writeValueAsString(oldUser))
                        .with(csrf())),
                LAST_NAME2,
                POS_ID2);
    }

    @Test
    public void testUpdateAdminUsernameValidationFailed() throws Exception {
        when(principal.getName()).thenReturn(PRINCIPAL_NAME);
        UserDetailsDTO oldUser = erpTestHelper.createUserDetailsDTO();

        oldUser.setUsername("username..");

        mvc.perform(post("/user/update/admin")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .principal(principal)
            .content(objectMapper.writeValueAsString(oldUser))
            .with(csrf())
        )
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateAdminUsernameTooLongValidationFailed() throws Exception {
        when(principal.getName()).thenReturn(PRINCIPAL_NAME);
        UserDetailsDTO oldUser = erpTestHelper.createUserDetailsDTO();

        oldUser.setUsername(RandomStringUtils.randomAlphabetic(101));

        mvc.perform(post("/user/update/admin")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .principal(principal)
            .content(objectMapper.writeValueAsString(oldUser))
            .with(csrf())
        )
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateAdminUsernameIsEmail() throws Exception {
        when(principal.getName()).thenReturn(PRINCIPAL_NAME);
        UserDetailsDTO oldUser = erpTestHelper.createUserDetailsDTO();
        UserDetailsDTO newUser = erpTestHelper.createUserDetailsDTO();

        oldUser.setUsername("this_is_a_very_long_email_username@test.bg");

        List<String> posIdList = new ArrayList<>(1);
        posIdList.add(POS_ID2);

        newUser.setLastName(LAST_NAME2);
        newUser.setPosIds(posIdList);

        when(userService.updateUserAdmin(oldUser, PRINCIPAL_NAME))
                .thenReturn(newUser);

        mvc.perform(post("/user/update/admin")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .principal(principal)
            .content(objectMapper.writeValueAsString(oldUser))
            .with(csrf())
        )
            .andExpect(status().isOk());
    }

    @Test
    public void testUpdatePasswordAdminIsBadRequest() throws Exception {
        mvc.perform(post("/user/update/password/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
        )
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    public void testRegistration() throws Exception {
        UserDetailsDTO userDetailsDTO = erpTestHelper.createUserDetailsDTO();

        when(principal.getName()).thenReturn(PRINCIPAL_NAME);

        User user = erpTestHelper.createUser();

        when(userService.createUser(user, PRINCIPAL_NAME)).thenReturn(userDetailsDTO);

        when(userService.loadUserByUsername(USERNAME)).thenThrow(new UsernameNotFoundException("User exists"));

        verifyUser(mvc.perform(post("/user/register")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user))
                        .with(csrf())),
                LAST_NAME, POS_ID);
    }

    @Test
    public void testRegistrationUsernameValidationFailed() throws Exception {

        when(principal.getName()).thenReturn(PRINCIPAL_NAME);

        User user = erpTestHelper.createUser();
        user.setUsername("username...");

        mvc.perform(post("/user/register")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user))
                .with(csrf())
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testRegistrationUsernameTooLongValidationFailed() throws Exception {

        when(principal.getName()).thenReturn(PRINCIPAL_NAME);

        User user = erpTestHelper.createUser();
        user.setUsername(RandomStringUtils.randomAlphabetic(101));

        mvc.perform(post("/user/register")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user))
                .with(csrf())
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testRegistrationPasswordValidationFailed() throws Exception {

        when(principal.getName()).thenReturn(PRINCIPAL_NAME);

        User user = erpTestHelper.createUser();
        user.setPassword("password");

        mvc.perform(post("/user/register")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user))
                .with(csrf())
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testRegistrationPasswordTooLongValidationFailed() throws Exception {

        when(principal.getName()).thenReturn(PRINCIPAL_NAME);

        User user = erpTestHelper.createUser();
        user.setPassword(RandomStringUtils.randomAlphabetic(251));

        mvc.perform(post("/user/register")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user))
                .with(csrf())
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testFailedRegistration() throws Exception {
        when(principal.getName()).thenReturn(PRINCIPAL_NAME);

        User user = erpTestHelper.createUser();

        mvc.perform(post("/user/register")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user))
                .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    public void testRegisterIsBadRequest() throws Exception {
        mvc.perform(post("/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
        )
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    public void testDelete() throws Exception {
        when(principal.getName()).thenReturn(PRINCIPAL_NAME);

        mvc.perform(delete("/user/delete/"+USER_ID)
                .principal(principal)
                .with(csrf()))
                .andExpect(status().isOk());

        verify(userService).deleteUser(USER_ID, PRINCIPAL_NAME);
    }

    @Test
    public void testDecommission() throws Exception {
        UserDetailsDTO userDetailsDTO = erpTestHelper.createUserDetailsDTO();

        when(principal.getName()).thenReturn(PRINCIPAL_NAME);

        when(userService.decommissionUser(USER_ID, PRINCIPAL_NAME))
                .thenReturn(userDetailsDTO);

        verifyUser(mvc.perform(delete("/user/decommission/"+USER_ID)
                        .principal(principal)
                        .with(csrf())),
                LAST_NAME,
                POS_ID);
    }

    @Test
    public void testActivate() throws Exception {
        UserDetailsDTO userDetailsDTO = erpTestHelper.createUserDetailsDTO();

        when(principal.getName()).thenReturn(PRINCIPAL_NAME);

        when(userService.activateUser(USER_ID, PRINCIPAL_NAME))
                .thenReturn(userDetailsDTO);

        verifyUser(mvc.perform(post("/user/activate/"+USER_ID)
                        .principal(principal)
                        .with(csrf())),
                LAST_NAME,
                POS_ID);
    }

    @Test
    public void testGetVendorDetails() throws Exception {
        UserDetailsResponse response = new UserDetailsResponse();
        response.setPartnerId(PARTNER_ID);
        response.setPartnerName(PARTNER_NAME);
        response.setPosId(POS_ID);
        response.setPosName(POS_NAME);

        when(principal.getName()).thenReturn(PRINCIPAL_NAME);

        when(userService.getVendorDetails(PRINCIPAL_NAME))
                .thenReturn(response);

        mvc.perform(get("/user/vendor").principal(principal))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.partnerId")
                        .value(PARTNER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.partnerName")
                        .value(PARTNER_NAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$.posId")
                        .value(POS_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.posName")
                        .value(POS_NAME));
    }

    @Test
    public void testGetAllUsers() throws Exception {
        List<UserDetailsDTO> users = new ArrayList<>(1);

        UserDetailsDTO user = erpTestHelper.createUserDetailsDTO();

        users.add(user);

        when(principal.getName()).thenReturn(PRINCIPAL_NAME);
        when(userService.getAllUsers(PRINCIPAL_NAME)).thenReturn(users);

        verifyUsers(mvc.perform(get("/user/all")
                .principal(principal)));
    }

    @Test
    public void testGetAllUsersByPartnerId() throws Exception {
        List<UserDetailsDTO> users = new ArrayList<>(1);

        UserDetailsDTO user = erpTestHelper.createUserDetailsDTO();

        users.add(user);

        when(userService.getAllUsersByPartnerId(PARTNER_ID)).thenReturn(users);

        verifyUsers(mvc.perform(get("/user/all/"+PARTNER_ID)
                .principal(principal)));
    }

    @Test
    public void testGetUserById() throws Exception {
        User user = erpTestHelper.createUser();
        when(principal.getName()).thenReturn(PRINCIPAL_NAME);
        when(userService.getUserById(USER_ID, PRINCIPAL_NAME)).thenReturn(user);

        verifyUser(mvc.perform(get("/user/"+USER_ID)
                        .principal(principal)),
                LAST_NAME, POS_ID);
    }
}