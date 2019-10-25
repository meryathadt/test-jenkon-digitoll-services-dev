package com.digitoll.erp.controller;

import com.digitoll.commons.aggregation.AggregatedResults;
import com.digitoll.commons.enumeration.DateGroupingBases;
import com.digitoll.commons.enumeration.VignetteValidityType;
import com.digitoll.commons.exception.ExpiredAuthTokenException;
import com.digitoll.commons.exception.NoPosIdAssignedToUserException;
import com.digitoll.commons.exception.SaleRowIncompleteDataException;
import com.digitoll.commons.request.AggregationRequest;
import com.digitoll.commons.response.PaginatedRowsResponse;
import com.digitoll.commons.response.SaleDTO;
import com.digitoll.commons.response.SaleRowDTO;
import com.digitoll.erp.component.PdfComponent;
import com.digitoll.erp.service.SaleReportService;
import com.mongodb.lang.Nullable;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Principal;
import java.util.Date;

@RestController
public class SaleReportController {

    @Autowired
    private SaleReportService saleReportService;

    private static final Logger log = LoggerFactory.getLogger(SaleReportController.class);

    @CrossOrigin
    @PreAuthorize(
            "hasAuthority('ROLE_ADMIN') or " +
                    "hasAuthority('ROLE_C9') or " +
                    "hasAuthority('ROLE_PARTNER_ADMIN') or " +
                    "hasAuthority('ROLE_ACCOUNTANT') or " +
                    "hasAuthority('ROLE_SUPPORT_1')"
    )
    @GetMapping(value = "/sale/pdf", produces = "application/pdf")
    public ResponseEntity<byte[]> generatePdf(
            @RequestParam(value = "vignette_id", required = true)
            @ApiParam(name = "vignette_id", example = "Vignette Id, e.g. 19080743566037", required = true)
                    String vignetteId,
            HttpSession session) throws ExpiredAuthTokenException, IOException, SaleRowIncompleteDataException {

        File file = saleReportService.generatePdf(vignetteId, session);
        byte[] contents = Files.readAllBytes(file.toPath());
        PdfComponent.deleteFile(file);

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_PDF);

        String filename = "eVignette Receipt - " + vignetteId + ".pdf";

        headers.setContentDispositionFormData(filename, filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(contents, headers, HttpStatus.OK);
    }

