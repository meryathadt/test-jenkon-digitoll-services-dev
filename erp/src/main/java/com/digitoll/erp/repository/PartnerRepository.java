package com.digitoll.erp.repository;

import com.digitoll.commons.model.Partner;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PartnerRepository extends MongoRepository<Partner, String> {
    Partner findOneById(String id);
}
