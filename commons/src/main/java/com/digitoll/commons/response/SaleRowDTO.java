package com.digitoll.commons.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.digitoll.commons.kapsch.classes.EVignetteInventoryProduct;
import com.digitoll.commons.model.SaleRow;
import com.digitoll.commons.model.Vehicle;
import com.digitoll.commons.request.SaleRowRequest;
import com.digitoll.commons.util.BasicUtils;

import java.util.Arrays;
import java.util.Objects;

//was Used for sale Responce
public class SaleRowDTO extends SaleRow {

    //Vehicle properties
    private Vehicle vehicle;
    private ProductsResponse productsResponse;


    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public SaleRowDTO(){

    }

    public SaleRowDTO(SaleRowRequest saleRowRequest){
        BasicUtils.copyPropsSkip(saleRowRequest,this,Arrays.asList("vehicle"));
        setVehicle(new Vehicle(saleRowRequest.getVehicle()));

    }

    @JsonIgnore
    public void addRowPropertiesFromKapschProduct(EVignetteInventoryProduct product ){
        this.setKapschProductId(product.getId());
        this.getVehicle().setType(product.getVehicleType());
        this.getVehicle().setEmissionClass(product.getEmissionClass());
        this.setValidityType(product.getValidityType());
        this.setPrice(product.getPrice());
    }

    public String getSaleIdWithMask(String saleIdMask) {
        String saleSeq = String.valueOf(getSaleSequence());
        StringBuilder stringBuilder = new StringBuilder(saleIdMask);
        if (saleSeq.length() <= saleIdMask.length()) {
            return stringBuilder.replace(saleIdMask.length() - saleSeq.length(), saleIdMask.length(), saleSeq).toString();
        }
        return String.valueOf(getSaleSequence());
    }

    public SaleRowDTO(SaleRow saleRow){
        BasicUtils.copyPropsSkip(saleRow,this, Arrays.asList("vehicleId"));
    }

    public ProductsResponse getProductsResponse() {
        return productsResponse;
    }

    public void setProductsResponse(ProductsResponse productsResponse) {
        this.productsResponse = productsResponse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SaleRowDTO that = (SaleRowDTO) o;
        return Objects.equals(vehicle, that.vehicle) &&
                Objects.equals(productsResponse, that.productsResponse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), vehicle, productsResponse);
    }

    @Override
    public String toString() {
      return "SaleRowDTO{" +
          "vignetteId=" + super.getVignetteId() +
          ", vehicle=" + vehicle +
          ", productsResponse=" + productsResponse +
          '}';
    }
}