    @CrossOrigin
    @PreAuthorize(
            "hasAuthority('ROLE_ADMIN') or " +
                    "hasAuthority('ROLE_PARTNER_ADMIN') or " +
                    "hasAuthority('ROLE_C2') or " +
                    "hasAuthority('ROLE_ACCOUNTANT') or " +
                    "hasAuthority('ROLE_SUPPORT_1')"
    )
    @GetMapping("/sales/filter")
    public PaginatedRowsResponse getSalesByCriteria(
            @RequestParam(value = "page_number")
            @ApiParam(name = "page_number", example = "Page number, e.g. 5", required = true)
                    Integer pageNumber,
            @RequestParam(value = "page_size")
            @ApiParam(name = "page_size", example = "Page size, e.g. 100", required = true)
                    Integer pageSize,
            @RequestParam(value = "sorting_parameter", required = false)
            @ApiParam(name = "sorting_parameter", example = "Sorting parameter, e.g. price.amount")
                    String sortingParameter,
            @RequestParam(value = "sorting_direction", required = false, defaultValue = "ASC")
            @ApiParam(name = "sorting_direction", example = "Sorting direction: ASC, DESC", defaultValue = "ASC")
                    Sort.Direction sortingDirection,
            @RequestParam(value = "vignette_id", required = false)
            @ApiParam(name = "vignette_id", example = "Vignette Id, e.g. 19080743566037")
                    String vignetteId,
            @RequestParam(value = "vehicle_id", required = false)
            @ApiParam(name = "vehicle_id", example = "Vehicle Id, e.g. 5d288d64a192f0dfe1f8a99a")
                    String vehicleId,
            @RequestParam(value = "partner_id", required = false)
            @ApiParam(name = "partner_id", example = "Partner Id, e.g. 5d288d64a192f0dfe1f8a99a")
                    String partnerId,
            @RequestParam(value = "pos_id", required = false)
            @ApiParam(name = "pos_id", example = "POS Id, e.g. 5d24550609cadc1fa8988508")
                    String posId,
            @RequestParam(value = "sale_id", required = false)
            @ApiParam(name = "sale_id", example = "Sale Id, e.g. 5d2893d0d00825000104103b")
                    String saleId,
            @RequestParam(value = "user_id", required = false)
            @ApiParam(name = "user_id", example = "User Id, e.g. 5d263939f0f430170b944b41")
                    String userId,
            @RequestParam(value = "partner_name", required = false)
            @ApiParam(name = "partner_name", example = "Partner name, e.g. Telenor")
                    String partnerName,
            @RequestParam(value = "pos_name", required = false)
            @ApiParam(name = "pos_name", example = "Pos name, e.g. Cash terminal")
                    String posName,
            @RequestParam(value = "user_name", required = false)
            @ApiParam(name = "user_name", example = "User name, e.g. cashterminal")
                    String userName,
            @RequestParam(value = "lpn", required = false)
            @ApiParam(name = "lpn", example = "License plate number, e.g. CA1234CA")
                    String lpn,
            @RequestParam(value = "email", required = false)
            @ApiParam(name = "email", example = "Email address, e.g. emal@email.com")
                    String email,
            @RequestParam(value = "validity_type", required = false)
            @ApiParam(name = "validity_type", example = "Validity type: week, weekend, month, quarter, year, day")
                    VignetteValidityType validityType,
            @RequestParam(value = "is_active", required = false)
            @ApiParam(name = "is_active", example = "Is vignette active: true or false")
                    Boolean active,
            @RequestParam(value = "validity_start_date", required = false)
            @ApiParam(name = "validity_start_date", example = "Validity start date in ISO format, e.g. 2019-08-31T09:00:00.000Z")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    Date validityStartDate,
            @RequestParam(value = "validity_end_date", required = false)
            @ApiParam(name = "validity_end_date", example = "Validity end date in ISO format, e.g. 2019-08-31T09:00:00.000Z")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    Date validityEndDate,
            @RequestParam(value = "created_on", required = false)
            @ApiParam(name = "created_on", example = "Created on date in ISO Format, e.g. 2019-08-06T00:00:00.000Z")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    Date createdOn,
            @RequestParam(value = "from_date", required = false)
            @ApiParam(name = "from_date", example = "From date to filter date of creation in ISO Format, e.g. 2019-08-06T00:00:00.000+0300")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    Date fromRegistrationDate,
            @RequestParam(value = "to_date", required = false)
            @ApiParam(name = "to_date", example = "To date to filter date of creation in ISO Format, e.g. 2019-08-06T00:00:00.000+0300")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    Date toRegistrationDate,
            @RequestParam(value = "from_activation_date", required = false)
            @ApiParam(name = "from_activation_date", example = "From date to filter date of eVignette activation in ISO Format, e.g. 2019-08-06T00:00:00.000+0300")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Nullable Date fromActivationDate,
            @RequestParam(value = "to_activation_date", required = false)
            @ApiParam(name = "to_activation_date", example = "To date to filter date of eVignette activation in ISO Format, e.g. 2019-08-06T00:00:00.000+0300")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Nullable Date toActivationDate,
            @RequestParam(value = "remote_client_id", required = false)
            @ApiParam(name = "remote_client_id", example = "Client id in the partners DB")
                    String remoteClientId,
            @RequestParam(value = "show_total_sum", required = false, defaultValue = "false")
            @ApiParam(name = "show_total_sum", example = "Should show total sales sum: true or false")
                    Boolean showTotalSum,
            @RequestParam(value = "category", required = false)
            @ApiParam(name = "category", example = "Vignette Category integer: 1,2,3")
                    Integer category,
            Principal principal) throws ExpiredAuthTokenException, NoPosIdAssignedToUserException {

        return saleReportService.getSalesByCriteria(
                validityStartDate,
                validityEndDate,
                lpn,
                partnerId,
                posId,
                vignetteId,
                saleId,
                vehicleId,
                userId,
                partnerName,
                posName,
                userName,
                validityType,
                email,
                active,
                createdOn,
                fromRegistrationDate,
                toRegistrationDate,
                fromActivationDate,
                toActivationDate,
                remoteClientId,
                getSortedPage(pageNumber, pageSize, sortingParameter, sortingDirection),
                showTotalSum,
                category,
                principal.getName()
        );
    }

