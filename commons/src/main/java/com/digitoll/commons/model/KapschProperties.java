package com.digitoll.commons.model;

import com.digitoll.commons.enumeration.EmissionClass;
import com.digitoll.commons.enumeration.VehicleType;
import com.digitoll.commons.enumeration.VignetteValidityType;

public class KapschProperties {

    private VehicleType vehicleType;
    private VignetteValidityType validityType;
    private EmissionClass emissionClass;
    private String kapsch_id;

    public KapschProperties(){

    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public VignetteValidityType getValidityType() {
        return validityType;
    }

    public void setValidityType(VignetteValidityType validityType) {
        this.validityType = validityType;
    }

    public EmissionClass getEmissionClass() {
        return emissionClass;
    }

    public void setEmissionClass(EmissionClass emissionClass) {
        this.emissionClass = emissionClass;
    }

    public String getKapsch_id() {
        return kapsch_id;
    }

    public void setKapsch_id(String kapsch_id) {
        this.kapsch_id = kapsch_id;
    }
}
