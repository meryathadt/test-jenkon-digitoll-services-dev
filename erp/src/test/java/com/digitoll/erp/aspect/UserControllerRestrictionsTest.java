package com.digitoll.erp.aspect;

import com.digitoll.commons.dto.UserDetailsDTO;
import com.digitoll.commons.enumeration.UserRole;
import com.digitoll.commons.model.Pos;
import com.digitoll.commons.model.User;
import com.digitoll.commons.util.BasicUtils;
import com.digitoll.erp.repository.PosRepository;
import com.digitoll.erp.service.UserService;
import com.digitoll.erp.utils.ErpTestHelper;
import org.aspectj.lang.JoinPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;

import java.security.Principal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.digitoll.erp.utils.ErpTestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@ContextConfiguration(classes = UserControllerRestrictions.class)
@RunWith(SpringRunner.class)
public class UserControllerRestrictionsTest {
    @MockBean
    private UserService userService;

    @MockBean
    private PosRepository posRepository;

    @Autowired
    private UserControllerRestrictions userControllerRestrictions;

    private ErpTestHelper erpTestHelper;
    private Pos pos;
    private JoinPoint joinPoint;
    private UserDetailsDTO partnerAdminUser;
    private UserDetailsDTO updatedUser;
    private Principal principal;

    @Before
    public void init() throws ParseException {
        erpTestHelper = new ErpTestHelper();
        pos = erpTestHelper.createPos();
        joinPoint = Mockito.mock(JoinPoint.class);
        partnerAdminUser = new UserDetailsDTO();
        updatedUser = new UserDetailsDTO();
        BasicUtils.copyPropsSkip(erpTestHelper.createUser(), partnerAdminUser, Collections.singletonList("password"));
        BasicUtils.copyPropsSkip(erpTestHelper.createUser(), updatedUser, Collections.singletonList("password"));
        principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn(USERNAME);
    }

