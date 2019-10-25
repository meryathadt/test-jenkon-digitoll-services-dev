package com.digitoll.erp.controller;

import com.digitoll.commons.dto.TransactionIdDTO;
import com.digitoll.commons.dto.VignetteIdDTO;
import com.digitoll.commons.model.*;
import com.digitoll.commons.request.SaleRequest;
import com.digitoll.commons.response.PaginatedRowsResponse;
import com.digitoll.commons.response.SaleDTO;
import com.digitoll.commons.response.SaleRowDTO;
import com.digitoll.erp.service.SaleReportService;
import com.digitoll.erp.service.SaleService;
import com.digitoll.erp.service.UserService;
import com.digitoll.erp.utils.ErpTestHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.digitoll.erp.utils.ErpTestHelper.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest( secure = false)
@ContextConfiguration(classes = {
        SaleController.class
})
public class SaleControllerTest extends SaleControllerTestBase {
    @MockBean
    protected Principal principal;

    @MockBean
    private SaleService saleService;

    @Test
    public void testPurchaseVignette()
            throws Exception {
        when(principal.getName()).thenReturn(PRINCIPAL_NAME);

        SaleRequest saleRequest = erpTestHelper.createSaleRequest();

        User user = erpTestHelper.createUser();
        when(userService.getUserDetails(any(String.class))).thenReturn(user);
        saleRequest.setUserId(user.getId());
        SaleDTO saleDTO = new SaleDTO(saleRequest);

        when(saleService.createSale(refEq(saleDTO, "createdOn"), eq(user), any(HttpSession.class)))
                .thenReturn(saleDTO);

        mvc.perform(post("/sale")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(saleRequest)).principal(principal))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId")
                        .value(USER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.posId")
                        .value(POS_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows", hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].email")
                        .value(EMAIL))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].kapschProductId")
                        .value(KAPSCH_PRODUCT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].activationDate")
                        .value(ACTIVATION_DATE.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].vehicle.lpn")
                        .value(LPN))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].vehicle.countryCode")
                        .value(COUNTRY_CODE));
    }

    @Test
    public void testPurchaseVignetteStatusIsBadRequest() throws Exception {

        mvc.perform(post("/sale")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testPurchaseVignetteWithPartnerPos()
            throws Exception {
        when(principal.getName()).thenReturn(PRINCIPAL_NAME);

        SaleRequest saleRequest = erpTestHelper.createSaleRequest();
        User user = erpTestHelper.createUser();
        saleRequest.setUserId(USER_ID);

        SaleDTO saleDTO = new SaleDTO(saleRequest);

        when(userService.getUserDetails(principal.getName())).thenReturn(user);
        when(saleService.createSaleWithPartnersPos(eq(saleRequest), eq(user), any(HttpSession.class)))
                .thenReturn(saleDTO);

        mvc.perform(post("/sale/pos")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(saleRequest)).principal(principal))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId")
                        .value(USER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.posId")
                        .value(POS_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows", hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].email")
                        .value(EMAIL))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].kapschProductId")
                        .value(KAPSCH_PRODUCT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].activationDate")
                        .value(ACTIVATION_DATE.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].vehicle.lpn")
                        .value(LPN))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].vehicle.countryCode")
                        .value(COUNTRY_CODE));
    }

    @Test
    public void testPurchaseVignetteWithPartnerPosStatusIsBadRequest() throws Exception {
        mvc.perform(post("/sale/pos")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testActivateSale() throws Exception {
        SaleDTO saleDTO = erpTestHelper.createSaleDTO();

        when(saleService.activateSaleBySaleId(eq(ID), any(HttpSession.class))).thenReturn(saleDTO);

        verifySalesDTO(mvc.perform(post("/sale/activate/"+ID)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()));
        ;
    }

    @Test
    public void testActivateSaleByVignetteId() throws Exception {
        VignetteIdDTO vignetteIdDTO = new VignetteIdDTO();
        vignetteIdDTO.setPosId(POS_ID);
        vignetteIdDTO.setVignetteId(VIGNETTE_ID);

        SaleRowDTO saleRowDTO = erpTestHelper.createSaleRowDTO();

        when(saleService.activateSaleByVignetteId(refEq(vignetteIdDTO), any(HttpSession.class))).thenReturn(saleRowDTO);

        verifySaleRowDTO(mvc.perform(post("/sale/activate/vignetteId")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vignetteIdDTO)))
                .andExpect(status().isOk()));

    }

    @Test
    public void testActivateSaleByVignetteIdStatusIsBadRequest() throws Exception {
        mvc.perform(post("/sale/activate/vignetteId")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testActivateSaleByTransactionId() throws Exception {
        SaleDTO saleDTO = erpTestHelper.createSaleDTO();

        TransactionIdDTO transactionIdDTO = erpTestHelper.createTransactionIdDTO();

        when(saleService.activateSaleByTransactionId(eq(TRANSACTION_ID), any(HttpSession.class)))
                .thenReturn(saleDTO);

        verifySalesDTO(mvc.perform(post("/sale/activate/trans")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionIdDTO)))
                .andExpect(status().isOk()));
    }

    @Test
    public void testActivateSaleByTransactionIdStatusIsBadRequest() throws Exception {

        mvc.perform(post("/sale/activate/trans")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateSaleTransactionId() throws Exception {
        TransactionIdDTO transactionIdDTO = erpTestHelper.createTransactionIdDTO();

        Sale sale = erpTestHelper.createSale();

        when(saleService.updateSaleTransactionId(eq(SALE_ID), eq(TRANSACTION_ID))).thenReturn(sale);

        mvc.perform(put("/sale/trans")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionIdDTO)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.partnerName")
                        .value(PARTNER_NAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$.bankTransactionId")
                        .value(BANK_TRANSACTION_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.language")
                        .value(LANGUAGE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.names")
                        .value(NAMES))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email")
                        .value(EMAIL))
                .andExpect(MockMvcResultMatchers.jsonPath("$.active")
                        .value(ACTIVE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdOn")
                        .value(CREATED_ON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.failureMessage")
                        .value(FAILURE_MESSAGE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.posId")
                        .value(POS_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.posName")
                        .value(POS_NAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleSeq")
                        .value(SALE_SEQ))
                .andExpect(MockMvcResultMatchers.jsonPath("$.posName")
                        .value(POS_NAME));
    }


    @Test
    public void testUpdateSaleTransactionIdStatusIsBadRequest() throws Exception {

        mvc.perform(put("/sale/trans")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testPurchaseVignetteFromSite()
            throws Exception {

        SaleDTO saleDTO = erpTestHelper.createSaleDTO();
        saleDTO.setUserId(null);
        saleDTO.setUserName(null);
        saleDTO.setEmail(null);

        SaleDTO returnedSaleDTO = erpTestHelper.createSaleDTO();

        when(saleService.populateSiteUserAndCreateSale(eq(saleDTO),
                any(HttpSession.class)))
                .thenReturn(returnedSaleDTO);

        verifySalesDTO(mvc.perform(post("/sale/site")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(saleDTO)))
                .andExpect(status().isOk()));
    }

    @Test
    public void testPurchaseVignetteFromSiteStatusIsBadRequest() throws Exception {

        mvc.perform(post("/sale/site")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest());
    }
}