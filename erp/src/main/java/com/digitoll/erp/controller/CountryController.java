package com.digitoll.erp.controller;

import com.digitoll.commons.dto.CountryResponseDTO;
import com.digitoll.commons.model.Country;
import com.digitoll.erp.service.CountryService;
import io.swagger.annotations.ApiParam;

import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CountryController {
    
    @Autowired
    CountryService countryService;
    
    private static final Logger log = LoggerFactory.getLogger(CountryController.class);
    
    //works with json files from 
    //https://github.com/umpirsky/country-list/tree/master/data
    
    @CrossOrigin
    @PostMapping("/countries/{lang}")    
    public void insertCountries(@RequestBody HashMap<String, String> list, 
            @PathVariable String lang) {
        
        countryService.insertCountryList(list, lang);
    }
    
    @CrossOrigin
    @GetMapping("/countries")    
    public List<CountryResponseDTO> getCountries(HttpServletRequest request) {
        
        String lang = request.getHeader("Accept-Language");
        
        return countryService.getCountriesByLanguage(lang);
    }   
    
    @CrossOrigin
    @GetMapping("/country")
    public Country getCountryNameByCode(            
            @RequestParam(value = "countryCode", required = true)
            @ApiParam(name = "countryCode", example = "ISO-2 country code: bg, en")
                    String countryCode, HttpServletRequest request) {
        
        Country country;
        String lang = request.getHeader("Accept-Language");
        
        country = countryService.getCountryNameByCodeAndLanguage(countryCode, lang);
        
        return country;
    }
    
}
 