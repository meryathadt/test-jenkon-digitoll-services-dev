package com.digitoll.erp.controller;

import com.digitoll.commons.dto.TransactionIdDTO;
import com.digitoll.commons.dto.VignetteIdDTO;
import com.digitoll.commons.exception.ExpiredAuthTokenException;
import com.digitoll.commons.exception.SaleIncompleteDataException;
import com.digitoll.commons.exception.SaleRowIncompleteDataException;
import com.digitoll.commons.exception.SaleRowNotFoundException;
import com.digitoll.commons.model.Sale;
import com.digitoll.commons.model.User;
import com.digitoll.commons.request.SaleRequest;
import com.digitoll.commons.response.SaleDTO;
import com.digitoll.commons.response.SaleRowDTO;
import com.digitoll.erp.service.SaleService;
import com.digitoll.erp.service.UserService;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;

@RestController
public class SaleController {

    private static final Logger logger = LoggerFactory.getLogger(SaleController.class);

    @Autowired
    private SaleService saleService;

    @Autowired
    private UserService userService;

    private static final Logger log = LoggerFactory.getLogger(SaleController.class);

    /**
     * uses digitoll posId for pos identification
     */
    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_C9') or hasAuthority('ROLE_C2') or hasAuthority('ROLE_PARTNER_ADMIN')")
    @PostMapping("/sale")
    public SaleDTO purchaseVignette(@RequestBody SaleRequest saleRequest,
                                    Principal principal,
                                    HttpSession session)
            throws Exception {

        logger.info("purchaseVignette({})", saleRequest);

        // there is no way user is authenticated without userId
        User user = userService.getUserDetails(principal.getName());

        saleRequest.setUserId(user.getId());
        SaleDTO saleDTO = new SaleDTO(saleRequest);
        return saleService.createSale(saleDTO, user, session);
    }

    /**
     * uses partners own posId ( the posid from their system) for pos identification
     */
    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_C2') or hasAuthority('ROLE_PARTNER_ADMIN')")
    @PostMapping("/sale/pos")
    public SaleDTO purchaseVignetteWithPartnerPos(
            @RequestBody SaleRequest saleRequest, Principal principal, HttpSession session)
            throws Exception {
        User user = userService.getUserDetails(principal.getName());
        saleRequest.setUserId(user.getId());
        return saleService.createSaleWithPartnersPos(saleRequest, user, session);
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
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_C2') or hasAuthority('ROLE_PARTNER_ADMIN')")
    @PostMapping("/sale/activate/{sale_id}")
    public SaleDTO activateSale(
            @PathVariable(value = "sale_id", required = true)
            @ApiParam(name = "sale_id", example = "Sale id, e.g. 76767656")
                    String saleId,
            Principal principal,
            HttpSession session
    )
            throws ExpiredAuthTokenException, SaleIncompleteDataException, SaleRowIncompleteDataException, MessagingException, IOException {

        return saleService.activateSaleBySaleId(saleId, session);
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
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_C2') or hasAuthority('ROLE_PARTNER_ADMIN')")
    @PostMapping("/sale/activate/vignetteId")
    public SaleRowDTO activateSaleByVignetteId(
            @Valid @RequestBody VignetteIdDTO vignetteIdDTO,
            Principal principal,
            HttpSession session) throws ExpiredAuthTokenException, HttpStatusCodeException, SaleRowNotFoundException, SaleRowIncompleteDataException, MessagingException, IOException {

        return saleService.activateSaleByVignetteId(vignetteIdDTO, session);
    }

    /**
     *
     * @param transactionIdDTO Transaction id from bank response
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
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_C9')")
    @PostMapping("/sale/activate/trans")
    public SaleDTO activateSaleByTransactionId(
            @Valid @RequestBody TransactionIdDTO transactionIdDTO,
            Principal principal,
            HttpSession session) throws ExpiredAuthTokenException, SaleIncompleteDataException, SaleRowIncompleteDataException, MessagingException, IOException {

        return saleService.activateSaleByTransactionId(transactionIdDTO.getTransactionId(), session);
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_C9')")
    @PutMapping("/sale/trans")
    public Sale updateSaleTransactionId(
            @Valid @RequestBody TransactionIdDTO request) throws ExpiredAuthTokenException {

        return saleService.updateSaleTransactionId(request.getSaleId(), request.getTransactionId());
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/sale/fillSequences")
    public long fillSequences(
            HttpSession session) throws ExpiredAuthTokenException {

        return saleService.fillMissedSaleSequences();
    }


    /**
     * Simple, but volatile
     * make a backup before using
     */
    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/sale/fillDenormalizedData")
    public void fillDenormalizedData(
            HttpSession session) throws ExpiredAuthTokenException, InterruptedException {

        saleService.fillDenormalizedData();
    }


    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_C9')")
    @PostMapping("/sale/site")
    public SaleDTO purchaseVignetteFromSite(@RequestBody SaleDTO saleDTO, HttpSession session)
            throws Exception {

        // get default site props
        saleDTO = saleService.populateSiteUserAndCreateSale(saleDTO, session);
        return saleDTO;
    }

}
