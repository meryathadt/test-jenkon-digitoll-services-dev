package com.digitoll.erp.controller;

import com.digitoll.commons.request.AuthenticationRequest;
import com.digitoll.commons.response.AuthenticationResponse;
import com.digitoll.commons.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @CrossOrigin
    @PostMapping(value = "/user/authenticate")
    public AuthenticationResponse authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return authenticationService.authenticate(request.getUserName().toLowerCase(), request.getPassword());
    }

}
