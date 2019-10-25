package com.digitoll.erp.aspect;

import com.digitoll.commons.dto.UserDetailsDTO;
import com.digitoll.commons.enumeration.UserRole;
import com.digitoll.commons.model.User;
import com.digitoll.erp.repository.PosRepository;
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
import java.util.List;

@Aspect
@Configuration
public class UserControllerRestrictions {

    @Autowired
    private UserService userService;

    @Autowired
    private PosRepository posRepository;
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Before("execution(* com.digitoll.erp.controller.AdminController.*(com.digitoll.commons.model.User,java.security.Principal,..))")
    public void before(JoinPoint joinPoint) throws HttpClientErrorException {
        if (joinPoint.getArgs() != null && joinPoint.getArgs()[0] != null && joinPoint.getArgs()[1] != null) {
            User userToBeCreated = (User) joinPoint.getArgs()[0];
            Principal principal = (Principal) joinPoint.getArgs()[1];
            UserDetailsDTO partnerAdminUser = userService.getUserDetailsDto(principal.getName());
            checkUserAuthority(partnerAdminUser, userToBeCreated.getPartnerId(), userToBeCreated.getPosIds());
        }
    }

    @Before("execution(* com.digitoll.erp.controller.AdminController.updateAdmin(..))")
    public void beforeUpdateAdmin(JoinPoint joinPoint) throws HttpClientErrorException {
        if (joinPoint.getArgs() != null && joinPoint.getArgs()[0] != null && joinPoint.getArgs()[1] != null) {
            UserDetailsDTO userToBeUpdated = (UserDetailsDTO) joinPoint.getArgs()[0];
            Principal principal = (Principal) joinPoint.getArgs()[1];
            UserDetailsDTO partnerAdminUser = userService.getUserDetailsDto(principal.getName());
            checkUserAuthority(partnerAdminUser, userToBeUpdated.getPartnerId(), userToBeUpdated.getPosIds());
        }
    }

    private void throwUnauthorized() {
        throw HttpClientErrorException.create(HttpStatus.UNAUTHORIZED,
                "Unauthorised",
                null,
                null,
                null);
    }


    private void checkUserAuthority(UserDetailsDTO partnerAdminUser, String partnerId, List<String> posIds) {
        if (!partnerAdminUser.hasAuthority(UserRole.ADMIN.getRoleCode())) {
            if (partnerAdminUser.hasAuthority(UserRole.PARTNER_ADMIN.getRoleCode())) {
                if (!partnerAdminUser.getPartnerId().equals(partnerId)) {
                    throwUnauthorized();
                } else {
                    checkUserPosAndPartnerPos(partnerAdminUser, posIds);
                }
            } else {
                throwUnauthorized();
            }
        }
    }

    private void checkUserPosAndPartnerPos(UserDetailsDTO partnerAdminUser, List<String> updatedUserPosIds) {
        for (String posId : updatedUserPosIds) {
            if (!posRepository.findPosByPartnerIdAndId(partnerAdminUser.getPartnerId(), posId).isPresent()) {
                throwUnauthorized();
            }
        }
    }
}
