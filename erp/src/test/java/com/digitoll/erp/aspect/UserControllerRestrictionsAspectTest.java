package com.digitoll.erp.aspect;

import com.digitoll.commons.dto.PasswordUpdateAdminDTO;
import com.digitoll.commons.dto.UserDetailsDTO;
import com.digitoll.commons.enumeration.UserRole;
import com.digitoll.commons.exception.CustomExceptionHandler;
import com.digitoll.commons.model.Pos;
import com.digitoll.commons.model.Role;
import com.digitoll.commons.model.User;
import com.digitoll.erp.aspect.UserControllerRestrictions;
import com.digitoll.erp.controller.AdminController;
import com.digitoll.erp.controller.UserController;
import com.digitoll.erp.controller.UserControllerTestBase;
import com.digitoll.erp.repository.PosRepository;
import com.digitoll.erp.utils.ErpTestHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {
        AdminController.class,
        CustomExceptionHandler.class,
        UserControllerRestrictions.class,
        AnnotationAwareAspectJAutoProxyCreator.class
})
public class UserControllerRestrictionsAspectTest extends UserControllerTestBase {
    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private PosRepository posRepository;

    protected ErpTestHelper erpTestHelper = new ErpTestHelper();

    @Test
    @WithMockUser(roles = "ADMIN", username = PRINCIPAL_NAME)
    public void testForAdmin() throws Exception {
        UserDetailsDTO partnerAdminUser = erpTestHelper.createUserDetailsDTO();
        Role adminRole = new Role();
        adminRole.setCode(UserRole.ADMIN.getRoleCode());

        partnerAdminUser.getRoles().add(adminRole);

        when(userService.getUserDetailsDto(ErpTestHelper.PRINCIPAL_NAME))
            .thenReturn(partnerAdminUser);

        UserDetailsDTO userToBeUpdated = erpTestHelper.createUserDetailsDTO();

        when(userService.updateUserAdmin(userToBeUpdated, PRINCIPAL_NAME))
                .thenReturn(userToBeUpdated);

        verifyUser(mvc.perform(post("/user/update/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userToBeUpdated))
                .with(csrf())),
                LAST_NAME, POS_ID);
    }

    @Test
    @WithMockUser(roles = "PARTNER_ADMIN", username = PRINCIPAL_NAME)
    public void testForPartnerAdmin() throws Exception {

        UserDetailsDTO partnerAdminUser = erpTestHelper.createUserDetailsDTO();
        Role adminRole = new Role();
        adminRole.setCode(UserRole.PARTNER_ADMIN.getRoleCode());

        partnerAdminUser.getRoles().add(adminRole);

        when(userService.getUserDetailsDto(ErpTestHelper.PRINCIPAL_NAME))
                .thenReturn(partnerAdminUser);

        PasswordUpdateAdminDTO passwordUpdateAdminDTO = new PasswordUpdateAdminDTO();
        passwordUpdateAdminDTO.setUserId(ErpTestHelper.USER_ID);
        passwordUpdateAdminDTO.setPassword(ErpTestHelper.PASSWORD);

        mvc.perform(post("/user/update/password/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordUpdateAdminDTO))
                        .with(csrf()))
                        .andExpect(status().isOk());

        verify(userService).updatePasswordAdmin(passwordUpdateAdminDTO, PRINCIPAL_NAME);
    }

    @Test
    @WithMockUser(roles = "PARTNER_ADMIN", username = PRINCIPAL_NAME)
    public void testForPartnerAdminWrongPos() throws Exception {
        UserDetailsDTO partnerAdminUser = erpTestHelper.createUserDetailsDTO();

        Role adminRole = new Role();
        adminRole.setCode(UserRole.PARTNER_ADMIN.getRoleCode());

        partnerAdminUser.getRoles().add(adminRole);

        when(userService.getUserDetailsDto(ErpTestHelper.PRINCIPAL_NAME))
                .thenReturn(partnerAdminUser);

        User user = erpTestHelper.createUser();

        List<String> posIDs = partnerAdminUser.getPosIds();

        mvc.perform(post("/user/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(csrf())
                    .content(objectMapper.writeValueAsString(user)))
                    .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "PARTNER_ADMIN", username = PRINCIPAL_NAME)
    public void testPartnerAdminCorrectPos() throws Exception {
        UserDetailsDTO partnerAdminUser = erpTestHelper.createUserDetailsDTO();

        Role adminRole = new Role();
        adminRole.setCode(UserRole.PARTNER_ADMIN.getRoleCode());

        partnerAdminUser.getRoles().add(adminRole);

        when(userService.getUserDetailsDto(ErpTestHelper.PRINCIPAL_NAME))
                .thenReturn(partnerAdminUser);

        when(userService.loadUserByUsername(ErpTestHelper.USERNAME))
                .thenThrow(new UsernameNotFoundException("not found"));

        User user = erpTestHelper.createUser();

        UserDetailsDTO newUser = erpTestHelper.createUserDetailsDTO();

        Pos pos = erpTestHelper.createPos();

        when(posRepository.findPosByPartnerIdAndId(partnerAdminUser.getPartnerId(),
                partnerAdminUser.getPosIds().get(0))).thenReturn(Optional.of(pos));

        when(userService.createUser(user,PRINCIPAL_NAME)).thenReturn(newUser);

        verifyUser(mvc.perform(post("/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(objectMapper.writeValueAsString(user))),
                LAST_NAME, POS_ID);
    }
}