package com.digitoll.erp.controller;

import com.digitoll.commons.dto.PasswordUpdateUserDTO;
import com.digitoll.commons.dto.UserDetailsDTO;
import com.digitoll.erp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

	// TODO do we need /login or /details
    @CrossOrigin
    @RequestMapping(value = "/user/details", method = {RequestMethod.GET, RequestMethod.OPTIONS,RequestMethod.POST}, produces = "application/json;charset=UTF-8")
	public UserDetailsDTO details(Principal principal) {
        String username = principal.getName();
		return userService.getUserDetailsDto(username);
	}

    @CrossOrigin
    @RequestMapping(value = "/user/update", method = {RequestMethod.POST, RequestMethod.OPTIONS}, produces = "application/json;charset=UTF-8")
    public UserDetailsDTO update(
            @Valid @RequestBody UserDetailsDTO updatedUser,
            Principal principal
    ) {
        return userService.updateUser(updatedUser, principal.getName());
    }

    @CrossOrigin
    @RequestMapping(value = "/user/update/password", method = {RequestMethod.POST, RequestMethod.OPTIONS}, produces = "application/json;charset=UTF-8")
    public void updatePasswordUser(
            @Valid @RequestBody PasswordUpdateUserDTO updateDto,
            Principal principal
    ) {
        userService.updatePasswordUser(updateDto, principal.getName());
    }    

}
