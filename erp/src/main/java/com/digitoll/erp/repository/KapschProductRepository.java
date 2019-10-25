package com.digitoll.erp.repository;

import com.digitoll.commons.kapsch.classes.EVignetteInventoryProduct;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface KapschProductRepository  extends MongoRepository<EVignetteInventoryProduct, String> {
    EVignetteInventoryProduct findOneById(Integer productId);

    EVignetteInventoryProduct findOneByVehicleType(String vehicleType);

    EVignetteInventoryProduct findOneByVehicleTypeAndEmissionClassAndValidityType( String vehicleType,
            String emissionClass,
            String validityType);

}
