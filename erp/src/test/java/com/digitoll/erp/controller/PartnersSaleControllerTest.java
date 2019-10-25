package com.digitoll.erp.controller;

import com.digitoll.commons.configuration.CustomAuthenticationEntryPoint;
import com.digitoll.commons.dto.VignetteIdDTO;
import com.digitoll.commons.exception.CustomExceptionHandler;
import com.digitoll.commons.model.User;
import com.digitoll.commons.request.SaleRequest;
import com.digitoll.commons.response.SaleDTO;
import com.digitoll.commons.response.SaleRowDTO;
import com.digitoll.erp.configuration.SecurityConfig;
import com.digitoll.erp.service.SaleReportService;
import com.digitoll.erp.service.SaleService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;

import javax.servlet.http.HttpSession;
import java.security.Principal;

import static com.digitoll.erp.utils.ErpTestHelper.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {
        PartnersSaleController.class,
        CustomExceptionHandler.class,
        SecurityConfig.class,
        CustomAuthenticationEntryPoint.class
})
@WithMockUser(roles = "C2", username = PRINCIPAL_NAME)
public class PartnersSaleControllerTest extends SaleControllerTestBase {

    @MockBean
    private SaleService saleService;

    @MockBean
    private SaleReportService saleReportService;

    private Principal principal;

    @Before
    public void setup() {
        principal = erpTestHelper.createPrincipal();
    }

