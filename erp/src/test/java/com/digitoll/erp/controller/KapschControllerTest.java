package com.digitoll.erp.controller;

import com.digitoll.commons.enumeration.EmissionClass;
import com.digitoll.commons.enumeration.VehicleType;
import com.digitoll.commons.enumeration.VignetteValidityType;
import com.digitoll.commons.kapsch.classes.EVignetteInventoryProduct;
import com.digitoll.commons.kapsch.response.ApiVersionResponse;
import com.digitoll.commons.kapsch.response.VignetteInventoryResponse;
import com.digitoll.commons.kapsch.response.c9.*;
import com.digitoll.commons.model.VignettePrice;
import com.digitoll.erp.service.KapschService;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest( secure = false)
@ContextConfiguration(classes = {
        KapschController.class
})
public class KapschControllerTest {

    public static final boolean OBSOLETE_VERSION = false;
    public static final boolean LATEST_VERSION = true;
    public static final BigDecimal VIGNETTE_PRICE = new BigDecimal("50.0");
    public static final Currency CURRENCY = Currency.getInstance("EUR");

    public static final String DESCTRIPTION = "description";
    public static final int STATE_ID = 7;
    public static final EmissionClass EMISSION_CLASS = EmissionClass.eur0;
    public static final VignetteValidityType VIGNETTE_VALIDITY_TYPE = VignetteValidityType.month;
    public static final VehicleType VEHICLE_TYPE = VehicleType.car;
    public static final int E_VIGNETTE_INVENTORY_PRODUCT_ID = 8;
    public static final String MONGO_ID = "mongoID";

    // Search test
    public static final Integer PAGE_NUMBER = 1;
    public static final Integer PAGE_SIZE = 2;
    public static final Integer LAST_RECORD = 3;
    public static final String SORTING_PARAMETER = "sortingParameter";
    public static final Sort.Direction SORTING_DIRECTION = Sort.Direction.ASC;
    public static final String E_VIGNETTE_ID = "7";
    public static final Integer SALES_PARTNER_ID = 4;
    public static final Integer PRODUCT_ID = 5;
    public static final String LPN = "lpn";
    public static final String COUNTRY_CODE = "countryCode";
    public static final Integer STATUS = 6;
    public static final String RECORDS_FROM = "recordsFrom";
    public static final boolean HAS_MORE_RECORDS = true;

    public static final String REQUESTED_VALIDITY_START_DATE = "2015-09-26T09:30:00.000+0000";
    public static final String VALIDITY_END_DATE_TIME_UTC = "2015-09-21T09:30:00.000";
    public static final String VALIDITY_START_DATE_TIME_UTC = "2015-09-26T09:30:00.000";

    public static final String PURCHASE_FROM_DATE = "2015-09-21T09:30:00.000+0000";
    public static final String PURCHASE_TO_DATE = "2015-09-26T09:30:00.000+0000";

    public static final String PURCHASE_DATE_TIME_UTC = "2015-09-16T09:30:00.000";
    public static final String POS_ID = "posId";

    public static final String LAST_UPDATE_FROM = "2015-09-23T09:30:00.000+0000";
    public static final String LAST_UPDATE_TO = "2015-09-03T09:30:00.000+0000";
    public static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public static final String SALES_PARTNER = "salesPartner";
    ////////////////

