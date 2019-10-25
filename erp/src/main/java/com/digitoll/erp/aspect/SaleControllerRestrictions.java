package com.digitoll.erp.aspect;

import com.digitoll.commons.dto.TransactionIdDTO;
import com.digitoll.commons.dto.UserDetailsDTO;
import com.digitoll.commons.dto.VignetteIdDTO;
import com.digitoll.commons.enumeration.UserRole;
import com.digitoll.commons.exception.SaleNotFoundException;
import com.digitoll.commons.model.Sale;
import com.digitoll.commons.request.SaleRequest;
import com.digitoll.erp.repository.PosRepository;
import com.digitoll.erp.repository.SaleRepository;
import com.digitoll.erp.service.UserService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.security.Principal;
import java.util.Optional;

@Aspect
@Configuration
public class SaleControllerRestrictions {

    @Autowired
    private UserService userService;

    @Autowired
    private PosRepository posRepository;

    @Autowired
    private SaleRepository saleRepository;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Before("execution(* com.digitoll.erp.controller.SaleController.purchaseVignette(..))")
    public void beforeRegister(JoinPoint joinPoint) throws HttpClientErrorException {
        if (joinPoint.getArgs() != null && joinPoint.getArgs()[0] != null && joinPoint.getArgs()[1] != null) {
            SaleRequest saleRequest = (SaleRequest) joinPoint.getArgs()[0];
            UserDetailsDTO user = getUserDetailsDTO(joinPoint);

            if (!user.hasAuthority(UserRole.ADMIN.getRoleCode()) && !user.hasAuthority(UserRole.NO_POS_USER.getRoleCode())) {
                if (user.hasAuthority(UserRole.PARTNER_ADMIN.getRoleCode())) {
                    if (!posRepository.findPosByPartnerIdAndId(user.getPartnerId(), saleRequest.getPosId()).isPresent()) {
                        throwUnauthorizedException();
                    }
                } else {
                    if (user.getPosIds() == null || user.getPosIds().isEmpty() || StringUtils.isEmpty(saleRequest.getPosId())) {
                        throwBadRequestException();
                    } else if (!user.getPosIds().contains(saleRequest.getPosId())) {
                        throwUnauthorizedException();
                    }
                }
            }
        }
    }

    @Before("execution(* com.digitoll.erp.controller.SaleController.activateSale(..))")
    public void beforeActivateWithSaleId(JoinPoint joinPoint) throws HttpClientErrorException, SaleNotFoundException {
        if (joinPoint.getArgs() != null && joinPoint.getArgs()[0] != null && joinPoint.getArgs()[1] != null) {
            String saleId = (String) joinPoint.getArgs()[0];
            Sale sale = Optional.ofNullable(saleRepository.findOneById(saleId))
                    .orElseThrow(() -> new SaleNotFoundException("Sale with id " + saleId + " not found!"));
            checkAuthorityWithPosId(getUserDetailsDTO(joinPoint), sale.getPosId());
        }
    }

    @Before("execution(* com.digitoll.erp.controller.SaleController.activateSaleByVignetteId(..))")
    public void beforeActivateWithVignetteId(JoinPoint joinPoint) throws HttpClientErrorException {
        if (joinPoint.getArgs() != null && joinPoint.getArgs()[0] != null && joinPoint.getArgs()[1] != null) {
            VignetteIdDTO vignetteIdDTO = (VignetteIdDTO) joinPoint.getArgs()[0];
            checkAuthorityWithPosId(getUserDetailsDTO(joinPoint), vignetteIdDTO.getPosId());
        }
    }

    @Before("execution(* com.digitoll.erp.controller.SaleController.activateSaleByTransactionId(..))")
    public void beforeActivateWithTransactionId(JoinPoint joinPoint) throws HttpClientErrorException, SaleNotFoundException {
        if (joinPoint.getArgs() != null && joinPoint.getArgs()[0] != null && joinPoint.getArgs()[1] != null) {
            TransactionIdDTO transactionId = (TransactionIdDTO) joinPoint.getArgs()[0];

            Sale sale = Optional.ofNullable(saleRepository.findOneByBankTransactionId(transactionId.getTransactionId()))
                    .orElseThrow(() -> new SaleNotFoundException("Sale with transaction id "
                            + transactionId.getTransactionId() + " not found!"));
            checkAuthorityWithPosId(getUserDetailsDTO(joinPoint), sale.getPosId());
        }
    }

    private void checkAuthorityWithPosId(UserDetailsDTO user, String posId) {

        if (user.hasAuthority(UserRole.ADMIN.getRoleCode()) || user.hasAuthority(UserRole.NO_POS_USER.getRoleCode())) {
            return;
        }
        // is a partner admin
        boolean isPartnerAdmin = user.hasAuthority(UserRole.PARTNER_ADMIN.getRoleCode());
        // is a partner admin, but the requested pos is not within the partner Pos list
        Boolean adminAndPosPartnerMismatch = isPartnerAdmin && !posRepository.findPosByPartnerIdAndId(user.getPartnerId(), posId).isPresent();
        // no pos id in the request or user
        boolean posIdMissing = user.getPosIds() == null || user.getPosIds().isEmpty() || StringUtils.isEmpty(posId);
        // check if sale pos id is part of user pos ids
        Boolean userHasNoPosPermission = !isPartnerAdmin && !posIdMissing && !user.getPosIds().contains(posId);

        //Partner admin doesn't have assigned pos and match post parameter by partner id, not by pos id
        if (!isPartnerAdmin && posIdMissing) {
            throwBadRequestException();
        }

        if (userHasNoPosPermission || adminAndPosPartnerMismatch) {
            throwUnauthorizedException();
        }

    }

    private UserDetailsDTO getUserDetailsDTO(JoinPoint joinPoint) {
        Principal principal = (Principal) joinPoint.getArgs()[1];
        return userService.getUserDetailsDto(principal.getName());
    }

    public void throwBadRequestException() {
        throw HttpClientErrorException.create(HttpStatus.BAD_REQUEST,
                "Either user or sale pos id missed",
                null,
                null,
                null);
    }

    public void throwUnauthorizedException() {
        throw HttpClientErrorException.create(HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                null,
                null,
                null);
    }
}