    @CrossOrigin
    @PreAuthorize(
            "hasAuthority('ROLE_ADMIN') or " +
                    "hasAuthority('ROLE_C2') or " +
                    "hasAuthority('ROLE_PARTNER_ADMIN') or " +
                    "hasAuthority('ROLE_ACCOUNTANT') or " +
                    "hasAuthority('ROLE_SUPPORT_1')"
    )
    @GetMapping("/sale/vignetteId")
    public SaleRowDTO getSaleRowByVignetteId(
            @RequestParam(value = "vignette_id", required = true)
            @ApiParam(name = "vignette_id", example = "vignette id, e.g. 76767656")
                    String vignetteId,
            HttpSession session) throws ExpiredAuthTokenException {

        return saleReportService.getSaleRowByVignetteId(vignetteId, session);
    }

    @CrossOrigin
    @PreAuthorize(
            "hasAuthority('ROLE_ADMIN') or " +
                    "hasAuthority('ROLE_C9') or " +
                    "hasAuthority('ROLE_ACCOUNTANT') or " +
                    "hasAuthority('ROLE_SUPPORT_1')"
    )
    @GetMapping("/sale")
    public SaleDTO getSaleByTransactionId(
            @RequestParam(value = "transaction_id", required = true)
            @ApiParam(name = "transaction_id", example = "Sale id, e.g. 76767656")
                    String transactionId,
            HttpSession session) throws ExpiredAuthTokenException {

        return saleReportService.getSaleByTransactionId(transactionId, session);
    }

    @CrossOrigin
    @PreAuthorize(
            "hasAuthority('ROLE_ADMIN') or " +
                    "hasAuthority('ROLE_C9') or " +
                    "hasAuthority('ROLE_ACCOUNTANT') or " +
                    "hasAuthority('ROLE_SUPPORT_1')"
    )
    @GetMapping("/sale/email")
    public PaginatedRowsResponse getSaleByEmail(
            @RequestParam(value = "email", required = true)
            @ApiParam(name = "email", example = "Sale id, e.g. 76767656", required = true)
                    String email,
            @RequestParam(value = "page_number", required = true)
            @ApiParam(name = "page_number", example = "Page number, e.g. 5", required = true)
                    Integer pageNumber,
            @RequestParam(value = "page_size", required = true)
            @ApiParam(name = "page_size", example = "Page size, e.g. 100", required = true)
                    Integer pageSize,
            @RequestParam(value = "sorting_parameter", required = false)
            @ApiParam(name = "sorting_parameter", example = "Sorting parameter, e.g. price.amount")
                    String sortingParameter,
            @RequestParam(value = "sorting_direction", required = false, defaultValue = "ASC")
            @ApiParam(name = "sorting_direction", example = "Sorting direction: ASC, DESC", defaultValue = "ASC")
                    Sort.Direction sortingDirection) {
        return saleReportService.getSaleByEmail(email, getSortedPage(pageNumber, pageSize, sortingParameter, sortingDirection));
    }

