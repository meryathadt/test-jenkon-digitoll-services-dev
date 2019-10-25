package com.digitoll.erp.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import com.digitoll.commons.dto.CountryResponseDTO;
import com.digitoll.commons.model.Country;
import com.digitoll.erp.service.CountryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CountryController.class })
@WebMvcTest(secure = false)
public class CountryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper mapper;

	@MockBean
	private CountryService service;

	@Test
	public void testGetCountriesSuccess() throws Exception {

		List<CountryResponseDTO> countries = Arrays.asList(new CountryResponseDTO());

		final String lang = "en";

		Mockito.when(service.getCountriesByLanguage(lang)).thenReturn(countries);

		mockMvc.perform(get("/countries").header("Accept-Language", lang))
				.andExpect(jsonPath("$.[0].countryName").value(countries.get(0).getCountryCode()))
				.andExpect(jsonPath("$.[0].countryCode").value(countries.get(0).getCountryName()))
				.andExpect(status().isOk());

	}
	
    @Test
    public void testGetCountriesEmptySuccess() throws Exception {

        Mockito.when(service.getCountriesByLanguage(Mockito.anyString())).thenReturn(new ArrayList<CountryResponseDTO>());

        mockMvc.perform(get("/countries")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
        
    }

	@Test
	public void testInsertCountriesSuccess() throws Exception {

		final HashMap<String, String> countries = new HashMap<>();
		countries.put("Bulgaria", "bg");

		mockMvc.perform(post("/countries/bg").content(mapper.writeValueAsString(countries))
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
		
		Mockito.verify(service).insertCountryList(countries, "bg");

	}

	@Test
	public void testGetCountryNameByCodeSuccess() throws Exception {
		final Country country = new Country("BG", "Bulgaria", "bg");
		country.setId("666");

		Mockito.when(service.getCountryNameByCodeAndLanguage(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(country);

		mockMvc.perform(get("/country", "bg").header("Accept-Language", "bg").param("countryCode", "bg"))
				.andExpect(jsonPath("$.id").value(country.getId()))
				.andExpect(jsonPath("$.countryCode").value(country.getCountryCode()))
				.andExpect(jsonPath("$.countryName").value(country.getCountryName()))
				.andExpect(jsonPath("$.language").value(country.getLanguage())).andExpect(status().isOk());

	}

	@Test
	public void testInsertCountriesFailure() throws Exception {

		mockMvc.perform(
				post("/countries/bg").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().is4xxClientError());

	}

	@Test
	public void testGetCountryNameByCodeFailure() throws Exception {

		mockMvc.perform(get("/country)")).andExpect(status().is4xxClientError());

	}

}
