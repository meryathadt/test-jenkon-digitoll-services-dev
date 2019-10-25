package com.digitoll.erp.controller;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import com.digitoll.commons.kapsch.classes.EVignetteInventoryProduct;
import com.digitoll.commons.response.ProductsResponse;
import com.digitoll.erp.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
@RunWith(SpringRunner.class)
@WebMvcTest(secure = false)
@ContextConfiguration(classes = { ProductController.class })
public class ProductControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	ProductService service;

	@Autowired
	ObjectMapper mapper;

	@Test
	public void testCreateProductSuccessful() throws Exception {
		final EVignetteInventoryProduct vignette = new EVignetteInventoryProduct();

		mockMvc.perform(post("/product/create").content(mapper.writeValueAsString(vignette))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

	}

	@Test
	public void testCreateBulkProductsSuccessful() throws Exception {
		List<EVignetteInventoryProduct> vignetteBulk = Arrays.asList(new EVignetteInventoryProduct());

		mockMvc.perform(
				post("/product/bulkCreate").content(mapper.writeValueAsString(vignetteBulk))
						.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

	}

	@Test
	public void testGetProductsSuccessful() throws Exception {
		
		List<ProductsResponse> mockedProducts = Arrays.asList(new ProductsResponse());
		
		when(service.getProducts()).thenReturn(mockedProducts);

		mockMvc.perform(get("/products"))
		.andExpect(jsonPath("$.[0].vehicleTypeText").value(mockedProducts.get(0).getVehicleTypeText()))
		.andExpect(jsonPath("$.[0].emissionClassText").value(mockedProducts.get(0).getEmissionClassText()))
		.andExpect(jsonPath("$.[0].validityTypeText").value(mockedProducts.get(0).getValidityTypeText()))
		.andExpect(jsonPath("$.[0].categoryDescriptionText").value(mockedProducts.get(0).getCategoryDescriptionText()))
		.andExpect(status().isOk());

	}

	@Test
	public void testCreateProductFail() throws Exception {
		mockMvc.perform(post("/product/create")
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().is4xxClientError());
	}

	@Test
	public void testCreateBulkProductsFail() throws Exception {
		List<EVignetteInventoryProduct> vignetteBulk = null;

		mockMvc.perform(
				post("/product/bulkCreate").content(mapper.writeValueAsString(vignetteBulk))
						.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is4xxClientError());
	}

}