    @MockBean
    private KapschService kapschService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testReturnInt() throws Exception {
        ApiVersionResponse response = new ApiVersionResponse();
        response.setLatest(LATEST_VERSION);
        response.setObsolete(OBSOLETE_VERSION);

        when(kapschService.getVersion()).thenReturn(response);

        mvc.perform(get("/v1/cbo/vignette/version")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.obsolete").value(OBSOLETE_VERSION))
                .andExpect(MockMvcResultMatchers.jsonPath("$.latest").value(LATEST_VERSION));
    }

    @Test
    public void testGetVignetteInventory() throws Exception {
        VignetteInventoryResponse response = new VignetteInventoryResponse();
        List<EVignetteInventoryProduct> products = new ArrayList<>(1);
        EVignetteInventoryProduct product = new EVignetteInventoryProduct();

        VignettePrice vignettePrice = new VignettePrice();
        vignettePrice.setAmount(VIGNETTE_PRICE);
        vignettePrice.setCurrency(CURRENCY);

        product.setPrice(vignettePrice);
        products.add(product);
        response.setProducts(products);

        when(kapschService.getInventory(any(HttpSession.class))).thenReturn(response);

        mvc.perform(get("/v1/cbo/vignette/inventory")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.products", hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products[0].price.currency").value(CURRENCY.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products[0].price.amount").value(VIGNETTE_PRICE.toString()));
    }

    @Test
    public void testSearchMissingPageNumber() throws Exception {
        mvc.perform(get("/v1/cbo/vignette/search")
                .param("page_size", PAGE_SIZE.toString())
                .param("last_record", LAST_RECORD.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testSearchMissingPageSize() throws Exception {
        mvc.perform(get("/v1/cbo/vignette/search")
                .param("page_number", PAGE_NUMBER.toString())
                .param("last_record", LAST_RECORD.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testSearchMissingLastRecord() throws Exception {
        mvc.perform(get("/v1/cbo/vignette/search")
                .param("page_number", PAGE_NUMBER.toString())
                .param("page_size", PAGE_SIZE.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testSearchVignette() throws Exception {
        SearchVignetteValidity searchVignetteValidity = new SearchVignetteValidity();
        searchVignetteValidity.setRequestedValidityStartDate(REQUESTED_VALIDITY_START_DATE);
        searchVignetteValidity.setValidityEndDateTimeUTC(VALIDITY_END_DATE_TIME_UTC);
        searchVignetteValidity.setValidityStartDateTimeUTC(VALIDITY_START_DATE_TIME_UTC);

        List<SearchVignette> searchVignetteList = new ArrayList<>(1);
        PaginatedKapshSearchResponse response = new PaginatedKapshSearchResponse();
        response.seteVignetteList(searchVignetteList);
        PageRequest page = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);

        SearchVignettePrice searchVignettePrice = new SearchVignettePrice();
        searchVignettePrice.setAmount(VIGNETTE_PRICE);
        searchVignettePrice.setCurrency(CURRENCY.toString());

        SearchVignetteVehicle searchVignetteVehicle = new SearchVignetteVehicle();
        searchVignetteVehicle.setCountryCode(COUNTRY_CODE);
        searchVignetteVehicle.setLpn(LPN);

        SearchVignetteProduct searchVignetteProduct = new SearchVignetteProduct();
        searchVignetteProduct.setEmissionClass(EMISSION_CLASS.name());
        searchVignetteProduct.setId(Integer.valueOf(E_VIGNETTE_ID));
        searchVignetteProduct.setValidityType(VIGNETTE_VALIDITY_TYPE.name());
        searchVignetteProduct.setVehicleType(VEHICLE_TYPE.name());

        SearchVignettePurchase searchVignettePurchase = new SearchVignettePurchase();
        searchVignettePurchase.setPurchaseDateTimeUTC(PURCHASE_DATE_TIME_UTC);

        SearchVignette searchVignette = new SearchVignette();
        searchVignette.setPrice(searchVignettePrice);
        searchVignette.setId(E_VIGNETTE_ID);
        searchVignette.setPosID(POS_ID);
        searchVignette.setProduct(searchVignetteProduct);
        searchVignette.setPurchase(searchVignettePurchase);
        searchVignette.setSalesPartner(SALES_PARTNER);
        searchVignette.setSalesPartnerID(SALES_PARTNER_ID);
        searchVignette.setStatus(STATUS);
        searchVignette.setValidity(searchVignetteValidity);
        searchVignette.setVehicle(searchVignetteVehicle);
        searchVignetteList.add(searchVignette);

        response.setHasMoreRecords(HAS_MORE_RECORDS);
        response.setLastRecord(LAST_RECORD);

        when(kapschService.vignetteSearch(eq(SORTING_PARAMETER),
                eq(SORTING_DIRECTION),
                eq(LAST_RECORD), eq(E_VIGNETTE_ID),
                eq(SALES_PARTNER_ID), eq(PRODUCT_ID), eq(LPN),
                eq(COUNTRY_CODE), eq(RECORDS_FROM),
                eq(STATUS), eq(formatter.parse(LAST_UPDATE_FROM)),
                eq(formatter.parse(LAST_UPDATE_TO)), any(HttpSession.class), refEq(page)))
                .thenReturn(response);

        mvc.perform(get("/v1/cbo/vignette/search")
                .param("page_number", PAGE_NUMBER.toString())
                .param("page_size", PAGE_SIZE.toString())
                .param("last_record", LAST_RECORD.toString())
                .param("sorting_parameter", SORTING_PARAMETER)
                .param("sorting_direction", SORTING_DIRECTION.name())
                .param("vignette_id", E_VIGNETTE_ID)
                .param("partner_id", SALES_PARTNER_ID.toString())
                .param("product_id", PRODUCT_ID.toString())
                .param("lpn", LPN)
                .param("country_code", COUNTRY_CODE)
                .param("status", STATUS.toString())
                .param("records_from", RECORDS_FROM)
                .param("last_update_from", LAST_UPDATE_FROM)
                .param("last_update_to", LAST_UPDATE_TO)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.hasMoreRecords").value(HAS_MORE_RECORDS))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastRecord").value(LAST_RECORD))
                .andExpect(MockMvcResultMatchers.jsonPath("$.eVignetteList", hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.eVignetteList[0].id").value(E_VIGNETTE_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.eVignetteList[0].posID").value(POS_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.eVignetteList[0].salesPartnerID").value(SALES_PARTNER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.eVignetteList[0].salesPartner").value(SALES_PARTNER))
                .andExpect(MockMvcResultMatchers.jsonPath("$.eVignetteList[0].status").value(STATUS.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.eVignetteList[0].price.amount").value(VIGNETTE_PRICE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.eVignetteList[0].price.currency").value(CURRENCY.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.eVignetteList[0].vehicle.countryCode").value(COUNTRY_CODE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.eVignetteList[0].vehicle.lpn").value(LPN))
                .andExpect(MockMvcResultMatchers.jsonPath("$.eVignetteList[0].product.emissionClass").value(EMISSION_CLASS.name()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.eVignetteList[0].product.id").value(E_VIGNETTE_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.eVignetteList[0].product.validityType").value(VIGNETTE_VALIDITY_TYPE.name()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.eVignetteList[0].product.vehicleType").value(VEHICLE_TYPE.name()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.eVignetteList[0].purchase.purchaseDateTimeUTC").value(PURCHASE_DATE_TIME_UTC))
                .andExpect(MockMvcResultMatchers.jsonPath("$.eVignetteList[0].validity.requestedValidityStartDate").value(REQUESTED_VALIDITY_START_DATE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.eVignetteList[0].validity.validityEndDateTimeUTC").value(VALIDITY_END_DATE_TIME_UTC))
                .andExpect(MockMvcResultMatchers.jsonPath("$.eVignetteList[0].validity.validityStartDateTimeUTC").value(VALIDITY_START_DATE_TIME_UTC));
    }

    @Test
    public void testGetVignetteStates() throws Exception {
        VignetteStatesResponse vignetteStatesResponse = new VignetteStatesResponse();

        List<VignetteStateResponse> vignetteStateResponseList =
                new ArrayList<>(1);

        VignetteStateResponse vignetteStateResponse = new VignetteStateResponse();

        vignetteStateResponse.setDescription(DESCTRIPTION);
        vignetteStateResponse.setId(STATE_ID);

        vignetteStateResponseList.add(vignetteStateResponse);

        vignetteStatesResponse.setStates(vignetteStateResponseList);

        when(kapschService.getVignetteStates(any(HttpSession.class)))
                .thenReturn(vignetteStatesResponse);

        mvc.perform(get("/v1/cbo/vignette/states")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.states", hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.states[0].description").value(DESCTRIPTION))
                .andExpect(MockMvcResultMatchers.jsonPath("$.states[0].id").value(STATE_ID));
    }

    @Test
    public void testGetInventoryC9() throws Exception {
        VignetteInventoryResponse response = new VignetteInventoryResponse();

        List<EVignetteInventoryProduct> products = new ArrayList<>(1);

        EVignetteInventoryProduct eVignetteInventoryProduct = new EVignetteInventoryProduct();

        VignettePrice vignettePrice = new VignettePrice();
        vignettePrice.setCurrency(CURRENCY);
        vignettePrice.setAmount(VIGNETTE_PRICE);

        eVignetteInventoryProduct.setPrice(vignettePrice);
        eVignetteInventoryProduct.setEmissionClass(EMISSION_CLASS);
        eVignetteInventoryProduct.setId(E_VIGNETTE_INVENTORY_PRODUCT_ID);
        eVignetteInventoryProduct.setMongoId(MONGO_ID);
        eVignetteInventoryProduct.setValidityType(VIGNETTE_VALIDITY_TYPE);
        eVignetteInventoryProduct.setVehicleType(VEHICLE_TYPE);

        products.add(eVignetteInventoryProduct);

        response.setProducts(products);

        when(kapschService.getInventoryC9(any(HttpSession.class)))
                .thenReturn(response);

        mvc.perform(get("/v1/cbo/vignette/inventoryC9")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.products", hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products[0].price.currency")
                        .value(CURRENCY.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products[0].price.amount")
                        .value(VIGNETTE_PRICE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products[0].id")
                        .value(E_VIGNETTE_INVENTORY_PRODUCT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products[0].mongoId")
                        .value(MONGO_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products[0].validityType")
                        .value(VIGNETTE_VALIDITY_TYPE.name()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products[0].vehicleType")
                        .value(VEHICLE_TYPE.name()))
                ;

    }

    @Test
    public void testGetPeriodSales() throws Exception {
        PeriodSalesResponse periodSalesResponse = new PeriodSalesResponse();

        List<VignetteSingleSale> vignetteSingleSaleList = new ArrayList<>(1);

        VignetteSingleSale vignetteSingleSale = new VignetteSingleSale();

        VignetteSingleSalePurchase vignetteSingleSalePurchase = new VignetteSingleSalePurchase();
        vignetteSingleSalePurchase.setPurchaseDateTimeUTC(PURCHASE_DATE_TIME_UTC);

        vignetteSingleSale.seteVignetteID(E_VIGNETTE_ID);
        vignetteSingleSale.setProductID(PRODUCT_ID);
        vignetteSingleSale.setPurchase(vignetteSingleSalePurchase);
        vignetteSingleSale.setSalesPartner(SALES_PARTNER);
        vignetteSingleSale.setSalesPartnerID(SALES_PARTNER_ID);

        vignetteSingleSaleList.add(vignetteSingleSale);

        periodSalesResponse.setHasMoreRecords(HAS_MORE_RECORDS);
        periodSalesResponse.setLastRecord(LAST_RECORD.toString());
        periodSalesResponse.setSingleSales(vignetteSingleSaleList);

        when(kapschService.getPeriodSales(eq(SALES_PARTNER_ID),
                eq(PRODUCT_ID), eq(RECORDS_FROM),
                eq(formatter.parse(PURCHASE_FROM_DATE)),
                eq(formatter.parse(PURCHASE_TO_DATE)), any(HttpSession.class)))
                .thenReturn(periodSalesResponse);
        mvc.perform(get("/v1/cbo/vignette/evsales")
                    .param("salesPartnerID", String.valueOf(SALES_PARTNER_ID))
                    .param("productID", String.valueOf(PRODUCT_ID))
                    .param("recordsFrom", RECORDS_FROM)
                    .param("purchaseFromDate", PURCHASE_FROM_DATE)
                    .param("purchaseToDate", PURCHASE_TO_DATE)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.hasMoreRecords").value(HAS_MORE_RECORDS))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastRecord").value(LAST_RECORD))
                .andExpect(MockMvcResultMatchers.jsonPath("$.singleSales[0].eVignetteID").value(E_VIGNETTE_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.singleSales[0].productID").value(PRODUCT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.singleSales[0].salesPartner").value(SALES_PARTNER))
                .andExpect(MockMvcResultMatchers.jsonPath("$.singleSales[0].salesPartnerID").value(SALES_PARTNER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.singleSales[0].purchase.purchaseDateTimeUTC").value(PURCHASE_DATE_TIME_UTC));
    }

    @Test
    public void testPeriodSalesMissingPartnerID() throws Exception {
        mvc.perform(get("/v1/cbo/vignette/evsales")
                .param("productID", String.valueOf(PRODUCT_ID))
                .param("recordsFrom", RECORDS_FROM)
                .param("purchaseFromDate", PURCHASE_FROM_DATE)
                .param("purchaseToDate", PURCHASE_TO_DATE))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testPeriodSalesMissingProductID() throws Exception {
        mvc.perform(get("/v1/cbo/vignette/evsales")
                .param("salesPartnerID", String.valueOf(SALES_PARTNER_ID))
                .param("recordsFrom", RECORDS_FROM)
                .param("purchaseFromDate", PURCHASE_FROM_DATE)
                .param("purchaseToDate", PURCHASE_TO_DATE))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testPeriodSalesMissingRecordsFrom() throws Exception {
        mvc.perform(get("/v1/cbo/vignette/evsales")
                .param("salesPartnerID", String.valueOf(SALES_PARTNER_ID))
                .param("productID", String.valueOf(PRODUCT_ID))
                .param("purchaseFromDate", PURCHASE_FROM_DATE)
                .param("purchaseToDate", PURCHASE_TO_DATE))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testPeriodSalesMissingPurchaseFromDate() throws Exception {
        mvc.perform(get("/v1/cbo/vignette/evsales")
                .param("salesPartnerID", String.valueOf(SALES_PARTNER_ID))
                .param("productID", String.valueOf(PRODUCT_ID))
                .param("recordsFrom", RECORDS_FROM)
                .param("purchaseToDate", PURCHASE_TO_DATE))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testPeriodSalesMissingPurchaseToDate() throws Exception {
        mvc.perform(get("/v1/cbo/vignette/evsales")
                .param("salesPartnerID", String.valueOf(SALES_PARTNER_ID))
                .param("productID", String.valueOf(PRODUCT_ID))
                .param("recordsFrom", RECORDS_FROM)
                .param("purchaseFromDate", PURCHASE_FROM_DATE))
                .andExpect(status().isBadRequest());
    }
}