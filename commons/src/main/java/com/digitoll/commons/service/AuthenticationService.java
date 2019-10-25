package com.digitoll.commons.service;

import com.digitoll.commons.response.AuthenticationResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthenticationService {

    @Value("${security.jwt.expiration:2520000}")
    private String tokenExpiration;

    @Value("${security.jwt.type:JWT}")
    private String tokenType;

    @Value("${security.jwt.secret}")
    private String tokenSecret;

    @Value("${security.jwt.issuer}")
    private String tokenIssuer;

    @Value("${security.jwt.audience}")
    private String tokenAudience;
    
    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthenticationResponse authenticate(String username, String password) {

        User user = ((User) attemptAuthentication(username, password).getPrincipal());

        List<String> roles = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        byte[] signingKey = tokenSecret.getBytes();

        String token = Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(signingKey), SignatureAlgorithm.HS512)
                .setHeaderParam("typ", tokenType)
                .setIssuer(tokenIssuer)
                .setAudience(tokenAudience)
                .setSubject(user.getUsername())
                .setExpiration(new Date(System.currentTimeMillis() + Long.parseLong(tokenExpiration)))
                .claim("rol", roles)
                .compact();

        AuthenticationResponse response = new AuthenticationResponse();

        response.setToken(token);

        return response;
    }

    private Authentication attemptAuthentication(String username, String password) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);

        //Here a org.springframework.security.authentication.BadCredentialsException might be thrown.
        //The try-catch block here is removed since there is a centralized exception handling place - 
        //CustomRestExceptionHandler, using the @ControllerAdvice        
        Authentication auth = authenticationManager.authenticate(authenticationToken);
        
        return auth;
    }
}
