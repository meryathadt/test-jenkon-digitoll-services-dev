package com.digitoll.erp.controller;

import com.digitoll.commons.kapsch.response.c9.PaginatedKapshSearchResponse;
import com.digitoll.commons.kapsch.response.c9.PeriodSalesResponse;
import com.digitoll.commons.exception.ExpiredAuthTokenException;
import com.digitoll.commons.kapsch.response.c9.VignetteStatesResponse;
import com.digitoll.erp.service.KapschService;
import com.digitoll.commons.kapsch.response.ApiVersionResponse;
import com.digitoll.commons.kapsch.response.VignetteInventoryResponse;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Date;

@RestController
@RequestMapping("/v1/cbo/vignette")
public class KapschController {

    @Autowired
    private KapschService kapschService;

    @GetMapping("/version")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiVersionResponse returnInt() {
        return kapschService.getVersion();
    }

    @GetMapping("/inventory")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public VignetteInventoryResponse getVignetteInventory(HttpSession session) throws ExpiredAuthTokenException {

        return kapschService.getInventory(session);

    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public PaginatedKapshSearchResponse searchVignette(
            @RequestParam(value = "page_number", required = true)
            @ApiParam(name = "page_number", example = "Page number, e.g. 5", required = true)
                    Integer pageNumber,
            @RequestParam(value = "page_size", required = true)
            @ApiParam(name = "page_size", example = "Page size, e.g. 100", required = true)
                    Integer pageSize,
            @RequestParam(value = "last_record", required = true)
            @ApiParam(name = "last_record", example = "Current page record from Kapsch starts from 1, e.g. 4", required = true)
                    Integer lastRecord,
            @RequestParam(value = "sorting_parameter", required = false)
            @ApiParam(name = "sorting_parameter", example = "Sorting parameter, e.g. price.amount")
                    String sortingParameter,
            @RequestParam(value = "sorting_direction", required = false, defaultValue = "ASC")
            @ApiParam(name = "sorting_direction", example = "Sorting direction: ASC, DESC", defaultValue = "ASC")
                    Sort.Direction sortingDirection,
            @RequestParam(value = "vignette_id", required = false)
            @ApiParam(name = "vignette_id", example = "Vignette id, e.g. 19070233645094")
                    String eVignetteID,
            @RequestParam(value = "partner_id", required = false)
            @ApiParam(name = "partner_id", example = "Sales partner id, e.g. 1")
                    Integer salesPartnerID,
            @RequestParam(value = "product_id", required = false)
            @ApiParam(name = "product_id", example = "Product id, e.g. 101")
                    Integer productID,
            @RequestParam(value = "lpn", required = false)
            @ApiParam(name = "lpn", example = "License plate number (Combination with country code is required), e.g. CA1234CA")
                    String lpn,
            @RequestParam(value = "country_code", required = false)
            @ApiParam(name = "country_code", example = "Country code (Combination with LPN is required), e.g. BG")
                    String countryCode,
            @RequestParam(value = "status", required = false)
            @ApiParam(name = "status", example = "Vignette status: 1,2,3")
                    Integer status,
            @RequestParam(value = "records_from", required = false)
            @ApiParam(name = "records_from", example = "Records pages of 500 offset, e.g. 1")
                    String recordsFrom,
            @RequestParam(value = "last_update_from", required = false)
            @ApiParam(name = "last_update_from", example = "Last updated records from date in ISO format, e.g. 2019-08-31T09:00:00.000Z")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    Date lastUpdateFrom,
            @RequestParam(value = "last_update_to", required = false)
            @ApiParam(name = "last_update_to", example = "Last updated records to date in ISO format, e.g. 2019-08-31T09:00:00.000Z")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    Date lastUpdateTo,
            HttpSession session
    ) throws ExpiredAuthTokenException {
        PageRequest page = PageRequest.of(pageNumber, pageSize);

        return kapschService.vignetteSearch(sortingParameter, sortingDirection, lastRecord, eVignetteID, salesPartnerID, productID, lpn, countryCode, recordsFrom,
                status, lastUpdateFrom, lastUpdateTo, session, page
        );
    }

    @GetMapping("/states")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public VignetteStatesResponse getVignetteStates(
            HttpSession session
    ) throws ExpiredAuthTokenException {

        return kapschService.getVignetteStates(session);
    }

    @GetMapping("/inventoryC9")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public VignetteInventoryResponse getInventoryC9(
            HttpSession session
    ) throws ExpiredAuthTokenException {

        return kapschService.getInventoryC9(session);
    }

    @GetMapping("/evsales")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public PeriodSalesResponse getPeriodSales(
            @RequestParam(required = true) Integer salesPartnerID,
            @RequestParam(required = true) Integer productID,
            @RequestParam(required = true) String recordsFrom,
            @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date purchaseFromDate,
            @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date purchaseToDate,
            HttpSession session
    ) throws ExpiredAuthTokenException {

        return kapschService.getPeriodSales(salesPartnerID, productID, recordsFrom,
                purchaseFromDate, purchaseToDate, session);
    }
    // TODO DO WE NEED THESE? Or the whole controller? @Fifo
//	@PostMapping("/register")
//	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
//	public VignetteRegistrationResponse registerVignette(@RequestBody VignetteRegistrationRequest request,
//            HttpSession session) throws ExpiredAuthTokenException {
//
//        return kapschService.registerVignette(request, session);
//	}
//
//    @PostMapping("/activate")
//	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
//    public VignetteRegistrationResponse activateVignette(@RequestBody VignetteActivationRequest request,
//            HttpSession session) throws ExpiredAuthTokenException {
//
//        return (VignetteRegistrationResponse)kapschService.activateVignette(request, session);
//    }
}
