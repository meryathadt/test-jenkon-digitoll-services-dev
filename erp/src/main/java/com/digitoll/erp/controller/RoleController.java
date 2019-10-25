package com.digitoll.erp.controller;

import com.digitoll.commons.model.Role;
import com.digitoll.erp.service.RoleService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoleController {

    @Autowired
    private RoleService roleService;

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping(value = "/role")
    public Role createRole(
            @RequestBody Role request
    ) {
        return roleService.createRole(request);
    }
    
    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping(value = "/roles")
    public List<Role> getAllRoles() {
        return roleService.getAllRoles();
    }
}