    @CrossOrigin
    @PreAuthorize(
            "hasAuthority('ROLE_ADMIN') or " +
                    "hasAuthority('ROLE_C9') or " +
                    "hasAuthority('ROLE_ACCOUNTANT') or " +
                    "hasAuthority('ROLE_SUPPORT_1')"
    )
    @GetMapping("/sale/user/{user_name}")
    public PaginatedRowsResponse getSales(@PathVariable(value = "user_name", required = true)
                                          @ApiParam(name = "user_name", example = "User name, e.g. ala@bala.bg")
                                                  String userName,
                                          @RequestParam(value = "page_number", required = true)
                                          @ApiParam(name = "page_number", example = "Page number, e.g. 5", required = true)
                                                  Integer pageNumber,
                                          @RequestParam(value = "page_size", required = true)
                                          @ApiParam(name = "page_size", example = "Page size, e.g. 100", required = true)
                                                  Integer pageSize,
                                          @RequestParam(value = "sorting_parameter", required = false)
                                          @ApiParam(name = "sorting_parameter", example = "Sorting parameter, e.g. price.amount")
                                                  String sortingParameter,
                                          @RequestParam(value = "sorting_direction", required = false, defaultValue = "ASC")
                                          @ApiParam(name = "sorting_direction", example = "Sorting direction: ASC, DESC", defaultValue = "ASC")
                                                  Sort.Direction sortingDirection) {

        return saleReportService.getSaleRowsForUser(userName, getSortedPage(pageNumber, pageSize, sortingParameter, sortingDirection));
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/sale/admin")
    public PaginatedRowsResponse getSalesAdmin(
            @RequestParam(value = "page_number", required = true)
            @ApiParam(name = "page_number", example = "Page number, e.g. 5", required = true)
                    Integer pageNumber,
            @RequestParam(value = "page_size", required = true)
            @ApiParam(name = "page_size", example = "Page size, e.g. 100", required = true)
                    Integer pageSize,
            @RequestParam(value = "sorting_parameter", required = false)
            @ApiParam(name = "sorting_parameter", example = "Sorting parameter, e.g. price.amount")
                    String sortingParameter,
            @RequestParam(value = "sorting_direction", required = false, defaultValue = "ASC")
            @ApiParam(name = "sorting_direction", example = "Sorting direction: ASC, DESC", defaultValue = "ASC")
                    Sort.Direction sortingDirection) {

        return saleReportService.getSalePages(getSortedPage(pageNumber, pageSize, sortingParameter, sortingDirection));
    }

    @CrossOrigin
    @PreAuthorize(
            "hasAuthority('ROLE_ADMIN') or " +
                    "hasAuthority('ROLE_PARTNER_ADMIN') or " +
                    "hasAuthority('ROLE_C2') or " +
                    "hasAuthority('ROLE_ACCOUNTANT') or " +
                    "hasAuthority('ROLE_SUPPORT_1')"
    )
    @PostMapping("/sales/aggregate")
    public AggregatedResults aggregateReport(
            @RequestParam(value = "page_number")
            @ApiParam(name = "page_number", example = "Page number, e.g. 5", required = true)
                    Integer pageNumber,
            @RequestParam(value = "page_size")
            @ApiParam(name = "page_size", example = "Page size, e.g. 100", required = true)
                    Integer pageSize,
            @RequestParam(value = "sorting_parameter", required = false)
            @ApiParam(name = "sorting_parameter", example = "Sorting parameter, e.g. price.amount")
                    String[] sortingParameters,
            @RequestParam(value = "sorting_direction", required = false, defaultValue = "ASC")
            @ApiParam(name = "sorting_direction", example = "Sorting direction: ASC, DESC", defaultValue = "ASC")
                    Sort.Direction sortingDirection,
            @RequestBody
                    AggregationRequest aggregationRequest,
            @RequestParam(value = "date_grouping_bases", required = false, defaultValue = "DAILY")
            @ApiParam(name = "date_grouping_bases", example = "Grouping parameter: DAILY, MONTHLY, YEARLY", defaultValue = "DAILY")
                    DateGroupingBases dateGroupingBases,
            Principal principal) throws ExpiredAuthTokenException, NoPosIdAssignedToUserException {

        return saleReportService.aggregateReport(aggregationRequest, PageRequest.of(pageNumber, pageSize),
                principal.getName(), sortingParameters, sortingDirection, dateGroupingBases);
    }

    private PageRequest getSortedPage(Integer pageNumber, Integer pageSize, String sortingParameter, Sort.Direction sortingDirection) {
        PageRequest page;
        if (sortingParameter != null) {
            if (sortingDirection == Sort.Direction.ASC) {
                page = PageRequest.of(pageNumber, pageSize, Sort.by(sortingParameter));
            } else {
                page = PageRequest.of(pageNumber, pageSize, Sort.by(sortingParameter).descending());
            }
        } else {
            page = PageRequest.of(pageNumber, pageSize, Sort.by("createdOn").descending());
        }
        return page;
    }
}
