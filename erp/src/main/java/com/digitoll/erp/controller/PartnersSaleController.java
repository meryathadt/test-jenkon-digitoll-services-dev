package com.digitoll.erp.controller;

import com.digitoll.commons.dto.VignetteIdDTO;
import com.digitoll.commons.exception.ExpiredAuthTokenException;
import com.digitoll.commons.exception.SaleIncompleteDataException;
import com.digitoll.commons.exception.SaleRowIncompleteDataException;
import com.digitoll.commons.exception.SaleRowNotFoundException;
import com.digitoll.commons.model.User;
import com.digitoll.commons.request.SaleRequest;
import com.digitoll.commons.response.SaleDTO;
import com.digitoll.commons.response.SaleResponse;
import com.digitoll.commons.response.SaleRowResponse;
import com.digitoll.erp.service.SaleReportService;
import com.digitoll.erp.service.SaleService;
import com.digitoll.erp.service.UserService;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;

import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;

@RestController
public class PartnersSaleController {


    private static final Logger logger = LoggerFactory.getLogger(PartnersSaleController.class);

    @Autowired
    private SaleService saleService;

    @Autowired
    private UserService userService;

    @Autowired
    private SaleReportService saleReportService;

    private static final Logger log = LoggerFactory.getLogger(PartnersSaleController.class);



    /**
     * uses partners own posId ( the posid from their system) for pos identification
     */
    @CrossOrigin
    @PreAuthorize(
            "hasAuthority('ROLE_ADMIN') or " +
            "hasAuthority('ROLE_C2') or " +
            "hasAuthority('ROLE_PARTNER_ADMIN')"
    )
    @PostMapping("/partners/sale")
    public SaleResponse purchaseVignetteWithPartnerPosPartner(
            @RequestBody SaleRequest saleRequest, Principal principal, HttpSession session)
            throws Exception {
        User user = userService.getUserDetails(principal.getName());
        saleRequest.setUserId(user.getId());
        // preffered case
        if(saleRequest.getPosId() != null) {
            return new SaleResponse(saleService.createSaleWithPartnersPos(saleRequest, user, session));
        }else {
            SaleDTO saleDTO = new SaleDTO(saleRequest);
            return new SaleResponse(saleService.createSale(saleDTO, user, session));
        }
    }

    /**
     *
     * @param saleId Registered but not activated sale
     * @param principal Needs for aspects SaleControllerRestrictions
     * @param session
     * @return Activated sale
     * @throws ExpiredAuthTokenException
     * @throws SaleIncompleteDataException
     * @throws SaleRowIncompleteDataException
     * @throws MessagingException
     * @throws IOException
     */
    @CrossOrigin
    @PreAuthorize(
            "hasAuthority('ROLE_ADMIN') or " +
            "hasAuthority('ROLE_C2') or " +
            "hasAuthority('ROLE_PARTNER_ADMIN')"
    )
    @PostMapping("/partners/sale/activate/{sale_id}")
    public SaleResponse activateSale(
            @PathVariable(value = "sale_id", required = true)
            @ApiParam(name = "sale_id", example = "Sale id, e.g. 76767656")
                    String saleId,
            Principal principal,
            HttpSession session
    )
            throws ExpiredAuthTokenException, SaleIncompleteDataException, SaleRowIncompleteDataException, MessagingException, IOException {

        return new SaleResponse(saleService.activateSaleBySaleId(saleId, session));
    }

    /**
     * Activate single vignette.
     * @param vignetteIdDTO Registered but not activated vignette id and pos id
     * @param principal Needs for aspects SaleControllerRestrictions
     * @param session
     * @return Activated single vignette
     * @throws ExpiredAuthTokenException
     * @throws HttpStatusCodeException
     * @throws SaleRowNotFoundException
     * @throws SaleRowIncompleteDataException
     * @throws MessagingException
     * @throws IOException
     */
    @CrossOrigin
    @PreAuthorize(
            "hasAuthority('ROLE_ADMIN') or " +
            "hasAuthority('ROLE_C2') or " +
            "hasAuthority('ROLE_PARTNER_ADMIN')"
    )
    @PostMapping("/partners/sale/activate/vignette")
    public SaleRowResponse activateSaleByVignetteId(
            @Valid @RequestBody VignetteIdDTO vignetteIdDTO,
            Principal principal,
            HttpSession session) throws ExpiredAuthTokenException, HttpStatusCodeException, SaleRowNotFoundException, SaleRowIncompleteDataException, MessagingException, IOException {

        return new SaleRowResponse(saleService.activateSaleByVignetteId(vignetteIdDTO, session));
    }

    @CrossOrigin
    @PreAuthorize(
            "hasAuthority('ROLE_ADMIN') or " +
            "hasAuthority('ROLE_C9') or " +
            "hasAuthority('ROLE_ACCOUNTANT') or " +
            "hasAuthority('ROLE_SUPPORT_1') or " +
            "hasAuthority('ROLE_C2')"
    )
    @GetMapping("/partners/sale/vignette/{vignette_id}")
    public SaleRowResponse getSaleRowByVignetteId(
            @PathVariable(value = "vignette_id", required = true)
            @ApiParam(name = "vignette_id", example = "vignette id, e.g. 76767656")
                    String vignetteId,
            HttpSession session) throws ExpiredAuthTokenException {

        return new SaleRowResponse(saleReportService.getSaleRowByVignetteId(vignetteId, session));
    }

    @CrossOrigin
    @PreAuthorize(
            "hasAuthority('ROLE_ADMIN') or " +
            "hasAuthority('ROLE_C9') or " +
            "hasAuthority('ROLE_ACCOUNTANT') or " +
            "hasAuthority('ROLE_SUPPORT_1') or " +
            "hasAuthority('ROLE_C2')"
    )
    @GetMapping("/partners/sale/{sale_id}")
    public SaleResponse getSaleBySaleId(
            @PathVariable(value = "sale_id", required = true)
            @ApiParam(name = "sale_id", example = "Sale id, e.g. 76767656")
                    String saleId,
            HttpSession session) throws ExpiredAuthTokenException {

        return new SaleResponse(saleReportService.getSaleById(saleId, session));
    }


}
