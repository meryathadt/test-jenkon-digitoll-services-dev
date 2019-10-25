package com.digitoll.erp.aspect;

import com.digitoll.commons.dto.UserDetailsDTO;
import com.digitoll.commons.model.Pos;
import com.digitoll.erp.service.UserService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.security.Principal;

@Aspect
@Configuration
public class PosControllerRestrictions {

    @Autowired
    private UserService userService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Before("execution(* com.digitoll.erp.controller.PosController.*(com.digitoll.commons.model.Pos,java.security.Principal,..))")
    public void before(JoinPoint joinPoint) throws HttpClientErrorException {
        if(joinPoint.getArgs() != null && joinPoint.getArgs()[0] != null && joinPoint.getArgs()[1] != null) {
            Pos pos = (Pos) joinPoint.getArgs()[0];
            Principal principal = (Principal) joinPoint.getArgs()[1];
            UserDetailsDTO partnerAdminUser = userService.getUserDetailsDto(principal.getName());
            if (!partnerAdminUser.hasAuthority("ROLE_ADMIN") && !partnerAdminUser.getPartnerId().equals(pos.getPartnerId())) {
                throw HttpClientErrorException.create(HttpStatus.UNAUTHORIZED,
                        "Unauthorised",
                        null,
                        null,
                        null);
            }
        }

    }

}
