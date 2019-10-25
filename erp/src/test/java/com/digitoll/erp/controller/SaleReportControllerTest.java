package com.digitoll.erp.controller;

import com.digitoll.commons.aggregation.AggregatedResults;
import com.digitoll.commons.dto.SaleAggregationDTO;
import com.digitoll.commons.enumeration.DateGroupingBases;
import com.digitoll.commons.request.AggregationRequest;
import com.digitoll.commons.response.PaginatedRowsResponse;
import com.digitoll.commons.response.SaleDTO;
import com.digitoll.commons.response.SaleRowDTO;
import com.digitoll.erp.component.PdfComponent;
import com.digitoll.erp.service.SaleReportService;
import com.digitoll.erp.utils.ErpTestHelper;
import org.apache.http.auth.BasicUserPrincipal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.digitoll.erp.utils.ErpTestHelper.*;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(PowerMockRunner.class)
@WebMvcTest( secure = false)
@ContextConfiguration(classes = {
        SaleReportController.class
})
@PrepareForTest({SaleReportController.class,
                 PdfComponent.class,
                 Files.class})
@PowerMockRunnerDelegate(SpringRunner.class)
public class SaleReportControllerTest extends SaleControllerTestBase {
    public static final String STATUS = "status";
    @MockBean
    private SaleReportService saleReportService;

    private static final boolean SHOW_TOTAL_SUM = true;
    private static final int NO_CATEGORY = 0;
    private static final String REMOTE_CLIENT_ID = "remoteClientId";

    private static final byte[] PDF_CONTENTS = "pdfContents".getBytes();

