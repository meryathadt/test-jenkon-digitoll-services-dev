package com.digitoll.erp.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.digitoll.commons.dto.CountryResponseDTO;
import com.digitoll.commons.model.Country;
import com.digitoll.commons.util.BasicUtils;
import com.digitoll.erp.repository.CountryRepository;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CountryService.class })
public class CountryServiceTest {

	@MockBean
	CountryRepository repository;

	@Autowired
	CountryService service;

	@Test
	public void testGetCountriesByLanguageSuccess() {
		String language = "en";
		List<Country> countriesInRepo = new LinkedList<>();
		List<CountryResponseDTO> mockedCountries = new LinkedList<>();
		Country mockCountry = new Country();
		mockCountry.setCountryCode("BG");
		mockCountry.setCountryName("Bulgaria");
		mockCountry.setLanguage(language);
		mockCountry.setId("9");
		countriesInRepo.add(mockCountry);

		Mockito.when(repository.findAllByLanguage(language)).thenReturn(countriesInRepo);

		countriesInRepo.forEach(country -> {
			CountryResponseDTO newCountry = new CountryResponseDTO();
			BasicUtils.copyPropsSkip(country, newCountry, Arrays.asList("id", "language"));
			mockedCountries.add(newCountry);
		});

		List<CountryResponseDTO> countries = service.getCountriesByLanguage(language);

		assertFalse(countries.isEmpty());
		assertEquals(mockedCountries, countries);
		assertTrue(countries.size() == countriesInRepo.size());

	}

	@Test
	public void testGetCountriesByLanguageSuccessEmpty() {
		String language = "en";

		Mockito.when(repository.findAllByLanguage(language)).thenReturn(new ArrayList<Country>());

		List<CountryResponseDTO> countries = service.getCountriesByLanguage(language);

		assertTrue(countries.isEmpty());

	}

	@Test
	public void testInsertCountryListSuccess() {
		String language = "bulgarian";
		Country mockCountry = new Country("BG", "Bulgaria", language.toLowerCase());
		HashMap<String, String> countries = new HashMap<String, String>();

		countries.put("BG", "Bulgaria");

		when(repository.save(mockCountry)).thenReturn(mockCountry);

		service.insertCountryList(countries, language);

	}

	@Test
	public void testGetCountryNameByCodeAndLanguage() {
		String code = "BG";
		String language = "bulgarian";
		Country mockCountry = new Country();

		Mockito.when(repository.findByCountryCodeAndLanguage(code, language)).thenReturn(mockCountry);

		Country response = service.getCountryNameByCodeAndLanguage(code, language);

		assertEquals(mockCountry, response);
	}

}
