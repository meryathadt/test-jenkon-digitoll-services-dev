package com.digitoll.commons.response;

import com.digitoll.commons.kapsch.classes.EVignetteInventoryProduct;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class ProductsResponse extends EVignetteInventoryProduct {
    @ApiModelProperty(notes = "A textual representation of the vehicleType field",
            example = "Light vehicle <= 3.5t")
    private String vehicleTypeText;
    @ApiModelProperty(notes = "A textual representation of the emissionClass field", example = "EURO 3")
    private String emissionClassText;
    @ApiModelProperty(notes = "A textual representation of the validityType field", example = "Quarterly")
    private String validityTypeText;
    @ApiModelProperty(notes = "The vehicle category is based on the vehicle's maximum" +
            " authorised mass (gross vehicle weight)", example = "Category 3")
    private String categoryDescriptionText;

    public ProductsResponse() {
    }

    public String getVehicleTypeText() {
        return vehicleTypeText;
    }

    public void setVehicleTypeText(String vehicleTypeText) {
        this.vehicleTypeText = vehicleTypeText;
    }

    public String getEmissionClassText() {
        return emissionClassText;
    }

    public void setEmissionClassText(String emissionClassText) {
        this.emissionClassText = emissionClassText;
    }

    public String getValidityTypeText() {
        return validityTypeText;
    }

    public void setValidityTypeText(String validityTypeText) {
        this.validityTypeText = validityTypeText;
    }

    public String getCategoryDescriptionText() {
        return categoryDescriptionText;
    }

    public void setCategoryDescriptionText(String categoryDescriptionText) {
        this.categoryDescriptionText = categoryDescriptionText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ProductsResponse that = (ProductsResponse) o;
        return Objects.equals(vehicleTypeText, that.vehicleTypeText) &&
                Objects.equals(emissionClassText, that.emissionClassText) &&
                Objects.equals(validityTypeText, that.validityTypeText) &&
                Objects.equals(categoryDescriptionText, that.categoryDescriptionText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), vehicleTypeText, emissionClassText, validityTypeText, categoryDescriptionText);
    }

    @Override
    public String toString() {
        return "ProductsResponse{" +
                "vehicleTypeText='" + vehicleTypeText + '\'' +
                ", emissionClassText='" + emissionClassText + '\'' +
                ", validityTypeText='" + validityTypeText + '\'' +
                ", categoryDescriptionText='" + categoryDescriptionText + '\'' +
                '}';
    }
}
