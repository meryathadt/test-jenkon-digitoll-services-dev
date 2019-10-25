package com.digitoll.erp.controller;

import com.digitoll.commons.response.PaginatedRowsResponse;
import com.digitoll.commons.response.SaleRowDTO;
import com.digitoll.erp.service.UserService;
import com.digitoll.erp.utils.ErpTestHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.digitoll.erp.utils.ErpTestHelper.*;
import static org.hamcrest.Matchers.hasSize;

public class SaleControllerTestBase {

    protected SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    protected SimpleDateFormat modelFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    @MockBean
    protected UserService userService;

    @Autowired
    protected ObjectMapper objectMapper;

    protected ErpTestHelper erpTestHelper = new ErpTestHelper();

    @Autowired
    protected MockMvc mvc;

    protected void verifySalesDTO(ResultActions resultActions) throws Exception {
        verifySaleRowElement(resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.userId")
                .value(USER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.posId")
                        .value(POS_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.active")
                        .value(ACTIVE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdOn")
                        .value(CREATED_ON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email")
                        .value(EMAIL))
                .andExpect(MockMvcResultMatchers.jsonPath("$.active")
                        .value(ACTIVE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.bankTransactionId")
                        .value(BANK_TRANSACTION_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.companyCity")
                        .value(COMPANY_CITY))
                .andExpect(MockMvcResultMatchers.jsonPath("$.companyCountry")
                        .value(COMPANY_COUNTRY))
                .andExpect(MockMvcResultMatchers.jsonPath("$.companyIdNumber")
                        .value(COMPANY_ID_NUMBER))
                .andExpect(MockMvcResultMatchers.jsonPath("$.companyName")
                        .value(COMPANY_NAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$.companyStreet")
                        .value(COMPANY_STREET))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdOn")
                        .value(CREATED_ON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email")
                        .value(EMAIL))
                .andExpect(MockMvcResultMatchers.jsonPath("$.failed")
                        .value(FAILED))
                .andExpect(MockMvcResultMatchers.jsonPath("$.failureMessage")
                        .value(FAILURE_MESSAGE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.language")
                        .value(LANGUAGE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.names")
                        .value(NAMES))
                .andExpect(MockMvcResultMatchers.jsonPath("$.partnerName")
                        .value(PARTNER_NAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$.posName")
                        .value(POS_NAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleSeq")
                        .value(SALE_SEQ))
                .andExpect(MockMvcResultMatchers.jsonPath("$.total")
                        .value(TOTAL))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userName")
                        .value(USERNAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId")
                        .value(USER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.vatId")
                        .value(VAT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.failureMessage")
                        .value(FAILURE_MESSAGE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows", hasSize(1))));
    }

    protected  void verifySaleRowElement(ResultActions resultActions) throws Exception {
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].kapschProductId")
                .value(KAPSCH_PRODUCT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].lpn")
                        .value(LPN))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].partnerId")
                        .value(PARTNER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].email")
                        .value(EMAIL))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].failedKapschTrans")
                        .value(FAILED_KAPCSH_TRANS))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].kapschProductId")
                        .value(KAPSCH_PRODUCT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].activationDate")
                        .value(ACTIVATION_DATE.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].vehicle.lpn")
                        .value(LPN))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].vehicle.countryCode")
                        .value(COUNTRY_CODE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].active")
                        .value(ACTIVE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].createdOn")
                        .value(CREATED_ON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].failedKapschTrans")
                        .value(FAILED_KAPCSH_TRANS))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].failureMessage")
                        .value(FAILURE_MESSAGE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].posId")
                        .value(POS_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].activationDate")
                        .value(ACTIVATION_DATE.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].saleId")
                        .value(SALE_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].saleSequence")
                        .value(SALE_SEQUENCE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].userName")
                        .value(USERNAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].vignetteId")
                        .value(VIGNETTE_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].validityType")
                        .value(VIGNETTE_VALIDITY_TYPE.name()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].validityStartDate")
                        .value(VALIDITY_START_DATE.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].validityEndDate")
                        .value(VALIDITY_END_DATE.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].userId")
                        .value(USER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].posName")
                        .value(POS_NAME))

                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].price.currency")
                        .value(CURRENCY.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].price.amount")
                        .value(TOTAL))

                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].kapschProperties.id")
                        .value(VIGNETTE_ID))

                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].kapschProperties.price.amount")
                        .value(TOTAL))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].kapschProperties.price.currency")
                        .value(CURRENCY.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].kapschProperties.product.emissionClass")
                        .value(EMISSION_CLASS.name()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].kapschProperties.product.id")
                        .value(PRODUCT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].kapschProperties.product.validityType")
                        .value(VIGNETTE_VALIDITY_TYPE.name()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].kapschProperties.product.vehicleType")
                        .value(VEHICLE_TYPE_HGVN3.name()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].productsResponse.categoryDescriptionText")
                        .value(CATEGORY_DESCRIPTION))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].productsResponse.emissionClassText")
                        .value(EMISSION_CLASS_TEXT))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].productsResponse.validityTypeText")
                        .value(VIGNETTE_VALIDITY_TYPE_TEXT))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].productsResponse.validityType")
                        .value(VIGNETTE_VALIDITY_TYPE.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].productsResponse.emissionClass")
                        .value(EMISSION_CLASS.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].productsResponse.id")
                        .value(PRODUCT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].productsResponse.price.amount")
                        .value(TOTAL))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleRows[0].productsResponse.price.currency")
                        .value(CURRENCY.toString()));
    }

    protected  void verifySaleRowDTO(ResultActions andExpect) throws Exception {
        andExpect.andExpect(MockMvcResultMatchers.jsonPath("$.lpn")
                .value(LPN))
                .andExpect(MockMvcResultMatchers.jsonPath("$.partnerId")
                        .value(PARTNER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email")
                        .value(EMAIL))
                .andExpect(MockMvcResultMatchers.jsonPath("$.failedKapschTrans")
                        .value(FAILED_KAPCSH_TRANS))
                .andExpect(MockMvcResultMatchers.jsonPath("$.kapschProductId")
                        .value(KAPSCH_PRODUCT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.activationDate")
                        .value(ACTIVATION_DATE.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.vehicle.lpn")
                        .value(LPN))
                .andExpect(MockMvcResultMatchers.jsonPath("$.vehicle.countryCode")
                        .value(COUNTRY_CODE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.active")
                        .value(ACTIVE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdOn")
                        .value(CREATED_ON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.failedKapschTrans")
                        .value(FAILED_KAPCSH_TRANS))
                .andExpect(MockMvcResultMatchers.jsonPath("$.failureMessage")
                        .value(FAILURE_MESSAGE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.posId")
                        .value(POS_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.activationDate")
                        .value(ACTIVATION_DATE.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleId")
                        .value(SALE_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.saleSequence")
                        .value(SALE_SEQUENCE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userName")
                        .value(USERNAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$.vignetteId")
                        .value(VIGNETTE_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.validityType")
                        .value(VIGNETTE_VALIDITY_TYPE.name()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.validityStartDate")
                        .value(VALIDITY_START_DATE.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.validityEndDate")
                        .value(VALIDITY_END_DATE.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId")
                        .value(USER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.posName")
                        .value(POS_NAME))

                .andExpect(MockMvcResultMatchers.jsonPath("$.price.currency")
                        .value(CURRENCY.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price.amount")
                        .value(TOTAL))

                .andExpect(MockMvcResultMatchers.jsonPath("$.kapschProperties.id")
                        .value(VIGNETTE_ID))

                .andExpect(MockMvcResultMatchers.jsonPath("$.kapschProperties.price.amount")
                        .value(TOTAL))
                .andExpect(MockMvcResultMatchers.jsonPath("$.kapschProperties.price.currency")
                        .value(CURRENCY.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.kapschProperties.product.emissionClass")
                        .value(EMISSION_CLASS.name()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.kapschProperties.product.id")
                        .value(PRODUCT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.kapschProperties.product.validityType")
                        .value(VIGNETTE_VALIDITY_TYPE.name()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.kapschProperties.product.vehicleType")
                        .value(VEHICLE_TYPE_HGVN3.name()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.productsResponse.categoryDescriptionText")
                        .value(CATEGORY_DESCRIPTION))
                .andExpect(MockMvcResultMatchers.jsonPath("$.productsResponse.emissionClassText")
                        .value(EMISSION_CLASS_TEXT))
                .andExpect(MockMvcResultMatchers.jsonPath("$.productsResponse.validityTypeText")
                        .value(VIGNETTE_VALIDITY_TYPE_TEXT))
                .andExpect(MockMvcResultMatchers.jsonPath("$.productsResponse.validityType")
                        .value(VIGNETTE_VALIDITY_TYPE.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.productsResponse.emissionClass")
                        .value(EMISSION_CLASS.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.productsResponse.id")
                        .value(PRODUCT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.productsResponse.price.amount")
                        .value(TOTAL))
                .andExpect(MockMvcResultMatchers.jsonPath("$.productsResponse.price.currency")
                        .value(CURRENCY.toString()));
    }

    protected PageRequest createPageRequest() {
        Sort sort = Sort.by(SORTING_DIRECTION, SORT_PROPERTY);
        PageRequest pageRequest = PageRequest.of(PAGE, SIZE, sort);

        return pageRequest;
    }

    protected PaginatedRowsResponse createPaginatedRowsResponse() throws ParseException {
        PaginatedRowsResponse response = new PaginatedRowsResponse();

        List<SaleRowDTO> saleRows = new ArrayList<>(1);
        SaleRowDTO saleRowDTO = erpTestHelper.createSaleRowDTO();
        saleRows.add(saleRowDTO);

        response.setSaleRows(saleRows);
        response.setTotalElements(TOTAL_ELEMENTS);
        response.setTotalPages(TOTAL_PAGES);

        return response;
    }
}
