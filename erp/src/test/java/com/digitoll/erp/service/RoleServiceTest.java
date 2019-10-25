package com.digitoll.erp.service;

import com.digitoll.commons.enumeration.UserRole;
import com.digitoll.commons.model.Role;
import com.digitoll.commons.model.User;
import com.digitoll.erp.utils.ErpTestHelper;
import com.digitoll.erp.repository.RoleRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = RoleService.class)
@RunWith( SpringRunner.class)
public class RoleServiceTest {
    @MockBean
    private RoleRepository roleRepository;

    @Autowired
    private RoleService roleService;

    @Test
    public void testCreateRole() throws ParseException {
        ErpTestHelper erpTestHelper = new ErpTestHelper();
        Role role = erpTestHelper.createRole(UserRole.ADMIN.getRoleCode());
        when(roleRepository.save(Mockito.any(Role.class))).thenReturn(role);
        assertSame(roleService.createRole(role), role);
    }

    @Test
    public void testCreateRoleFail() throws ParseException {
        ErpTestHelper erpTestHelper = new ErpTestHelper();
        Role role = erpTestHelper.createRole(UserRole.ADMIN.getRoleCode());
        when(roleRepository.save(Mockito.any(Role.class))).thenReturn(null);
        assertNull(roleService.createRole(role));
    }

    @Test
    public void testGetAllRoles() throws ParseException {
        ErpTestHelper erpTestHelper = new ErpTestHelper();
        Role roleAdmin = erpTestHelper.createRole(UserRole.ADMIN.getRoleCode());
        Role rolePartner = erpTestHelper.createRole(UserRole.PARTNER_ADMIN.getRoleCode());
        Role roleC2 = erpTestHelper.createRole(UserRole.PARTNER_EMPLOYEE.getRoleCode());
        Role roleC9 = erpTestHelper.createRole(UserRole.WEB_USER.getRoleCode());
        List<Role> allRoles = new ArrayList<>();
        allRoles.add(roleAdmin);
        allRoles.add(rolePartner);
        allRoles.add(roleC2);
        allRoles.add(roleC9);
        when(roleRepository.findAll()).thenReturn(allRoles);
        assertEquals(roleService.getAllRoles().size(), allRoles.size());
    }

    @Test
    public void testGetAllRolesFail() {
        when(roleRepository.findAll()).thenReturn(new ArrayList<>());
        assertTrue(roleService.getAllRoles().isEmpty());
    }

    @Test
    public void testGetPartnerRole() throws ParseException {
        ErpTestHelper erpTestHelper = new ErpTestHelper();
        Role rolePartner = erpTestHelper.createRole(UserRole.PARTNER_EMPLOYEE.getRoleCode());
        when(roleRepository.findById(Mockito.anyString())).thenReturn(Optional.ofNullable(rolePartner));
        assertEquals(roleService.getPartnerEmployeeRole().get().getCode(), UserRole.PARTNER_EMPLOYEE.getRoleCode());
    }

    @Test
    public void testGetPartnerRoleFail() {
        when(roleRepository.findById(Mockito.anyString())).thenReturn(null);
        assertNull(roleService.getPartnerEmployeeRole());
    }

    @Test
    public void testIsUserAdmin() throws ParseException {
        ErpTestHelper erpTestHelper = new ErpTestHelper();
        Role roleAdmin = erpTestHelper.createRole(UserRole.ADMIN.getRoleCode());
        User user = erpTestHelper.createUser();
        user.getRoles().add(roleAdmin);
        assertTrue(roleService.isUserAdmin(user));
    }

    @Test(expected = NullPointerException.class)
    public void testIsUserAdminFail() throws ParseException {
        ErpTestHelper erpTestHelper = new ErpTestHelper();
        User user = erpTestHelper.createUser();
        user.setRoles(null);
        roleService.isUserAdmin(user);
    }

    @Test
    public void testIsUserNotAdmin() throws ParseException {
        ErpTestHelper erpTestHelper = new ErpTestHelper();
        User user = erpTestHelper.createUser();
        assertFalse(roleService.isUserAdmin(user));
    }

    @Test
    public void testIsUserPartnerAdmin() throws ParseException {
        ErpTestHelper erpTestHelper = new ErpTestHelper();
        Role roleAdmin = erpTestHelper.createRole(UserRole.PARTNER_ADMIN.getRoleCode());
        User user = erpTestHelper.createUser();
        user.getRoles().add(roleAdmin);
        assertTrue(roleService.isUserPartnerAdmin(user));
    }

    @Test
    public void testIsUserNotPartnerAdmin() throws ParseException {
        ErpTestHelper erpTestHelper = new ErpTestHelper();
        User user = erpTestHelper.createUser();
        assertFalse(roleService.isUserPartnerAdmin(user));
    }

    @Test(expected = NullPointerException.class)
    public void testIsUserPartnerAdminFail() throws ParseException {
        ErpTestHelper erpTestHelper = new ErpTestHelper();
        User user = erpTestHelper.createUser();
        user.setRoles(null);
        roleService.isUserPartnerAdmin(user);
    }
}