    @Test
    public void test_purchaseVignetteWithPartnerPosPartner_success() throws Exception {

        SaleRequest saleRequest = erpTestHelper.createSaleRequest();
        SaleDTO saleDTO = erpTestHelper.createSaleDTO();
        User user = erpTestHelper.createUser();

        when(userService.getUserDetails(any(String.class))).thenReturn(user);
        when(saleService.createSaleWithPartnersPos(any(SaleRequest.class), eq(user), any(HttpSession.class)))
                .thenReturn(saleDTO);

        verifySale(
            mvc.perform(post("/partners/sale")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(saleRequest)).principal(principal))
                .andExpect(status().isOk())
        );
    }

    @Test
    public void test_purchaseVignetteWithPartnerPosPartner_noPos_success() throws Exception {

        SaleRequest saleRequest = erpTestHelper.createSaleRequest();
        saleRequest.setPosId(null);

        SaleDTO saleDTO = erpTestHelper.createSaleDTO();
        User user = erpTestHelper.createUser();

        when(userService.getUserDetails(any(String.class))).thenReturn(user);
        when(saleService.createSale(any(SaleDTO.class), eq(user), any(HttpSession.class)))
                .thenReturn(saleDTO);

        verifySale(
            mvc.perform(post("/partners/sale")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(saleRequest)).principal(principal))
                .andExpect(status().isOk())
        );
    }

    @Test
    public void test_purchaseVignetteWithPartnerPosPartner_isBadRequest() throws Exception {

        mvc.perform(post("/partners/sale")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void test_activateSale_success() throws Exception {

        SaleDTO saleDTO = erpTestHelper.createSaleDTO();

        when(saleService.activateSaleBySaleId(eq(SALE_ID), any(HttpSession.class))).thenReturn(saleDTO);

        verifySale(
            mvc.perform(post("/partners/sale/activate/" + SALE_ID)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
        );
    }

    @Test
    public void test_activateSaleByVignetteId_success() throws Exception {

        VignetteIdDTO vignetteIdDTO = new VignetteIdDTO();
        vignetteIdDTO.setPosId(POS_ID);
        vignetteIdDTO.setVignetteId(VIGNETTE_ID);

        SaleRowDTO saleRowDTO = erpTestHelper.createSaleRowDTO();

        when(saleService.activateSaleByVignetteId(refEq(vignetteIdDTO), any(HttpSession.class))).thenReturn(saleRowDTO);

        verifySaleRow(
            mvc.perform(post("/partners/sale/activate/vignette")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vignetteIdDTO)))
                .andExpect(status().isOk())
        );

    }

    @Test
    public void test_activateSaleByVignetteId_IsBadRequest() throws Exception {

        mvc.perform(post("/partners/sale/activate/vignette")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void test_activateSaleByVignetteId_noVignetteId_IsBadRequest() throws Exception {

        VignetteIdDTO vignetteIdDTO = new VignetteIdDTO();
        vignetteIdDTO.setPosId(POS_ID);

        mvc.perform(post("/partners/sale/activate/vignette")
                .principal(principal)
                .content(objectMapper.writeValueAsString(vignetteIdDTO))
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void test_getSaleRowByVignetteId_success() throws Exception {

        SaleRowDTO saleRowDTO = erpTestHelper.createSaleRowDTO();

        when(saleReportService.getSaleRowByVignetteId(eq(VIGNETTE_ID), any(HttpSession.class)))
                .thenReturn(saleRowDTO);

        verifySaleRow(
            mvc.perform(get("/partners/sale/vignette/" + VIGNETTE_ID)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
        );
    }

    @Test
    public void test_getSaleBySaleId_success() throws Exception {

        SaleDTO saleDTO = erpTestHelper.createSaleDTO();

        when(saleReportService.getSaleById(eq(SALE_ID), any(HttpSession.class)))
                .thenReturn(saleDTO);

        verifySale(
            mvc.perform(get("/partners/sale/" + SALE_ID)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
        );
    }

    private void verifySale(ResultActions resultActions) throws Exception {
        verifySaleRow(
            resultActions
                .andExpect(jsonPath("$.id").value(SALE_ID))
                .andExpect(jsonPath("$.active").value(ACTIVE))
                .andExpect(jsonPath("$.total").value(TOTAL))
                .andExpect(jsonPath("$.createdOn").value(CREATED_ON))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.saleRows", hasSize(1)))
        , true);
    }

    private static void verifySaleRow(ResultActions resultActions) throws Exception {
        verifySaleRow(resultActions, false);
    }

    private static void verifySaleRow(ResultActions resultActions, boolean isArray) throws Exception {

        String firstElement = isArray ? ".saleRows[0]" : "";

        resultActions
            .andExpect(jsonPath("$" + firstElement + ".remoteClientId").value(nullValue()))
            .andExpect(jsonPath("$" + firstElement + ".active").value(ACTIVE))
            .andExpect(jsonPath("$" + firstElement + ".createdOn").value(CREATED_ON))
            .andExpect(jsonPath("$" + firstElement + ".kapschProperties.id").value(VIGNETTE_ID))
            .andExpect(jsonPath("$" + firstElement + ".kapschProperties.status").value(VIGNETTE_STATUS_ACTIVE))
            .andExpect(jsonPath("$" + firstElement + ".kapschProperties.product.id").value(PRODUCT_ID))
            .andExpect(jsonPath("$" + firstElement + ".kapschProperties.product.vehicleType").value(VEHICLE_TYPE_HGVN3.toString()))
            .andExpect(jsonPath("$" + firstElement + ".kapschProperties.product.emissionClass").value(EMISSION_CLASS.toString()))
            .andExpect(jsonPath("$" + firstElement + ".kapschProperties.product.validityType").value(VIGNETTE_VALIDITY_TYPE.toString()))
            .andExpect(jsonPath("$" + firstElement + ".kapschProperties.vehicle.lpn").value(LPN))
            .andExpect(jsonPath("$" + firstElement + ".kapschProperties.vehicle.countryCode").value(COUNTRY_CODE))
            .andExpect(jsonPath("$" + firstElement + ".kapschProperties.validity.requestedValidityStartDate").value(VALIDITY_START_DATE_PARAM))
            .andExpect(jsonPath("$" + firstElement + ".kapschProperties.validity.validityStartDateTimeUTC").value(VALIDITY_START_DATE_PARAM))
            .andExpect(jsonPath("$" + firstElement + ".kapschProperties.validity.validityEndDateTimeUTC").value(VALIDITY_START_DATE_PARAM))
            .andExpect(jsonPath("$" + firstElement + ".kapschProperties.validity.validityEndDateTimeEET").value(VALIDITY_START_DATE_PARAM))
            .andExpect(jsonPath("$" + firstElement + ".kapschProperties.validity.validityStartDateTimeEET").value(VALIDITY_START_DATE_PARAM))
            .andExpect(jsonPath("$" + firstElement + ".kapschProperties.price.amount").value(TOTAL))
            .andExpect(jsonPath("$" + firstElement + ".kapschProperties.price.currency").value(CURRENCY.toString()))
            .andExpect(jsonPath("$" + firstElement + ".kapschProperties.purchase.purchaseDateTimeUTC").value(VALIDITY_START_DATE_PARAM))
        ;
    }
}