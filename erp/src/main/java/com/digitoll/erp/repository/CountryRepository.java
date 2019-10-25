package com.digitoll.erp.repository;

import com.digitoll.commons.model.Country;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CountryRepository extends MongoRepository<Country, String> {
    public List<Country> findAllByLanguage(String language);
    public Country findByCountryCodeAndLanguage(String countryCode, String language);
}
