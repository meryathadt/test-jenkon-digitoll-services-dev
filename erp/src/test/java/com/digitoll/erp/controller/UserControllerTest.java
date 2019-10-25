package com.digitoll.erp.controller;

import com.digitoll.commons.dto.PasswordUpdateUserDTO;
import com.digitoll.commons.dto.UserDetailsDTO;
import com.digitoll.commons.exception.CustomExceptionHandler;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static com.digitoll.erp.utils.ErpTestHelper.PRINCIPAL_NAME;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {
        UserController.class,
        CustomExceptionHandler.class
})
@WithMockUser(username = PRINCIPAL_NAME)
public class UserControllerTest extends UserControllerTestBase {

    @Test
	public void testGetDetails() throws Exception {
        UserDetailsDTO user = erpTestHelper.createUserDetailsDTO();

        when(principal.getName()).thenReturn(PRINCIPAL_NAME);

        when(userService.getUserDetailsDto(PRINCIPAL_NAME)).thenReturn(user);

        verifyUser(mvc.perform(get("/user/details")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .principal(principal)
                .with(csrf())),
                LAST_NAME,
                POS_ID);
	}

    @Test
    public void testUpdate() throws Exception {
        UserDetailsDTO oldUser = erpTestHelper.createUserDetailsDTO();
        UserDetailsDTO newUser = erpTestHelper.createUserDetailsDTO();

        List<String> posIdList = new ArrayList<>();
        posIdList.add(POS_ID2);

        newUser.setPosIds(posIdList);
        newUser.setLastName(LAST_NAME2);

        when(principal.getName()).thenReturn(PRINCIPAL_NAME);
        when(userService.updateUser(oldUser, PRINCIPAL_NAME))
                .thenReturn(newUser);

        verifyUser(mvc.perform(post("/user/update")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .principal(principal)
                .content(objectMapper.writeValueAsString(oldUser))
                .with(csrf())),
                LAST_NAME2,
                POS_ID2);
    }

    @Test
    public void testUpdatePasswordUser() throws Exception {
        when(principal.getName()).thenReturn(PRINCIPAL_NAME);
        PasswordUpdateUserDTO updateDto = new PasswordUpdateUserDTO();

        updateDto.setNewPassword(NEW_PASSWORD);
        updateDto.setOldPassword(PASSWORD);

        mvc.perform(post("/user/update/password")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .principal(principal)
                .content(objectMapper.writeValueAsString(updateDto))
                .with(csrf()))
                .andExpect(status().isOk());

        verify(userService).updatePasswordUser(updateDto, PRINCIPAL_NAME);
    }

    @Test
    public void testUpdatePasswordUserPasswordValidationFailed() throws Exception {
        when(principal.getName()).thenReturn(PRINCIPAL_NAME);
        PasswordUpdateUserDTO updateDto = new PasswordUpdateUserDTO();

        updateDto.setNewPassword("new password");
        updateDto.setOldPassword(PASSWORD);

        mvc.perform(post("/user/update/password")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .principal(principal)
                .content(objectMapper.writeValueAsString(updateDto))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updatePasswordUser(updateDto, PRINCIPAL_NAME);
    }

    @Test
    public void testUpdatePasswordUserPasswordTooLongValidationFailed() throws Exception {
        when(principal.getName()).thenReturn(PRINCIPAL_NAME);
        PasswordUpdateUserDTO updateDto = new PasswordUpdateUserDTO();

        updateDto.setNewPassword(RandomStringUtils.randomAlphabetic(251));
        updateDto.setOldPassword(PASSWORD);

        mvc.perform(post("/user/update/password")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .principal(principal)
                .content(objectMapper.writeValueAsString(updateDto))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updatePasswordUser(updateDto, PRINCIPAL_NAME);
    }

    @Test
    public void testUpdateIsBadRequest() throws Exception {
        mvc.perform(post("/user/update")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdatePasswordUserIsBadRequest() throws Exception {
        mvc.perform(post("/user/update/password")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isBadRequest())
        ;
    }
}