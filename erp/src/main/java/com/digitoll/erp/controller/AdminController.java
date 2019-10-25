package com.digitoll.erp.controller;

import com.digitoll.commons.dto.PasswordUpdateAdminDTO;
import com.digitoll.commons.dto.UserDetailsDTO;
import com.digitoll.commons.exception.UserExistsException;
import com.digitoll.commons.model.User;
import com.digitoll.commons.response.UserDetailsResponse;
import com.digitoll.erp.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@RestController
public class AdminController {

    @Autowired
    private UserService userService;

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_PARTNER_ADMIN')")
    @RequestMapping(value = "/user/register", method = {RequestMethod.POST})
    @ApiOperation("Register a user")
    public UserDetailsDTO register(
            @Valid @RequestBody User user,
            Principal principal,
            BindingResult bindingResult
    ) throws UserExistsException {
        try {
            userService.loadUserByUsername(user.getUsername());

//            // TODO do we use BindingResult???
//            bindingResult
//                    .rejectValue("username", "error.user",
//                            "There is already a user registered with the username provided");

            throw new UserExistsException("There is already a user registered with the username provided");
        } catch (UsernameNotFoundException e) {

            return userService.createUser(user, principal.getName());
        }
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_PARTNER_ADMIN')")
    @RequestMapping(value = "/user/update/admin", method = {RequestMethod.POST, RequestMethod.OPTIONS}, produces = "application/json;charset=UTF-8")
    public UserDetailsDTO updateAdmin(
            @Valid @RequestBody UserDetailsDTO updatedUser,
            Principal principal
    ) {
        return userService.updateUserAdmin(updatedUser, principal.getName());
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_PARTNER_ADMIN')")
    @RequestMapping(value = "/user/update/password/admin", method = {RequestMethod.POST, RequestMethod.OPTIONS}, produces = "application/json;charset=UTF-8")
    public void updatePasswordAdmin(
            @Valid @RequestBody PasswordUpdateAdminDTO updateDto,
            Principal principal
    ) {
        userService.updatePasswordAdmin(updateDto, principal.getName());
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(value = "/user/delete/{userId}", method = RequestMethod.DELETE)
    public void delete(@PathVariable String userId, Principal principal) {

        userService.deleteUser(userId, principal.getName());
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_PARTNER_ADMIN')")
    @RequestMapping(value = "/user/decommission/{userId}", method = RequestMethod.DELETE)
    public UserDetailsDTO decommission(@PathVariable String userId, Principal principal) {

        return userService.decommissionUser(userId, principal.getName());
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_PARTNER_ADMIN')")
    @RequestMapping(value = "/user/activate/{userId}", method = RequestMethod.POST)
    public UserDetailsDTO activate(@PathVariable String userId, Principal principal) {

        return userService.activateUser(userId, principal.getName());
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(value = "/user/vendor", method = {RequestMethod.GET, RequestMethod.OPTIONS})
    public UserDetailsResponse getVendorDetails(Principal principal) {
        String username = principal.getName();
        return userService.getVendorDetails(username);
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_PARTNER_ADMIN')")
    @GetMapping(value = "/user/all")
    public List<UserDetailsDTO> getAllUsers(Principal principal) {

        return userService.getAllUsers(principal.getName());
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping(value = "/user/all/{partnerId}")
    public List<UserDetailsDTO> getAllUsersByPartnerId(@PathVariable String partnerId) {

        return userService.getAllUsersByPartnerId(partnerId);
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_PARTNER_ADMIN')")
    @GetMapping(value = "/user/{userId}")
    public User getUserById(@PathVariable String userId, Principal principal) {

        return userService.getUserById(userId, principal.getName());
    }
}