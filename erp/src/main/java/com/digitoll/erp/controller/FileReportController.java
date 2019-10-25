package com.digitoll.erp.controller;

import com.digitoll.commons.enumeration.VignetteValidityType;
import com.digitoll.commons.exception.ExpiredAuthTokenException;
import com.digitoll.commons.exception.NoPosIdAssignedToUserException;
import com.digitoll.erp.service.FilesReportService;
import com.mongodb.lang.Nullable;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

@RestController
public class FileReportController {

    @Autowired
    private FilesReportService filesReportService;

    // get sales aggregated by partner id
    // if you passPartnerId as arguement - the report will show only this  partner's aggregations
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping(value = "/file/report")
    public void getCsvReportForDate(
            @RequestParam(value = "from_date", required = false)
            @ApiParam(name = "from_date", example = "from date on date in ISO Format, e.g. 2019-08-06T00:00:00.000+0300")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    Date date,
            @RequestParam(value = "partner_id", required = false)
            @ApiParam(name = "partner_id", required = false, example = "Partner id, e.g. 76767656")
                    String partnerId,
            HttpServletResponse response)
            throws IOException, ExpiredAuthTokenException, ParseException {
        filesReportService.getCsvAggregatedForPartners(date, partnerId, response, null);
    }

    // get sales aggregated by partner id
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping(value = "/send/reports")
    public void sendReports(
            @RequestParam(value = "from_date", required = false)
            @ApiParam(name = "from_date", example = "from date on date in ISO Format, e.g. 2019-08-06T00:00:00.000+0300")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    Date date,
            Principal principal
    ) throws IOException, MessagingException, NoPosIdAssignedToUserException {
        String directoryName = "auto" + UUID.randomUUID().toString();

        File directory = new File(directoryName);
        try {
            filesReportService.sendReportToPartners(date, directory, principal.getName());
        } catch (Exception e) {
            // we want to catch all exceptions, runtime included and always delete dir
            throw e;
        } finally {
            FileUtils.deleteDirectory(directory);
        }
    }

    // Get daily report for sales for partners ( no aggregation, just sale rows )
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping(value = "/file/report/partners")
    public void getCsvForPartners(
            @RequestParam(value = "from_date", required = false)
            @ApiParam(name = "from_date", example = "from date on date in ISO Format, e.g. 2019-08-06T00:00:00.000+0300")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    Date date,
            @RequestParam(value = "partner_id", required = true)
            @ApiParam(name = "partner_id", required = true, example = "Partner id, e.g. 76767656")
                    String partnerId,
            Principal principal,
            HttpServletResponse response)
            throws IOException, ExpiredAuthTokenException, ParseException, NoPosIdAssignedToUserException {
        filesReportService.getCsvForPartnersDaily(date, partnerId, response, "", principal.getName());
    }

    // get sales aggregated by partner id
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping(value = "/file/report/aggregated")
    public void getAggregatedReport(
            @RequestParam(value = "from_date", required = false)
            @ApiParam(name = "from_date", example = "from date on date in ISO Format, e.g. 2019-08-06T00:00:00.000+0300")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    Date date,
            @RequestParam(value = "partner_id", required = false)
            @ApiParam(name = "partner_id", required = false, example = "Partner id, e.g. 76767656")
                    String partnerId,
            @RequestParam(value = "type", required = true)
            @ApiParam(name = "type", required = true, example = "daily / monthly")
                    String type,
            HttpServletResponse response)
            throws IOException {
        response.setContentType("text/csv;charset=utf-8");
        if (type.equals("monthly")) {
            filesReportService.getAggregatedReportForProductsTwiceMonthly(date, partnerId, response, null);
        } else {
            filesReportService.getAggregatedReportForProductsDaily(date, partnerId, response, null);
        }
    }

    @CrossOrigin
    @PreAuthorize(
            "hasAuthority('ROLE_ADMIN') or " +
                    "hasAuthority('ROLE_PARTNER_ADMIN') or " +
                    "hasAuthority('ROLE_C2') or " +
                    "hasAuthority('ROLE_ACCOUNTANT') or " +
                    "hasAuthority('ROLE_SUPPORT_1')"
    )
    @GetMapping("/file/report/export")
    public void getCsvExport(
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
            @ApiParam(name = "from_date", example = "from date on date in ISO Format, e.g. 2019-08-06T00:00:00.000+0300")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    Date fromRegistrationDate,
            @RequestParam(value = "to_date", required = false)
            @ApiParam(name = "to_date", example = "Created on date in ISO Format, e.g. 2019-08-06T00:00:00.000+0300")
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
            @RequestParam(value = "category", required = false)
            @ApiParam(name = "category", example = "Vignette Category integer: 1,2,3")
                    Integer category,
            Principal principal,
            HttpServletResponse response) throws ExpiredAuthTokenException, IOException, NoPosIdAssignedToUserException {

        response.setContentType("text/csv;charset=utf-8");
        filesReportService.getCsvExport(
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
                category,
                principal.getName(),
                response
        );
    }
}