    @Test
    public void testBeforeRegisterPartnerAdmin() throws ParseException {
        partnerAdminUser.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_ADMIN.getRoleCode())));
        partnerAdminUser.setPartnerId(PARTNER_ID);
        User userToBeCreated = erpTestHelper.createUser();
        pos.setPartnerId(PARTNER_ID);
        when(joinPoint.getArgs()).thenReturn(new Object[]{userToBeCreated, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(partnerAdminUser);
        when(posRepository.findPosByPartnerIdAndId(partnerAdminUser.getPartnerId(), userToBeCreated.getPosIds().get(0))).thenReturn(Optional.ofNullable(pos));

        userControllerRestrictions.before(joinPoint);
        verify(posRepository).findPosByPartnerIdAndId(partnerAdminUser.getPartnerId(), userToBeCreated.getPosIds().get(0));
    }

    @Test
    public void testBeforePartnerAdminWrongPartnerId() throws ParseException {
        partnerAdminUser.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_ADMIN.getRoleCode())));
        setPosIdToUser(POS_ID);
        partnerAdminUser.setPartnerId(PARTNER_ID);
        User userToBeCreated = erpTestHelper.createUser();
        pos.setPartnerId("other id");
        when(joinPoint.getArgs()).thenReturn(new Object[]{userToBeCreated, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(partnerAdminUser);
        when(posRepository.findPosByPartnerIdAndId(partnerAdminUser.getPartnerId(), userToBeCreated.getPosIds().get(0))).thenReturn(Optional.empty());

        try {
            userControllerRestrictions.before(joinPoint);
        } catch (HttpClientErrorException ex) {
            assertEquals(ex.getStatusCode(), UNAUTHORIZED);
        }
    }


    @Test
    public void testBeforePartnerAdminWrongPosId() throws ParseException {
        partnerAdminUser.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_ADMIN.getRoleCode())));
        setPosIdToUser(POS_ID_3);
        partnerAdminUser.setPartnerId(PARTNER_ID);
        User userToBeCreated = erpTestHelper.createUser();
        pos.setPartnerId(PARTNER_ID);
        List<String> posIds = new ArrayList<>();
        posIds.add(POS_ID_2);
        userToBeCreated.setPosIds(posIds);
        when(joinPoint.getArgs()).thenReturn(new Object[]{userToBeCreated, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(partnerAdminUser);
        when(posRepository.findPosByPartnerIdAndId(partnerAdminUser.getPartnerId(), userToBeCreated.getPosIds().get(0))).thenReturn(Optional.empty());

        try {
            userControllerRestrictions.before(joinPoint);
        } catch (HttpClientErrorException ex) {
            assertEquals(ex.getStatusCode(), UNAUTHORIZED);
        }
    }

    @Test
    public void testBeforeRegisterAdmin() throws ParseException {
        partnerAdminUser.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.ADMIN.getRoleCode())));
        setPosIdToUser(POS_ID);
        partnerAdminUser.setPartnerId(PARTNER_ID);
        User userToBeCreated = erpTestHelper.createUser();
        pos.setPartnerId(PARTNER_ID);
        when(joinPoint.getArgs()).thenReturn(new Object[]{userToBeCreated, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(partnerAdminUser);
        when(posRepository.findPosByPartnerIdAndId(partnerAdminUser.getPartnerId(), userToBeCreated.getPosIds().get(0))).thenReturn(Optional.ofNullable(pos));

        userControllerRestrictions.before(joinPoint);
        verify(posRepository, never()).findPosByPartnerIdAndId(partnerAdminUser.getPartnerId(), userToBeCreated.getPosIds().get(0));
    }

    @Test
    public void testBeforeRegisterNoAdminUser() throws ParseException {
        partnerAdminUser.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_EMPLOYEE.getRoleCode())));
        setPosIdToUser(POS_ID);
        partnerAdminUser.setPartnerId(PARTNER_ID);
        User userToBeCreated = erpTestHelper.createUser();
        pos.setPartnerId(PARTNER_ID);
        when(joinPoint.getArgs()).thenReturn(new Object[]{userToBeCreated, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(partnerAdminUser);
        when(posRepository.findPosByPartnerIdAndId(partnerAdminUser.getPartnerId(), userToBeCreated.getPosIds().get(0))).thenReturn(Optional.ofNullable(pos));

        try {
            userControllerRestrictions.before(joinPoint);
        } catch (HttpClientErrorException ex) {
            assertEquals(ex.getStatusCode(), UNAUTHORIZED);
        }
    }

    @Test
    public void testBeforeUpdateAdminPartnerAdmin() throws ParseException {
        partnerAdminUser.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_ADMIN.getRoleCode())));
        setPosIdToUser(POS_ID);
        partnerAdminUser.setPartnerId(PARTNER_ID);
        pos.setPartnerId(PARTNER_ID);
        when(joinPoint.getArgs()).thenReturn(new Object[]{updatedUser, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(partnerAdminUser);
        when(posRepository.findPosByPartnerIdAndId(partnerAdminUser.getPartnerId(), updatedUser.getPosIds().get(0))).thenReturn(Optional.ofNullable(pos));

        userControllerRestrictions.beforeUpdateAdmin(joinPoint);
        verify(posRepository).findPosByPartnerIdAndId(partnerAdminUser.getPartnerId(), updatedUser.getPosIds().get(0));
    }

    @Test
    public void testBeforeUpdatePartnerAdminWrongPartnerId() throws ParseException {
        partnerAdminUser.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_ADMIN.getRoleCode())));
        setPosIdToUser(POS_ID);
        partnerAdminUser.setPartnerId(PARTNER_ID);
        pos.setPartnerId("other id");
        when(joinPoint.getArgs()).thenReturn(new Object[]{updatedUser, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(partnerAdminUser);
        when(posRepository.findPosByPartnerIdAndId(partnerAdminUser.getPartnerId(), updatedUser.getPosIds().get(0))).thenReturn(Optional.empty());

        try {
            userControllerRestrictions.beforeUpdateAdmin(joinPoint);
        } catch (HttpClientErrorException ex) {
            assertEquals(ex.getStatusCode(), UNAUTHORIZED);
        }
    }


    @Test
    public void testBeforeUpdatePartnerAdminWrongPosId() throws ParseException {
        partnerAdminUser.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_ADMIN.getRoleCode())));
        setPosIdToUser(POS_ID_3);
        partnerAdminUser.setPartnerId(PARTNER_ID);
        pos.setPartnerId(PARTNER_ID);
        List<String> posIds = new ArrayList<>();
        posIds.add(POS_ID_2);
        updatedUser.setPosIds(posIds);
        when(joinPoint.getArgs()).thenReturn(new Object[]{updatedUser, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(partnerAdminUser);
        when(posRepository.findPosByPartnerIdAndId(partnerAdminUser.getPartnerId(), updatedUser.getPosIds().get(0))).thenReturn(Optional.empty());

        try {
            userControllerRestrictions.beforeUpdateAdmin(joinPoint);
        } catch (HttpClientErrorException ex) {
            assertEquals(ex.getStatusCode(), UNAUTHORIZED);
        }
    }

    @Test
    public void testBeforeUpdateAdmin() throws ParseException {
        partnerAdminUser.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.ADMIN.getRoleCode())));
        setPosIdToUser(POS_ID);
        partnerAdminUser.setPartnerId(PARTNER_ID);
        pos.setPartnerId(PARTNER_ID);
        when(joinPoint.getArgs()).thenReturn(new Object[]{updatedUser, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(partnerAdminUser);
        when(posRepository.findPosByPartnerIdAndId(partnerAdminUser.getPartnerId(), updatedUser.getPosIds().get(0))).thenReturn(Optional.ofNullable(pos));

        userControllerRestrictions.beforeUpdateAdmin(joinPoint);
        verify(posRepository, never()).findPosByPartnerIdAndId(partnerAdminUser.getPartnerId(), updatedUser.getPosIds().get(0));
    }

    @Test
    public void testBeforeUpdateNoAdminUser() throws ParseException {
        partnerAdminUser.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_EMPLOYEE.getRoleCode())));
        setPosIdToUser(POS_ID);
        partnerAdminUser.setPartnerId(PARTNER_ID);
        pos.setPartnerId(PARTNER_ID);
        when(joinPoint.getArgs()).thenReturn(new Object[]{updatedUser, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(partnerAdminUser);
        when(posRepository.findPosByPartnerIdAndId(partnerAdminUser.getPartnerId(), updatedUser.getPosIds().get(0))).thenReturn(Optional.ofNullable(pos));

        try {
            userControllerRestrictions.beforeUpdateAdmin(joinPoint);
        } catch (HttpClientErrorException ex) {
            assertEquals(ex.getStatusCode(), UNAUTHORIZED);
        }
    }

    private void setPosIdToUser(String posId) {
        List<String> posIds = new ArrayList<>();
        posIds.add(posId);
        partnerAdminUser.setPosIds(posIds);
    }
}