    @Test
    public void testGetSalesByCriteria() throws Exception {
        Principal principal = new BasicUserPrincipal(PRINCIPAL_NAME);
        PaginatedRowsResponse response = createPaginatedRowsResponse();

        PageRequest pageRequest = createPageRequest();

        when(saleReportService.getSalesByCriteria(
                modelFormatter.parse(VALIDITY_START_DATE),
                modelFormatter.parse(VALIDITY_END_DATE),
                LPN,
                PARTNER_ID,
                POS_ID,
                VIGNETTE_ID,
                SALE_ID,
                VEHICLE_ID,
                USER_ID,
                PARTNER_NAME,
                POS_NAME,
                USERNAME,
                VIGNETTE_VALIDITY_TYPE,
                EMAIL,
                ACTIVE,
                modelFormatter.parse(CREATED_ON),
                formatter.parse(FROM_DATE),
                formatter.parse(TO_DATE),
                formatter.parse(FROM_DATE),
                formatter.parse(TO_DATE),
                REMOTE_CLIENT_ID,
                pageRequest,
                SHOW_TOTAL_SUM,
                NO_CATEGORY,
                PRINCIPAL_NAME
        )).thenReturn(response);

        verifySaleRowElement(mvc.perform(get("/sales/filter")
                .param("page_number", String.valueOf(PAGE))
                .param("page_size", String.valueOf(SIZE))
                .param("sorting_parameter", SORT_PROPERTY)
                .param("sorting_direction", SORTING_DIRECTION.name())
                .param("vignette_id", VIGNETTE_ID)
                .param("vehicle_id", VEHICLE_ID)
                .param("partner_id", PARTNER_ID)
                .param("pos_id", POS_ID)
                .param("sale_id", SALE_ID)
                .param("user_id", USER_ID)
                .param("partner_name", PARTNER_NAME)
                .param("pos_name", POS_NAME)
                .param("user_name", USERNAME)
                .param("lpn", LPN)
                .param("email", EMAIL)
                .param("validity_type", VIGNETTE_VALIDITY_TYPE.name())
                .param("is_active", String.valueOf(ACTIVE))
                .param("validity_start_date", VALIDITY_START_DATE_PARAM)
                .param("validity_end_date", VALIDITY_END_DATE_PARAM)
                .param("created_on", CREATED_ON_PARAM)
                .param("from_date", FROM_DATE)
                .param("to_date", TO_DATE)
                .param("from_activation_date", FROM_DATE)
                .param("to_activation_date", TO_DATE)
                .param("remote_client_id", REMOTE_CLIENT_ID)
                .param("show_total_sum", String.valueOf(SHOW_TOTAL_SUM))
                .param("category", String.valueOf(NO_CATEGORY))
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements")
                        .value(TOTAL_ELEMENTS))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages")
                        .value(TOTAL_PAGES)));
    }

    @Test
    public void testGeneratePdf() throws Exception {
        PowerMockito.mockStatic(Files.class);
        PowerMockito.mockStatic(PdfComponent.class);

        File file = mock(File.class);
        Path path = mock(Path.class);

        when(file.toPath()).thenReturn(path);

        when(saleReportService.generatePdf(eq(VIGNETTE_ID), any(HttpSession.class))).thenReturn(file);

        PowerMockito.when(Files.readAllBytes(path)).thenReturn(PDF_CONTENTS);

        mvc.perform(get("/sale/pdf").param("vignette_id", VIGNETTE_ID))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().string(new String(PDF_CONTENTS)))
                .andExpect(header().string("Content-Disposition",
                        "form-data; name=\"eVignette Receipt - "+VIGNETTE_ID+".pdf\"; filename=\"eVignette Receipt - "+
                                VIGNETTE_ID+".pdf\""));

        PowerMockito.verifyStatic(PdfComponent.class, times(1));
        PdfComponent.deleteFile(file);
    }

    @Test
    public void testGetSaleRowByVignetteId() throws Exception {
        SaleRowDTO saleRowDTO = erpTestHelper.createSaleRowDTO();
        when(saleReportService.getSaleRowByVignetteId(eq(VIGNETTE_ID), any(HttpSession.class)))
                .thenReturn(saleRowDTO);

        verifySaleRowDTO(mvc.perform(get("/sale/vignetteId")
                .param("vignette_id", VIGNETTE_ID)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()));
    }

    @Test
    public void testAggregateReport() throws Exception {

        Principal principal = new BasicUserPrincipal(PRINCIPAL_NAME);
        AggregatedResults aggregatedResults = erpTestHelper.createAggregatedResults();

        when(saleReportService.aggregateReport(any(AggregationRequest.class), any(PageRequest.class), anyString(),
                any(), any(), any())).thenReturn(aggregatedResults);

        System.out.println(objectMapper.writeValueAsString(aggregatedResults));
        mvc.perform(post("/sales/aggregate")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aggregatedResults))
                .param("page_number", String.valueOf(PAGE))
                .param("page_size", String.valueOf(SIZE))
                .param("sorting_parameter", STATUS)
                .param("sorting_direction", SORTING_DIRECTION.name())
                .param("date_grouping_bases", DateGroupingBases.DAILY.name())
                .principal(principal))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[0].status").value(SaleAggregationDTO.ACTIVE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[0].purchaseDate").value(PURCHASE_DATE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[0].kapschProductId")
                        .value(KAPSCH_PRODUCT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[0].totalAmount")
                        .value(AMOUNT.bigDecimalValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[0].partnerName")
                        .value(PARTNER_NAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[0].productName")
                        .value(ErpTestHelper.DESCRIPTION))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[0].count")
                        .value(ErpTestHelper.AGGREGATED_COUNT))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[0].posName")
                        .value(POS_NAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[0].registrationDate")
                        .value(ErpTestHelper.PURCHASE_DATE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements")
                        .value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages")
                        .value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalSum")
                        .value(AMOUNT.bigDecimalValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount")
                        .value(AGGREGATED_COUNT));
    }

    @Test
    public void testGetSaleByTransactionId() throws Exception {
        SaleDTO saleDTO = erpTestHelper.createSaleDTO();
        when(saleReportService.getSaleByTransactionId(eq(TRANSACTION_ID), any(HttpSession.class)))
                .thenReturn(saleDTO);

        verifySalesDTO(mvc.perform(get("/sale")
                .param("transaction_id", TRANSACTION_ID)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()));
    }

    @Test
    public void testGetSaleByEmail() throws Exception {
        PaginatedRowsResponse response = createPaginatedRowsResponse();

        PageRequest pageRequest = createPageRequest();

        when(saleReportService.getSaleByEmail(EMAIL, pageRequest))
                .thenReturn(response);

        verifySaleRowElement(mvc.perform(get("/sale/email")
                .param("email", EMAIL)
                .param("page_number", String.valueOf(PAGE))
                .param("page_size", String.valueOf(SIZE))
                .param("sorting_parameter", SORT_PROPERTY)
                .param("sorting_direction", SORTING_DIRECTION.name())
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements")
                        .value(TOTAL_ELEMENTS))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages")
                        .value(TOTAL_PAGES)));
    }



    @Test
    public void testGetSales() throws Exception {

        PaginatedRowsResponse paginatedRowsResponse = createPaginatedRowsResponse();
        PageRequest pageRequest = createPageRequest();

        when(saleReportService.getSaleRowsForUser(USERNAME, pageRequest))
                .thenReturn(paginatedRowsResponse);

        verifySaleRowElement(mvc.perform(get("/sale/user/"+USERNAME)
                .param("user_name", USERNAME)
                .param("page_number", String.valueOf(PAGE))
                .param("page_size", String.valueOf(SIZE))
                .param("sorting_parameter", SORT_PROPERTY)
                .param("sorting_direction", SORTING_DIRECTION.name())
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements")
                        .value(TOTAL_ELEMENTS))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages")
                        .value(TOTAL_PAGES)));
    }

    @Test
    public void testGetSalesAdmin() throws Exception {

        PaginatedRowsResponse response = createPaginatedRowsResponse();
        when(saleReportService.getSalePages(createPageRequest())).thenReturn(response);

        verifySaleRowElement(mvc.perform(get("/sale/admin")
                .param("page_number", String.valueOf(PAGE))
                .param("page_size", String.valueOf(SIZE))
                .param("sorting_parameter", SORT_PROPERTY)
                .param("sorting_direction", SORTING_DIRECTION.name())
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements")
                        .value(TOTAL_ELEMENTS))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages")
                        .value(TOTAL_PAGES)));
    }

    @Test
    public void testRequiredFields() {
        Map<String/*endpoint*/, Set<String/*param*/>> requiredFields = new HashMap<>();

        Set<String> pdfRequiredParams = new HashSet<>();
        pdfRequiredParams.add("vignette_id");

        Set<String> filterRequiredParams = new HashSet<>();
        filterRequiredParams.add("page_number");
        filterRequiredParams.add("page_size");

        Set<String> emailRequiredParams = new HashSet<>();
        emailRequiredParams.add("email");
        emailRequiredParams.add("page_number");
        emailRequiredParams.add("page_size");

        Set<String> userRequiredParams = new HashSet<>();
        userRequiredParams.add("page_number");
        userRequiredParams.add("page_size");

        Set<String> adminRequiredParams = new HashSet<>();
        adminRequiredParams.add("page_number");
        adminRequiredParams.add("page_size");

        Set<String> vignetteIdRequiredParams = new HashSet<>();
        vignetteIdRequiredParams.add("vignette_id");

        Set<String> saleRequiredParams = new HashSet<>();
        saleRequiredParams.add("transaction_id");

        requiredFields.put("/sale/pdf", pdfRequiredParams);
        requiredFields.put("/sales/filter", filterRequiredParams);
        requiredFields.put("/sale/email", emailRequiredParams);
        requiredFields.put("/sale/user/"+USERNAME, userRequiredParams);
        requiredFields.put("/sale/admin", adminRequiredParams);
        requiredFields.put("/sale/vignetteId", vignetteIdRequiredParams);
        requiredFields.put("/sale", saleRequiredParams);

        requiredFields.forEach(this::checkRequiredFields);
    }

    private void checkRequiredFields(String endpoint, Set<String> requiredFields) {
        requiredFields.forEach((field)->{
            try {
                checkRequiredField(endpoint, requiredFields, field);
            } catch (Exception e) {
                e.printStackTrace();
                fail(e.getMessage());
            }
        });
    }

    private void checkRequiredField(String endpoint, Set<String> requiredFields, String field) throws Exception {
        Map<String, String> fieldValues = new HashMap<>();

        fieldValues.put("vignette_id", VIGNETTE_ID);
        fieldValues.put("transaction_id", TRANSACTION_ID);
        fieldValues.put("page_number", String.valueOf(PAGE_NUMBER));
        fieldValues.put("page_size", String.valueOf(PAGE_SIZE));
        fieldValues.put("username", USERNAME);
        fieldValues.put("email", EMAIL);

        Set<String> fieldsLeft = new HashSet<>(requiredFields);
        fieldsLeft.remove(field);

        MockHttpServletRequestBuilder requestBuilder = get(endpoint)
                .contentType(MediaType.APPLICATION_JSON);

        for (String fieldLeft: fieldsLeft) {
            requestBuilder = requestBuilder.param(fieldLeft, fieldValues.get(fieldLeft));
        }

        mvc.perform(requestBuilder)
                .andExpect(status().isBadRequest());
    }
}