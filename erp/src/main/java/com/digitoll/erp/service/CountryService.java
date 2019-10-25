package com.digitoll.erp.service;

import com.digitoll.commons.util.BasicUtils;
import com.digitoll.commons.dto.CountryResponseDTO;
import com.digitoll.commons.model.Country;
import com.digitoll.erp.repository.CountryRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CountryService {
    
    @Autowired
    private CountryRepository countryRepository;

    public void insertCountryList(HashMap<String, String> countryList, String language) {
        
        Country country;
        
        for (String code: countryList.keySet()) {
            country = new Country(code.toUpperCase(), countryList.get(code), language.toLowerCase());
            countryRepository.save(country);
        }
    }
    
    public List<CountryResponseDTO> getCountriesByLanguage(String language) {
        
        List<Country> tempResult;
        List<CountryResponseDTO> result = new ArrayList();
        CountryResponseDTO newCountry;
        
        language = language == null ? "en" : language;
        
        tempResult = countryRepository.findAllByLanguage(language.toLowerCase());
        
        for (Country oldCountry:tempResult) {
            newCountry = new CountryResponseDTO();
            BasicUtils.copyPropsSkip(oldCountry, newCountry, Arrays.asList("id", "language"));
            result.add(newCountry);
        }
        
        return result;
    }
    
    public Country getCountryNameByCodeAndLanguage(String code, String language) {
        
        return countryRepository.findByCountryCodeAndLanguage(code.toUpperCase(), language.toLowerCase());
    }
}
