package com.digitoll.commons.request;

import com.digitoll.commons.model.Vehicle;

// we want these things in both request and response
public interface SaleRowDTOProperties {

    Vehicle.KapschVehicle getVehicle();

    void setVehicle(Vehicle.KapschVehicle vehicle);

}
