package com.digitoll.commons.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class VignetteValidityDTO {
    private Boolean isValid;
    private String vignetteCode;
    private String vehicleClass;
    private String emissionsClass;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date validityStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date validityEndDate;
    private Double price;
    private String error;

    public Boolean getValid() {
        return isValid;
    }

    public void setValid(Boolean valid) {
        isValid = valid;
    }

    public String getVignetteCode() {
        return vignetteCode;
    }

    public void setVignetteCode(String vignetteCode) {
        this.vignetteCode = vignetteCode;
    }

    public String getVehicleClass() {
        return vehicleClass;
    }

    public void setVehicleClass(String vehicleClass) {
        this.vehicleClass = vehicleClass;
    }

    public String getEmissionsClass() {
        return emissionsClass;
    }

    public void setEmissionsClass(String emissionsClass) {
        this.emissionsClass = emissionsClass;
    }

    public Date getValidityStartDate() {
        return validityStartDate;
    }

    public void setValidityStartDate(Date validityStartDate) {
        this.validityStartDate = validityStartDate;
    }

    public Date getValidityEndDate() {
        return validityEndDate;
    }

    public void setValidityEndDate(Date validityEndDate) {
        this.validityEndDate = validityEndDate;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
