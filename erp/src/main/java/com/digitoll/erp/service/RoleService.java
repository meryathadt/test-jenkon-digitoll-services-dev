package com.digitoll.erp.service;

import com.digitoll.commons.enumeration.UserRole;
import com.digitoll.commons.model.Role;
import com.digitoll.commons.model.User;
import com.digitoll.erp.repository.RoleRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoleService {

    private static final String PARTNER_EMPLOYEE_ROLE_ID = "5cbd154d9f8e4b0001c59b03"; // ROLE_C2

    @Autowired
    private RoleRepository roleRepository;

    public Role createRole(Role role) {
        return roleRepository.save(role);
    }

    boolean isUserAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getCode().equals(UserRole.ADMIN.getRoleCode()));
    }

    boolean isUserPartnerAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getCode().equals(UserRole.PARTNER_ADMIN.getRoleCode()));
    }

    boolean isNoPosUser(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getCode().equals(UserRole.NO_POS_USER.getRoleCode()));
    }

    Optional<Role> getPartnerEmployeeRole() {
        return roleRepository.findById(PARTNER_EMPLOYEE_ROLE_ID);
    }
    
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

}
