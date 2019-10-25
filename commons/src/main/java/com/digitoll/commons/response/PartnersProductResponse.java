package com.digitoll.commons.response;

import com.digitoll.commons.enumeration.EmissionClass;
import com.digitoll.commons.enumeration.VehicleType;
import com.digitoll.commons.enumeration.VignetteValidityType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;

public class PartnersProductResponse {

    private Integer id;

    @NotBlank
    private VehicleType vehicleType;

    @NotBlank
    private EmissionClass emissionClass;

    @NotNull
    private VignetteValidityType validityType;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public EmissionClass getEmissionClass() {
        return emissionClass;
    }

    public void setEmissionClass(EmissionClass emissionClass) {
        this.emissionClass = emissionClass;
    }

    public VignetteValidityType getValidityType() {
        return validityType;
    }

    public void setValidityType(VignetteValidityType validityType) {
        this.validityType = validityType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PartnersProductResponse that = (PartnersProductResponse) o;
        return Objects.equals(id, that.id) &&
                vehicleType == that.vehicleType &&
                emissionClass == that.emissionClass &&
                validityType == that.validityType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, vehicleType, emissionClass, validityType);
    }
}
