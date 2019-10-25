package com.digitoll.commons.response;

import com.digitoll.commons.kapsch.classes.EVignetteProduct;
import com.digitoll.commons.kapsch.classes.VignettePurchase;
import com.digitoll.commons.kapsch.classes.VignetteValidity;
import com.digitoll.commons.kapsch.response.VignetteRegistrationResponseContent;
import com.digitoll.commons.model.Vehicle;
import com.digitoll.commons.model.VignettePrice;
import com.digitoll.commons.util.BasicUtils;

import java.util.Objects;

public class KapschPropertiesResponse {
    private String id;

    private PartnersProductResponse product = new PartnersProductResponse();

    private Integer status;

    private Vehicle.KapschVehicle vehicle = new Vehicle.KapschVehicle();

    private VignetteValidity validity;

    private VignettePrice price;

    private VignettePurchase purchase;

    public KapschPropertiesResponse() {

    }

    public KapschPropertiesResponse(VignetteRegistrationResponseContent in) {
        BasicUtils.copyNonNullProps(in, this);
        BasicUtils.copyNonNullProps(in.getProduct(),this.product);
    }

    public Vehicle.KapschVehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle.KapschVehicle vehicle) {
        this.vehicle = vehicle;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public VignetteValidity getValidity() {
        return validity;
    }

    public void setValidity(VignetteValidity validity) {
        this.validity = validity;
    }

    public VignettePrice getPrice() {
        return price;
    }

    public void setPrice(VignettePrice price) {
        this.price = price;
    }

    public VignettePurchase getPurchase() {
        return purchase;
    }

    public void setPurchase(VignettePurchase purchase) {
        this.purchase = purchase;
    }

    public PartnersProductResponse getProduct() {
        return product;
    }

    public void setProduct(PartnersProductResponse product) {
        this.product = product;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        KapschPropertiesResponse that = (KapschPropertiesResponse) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(product, that.product) &&
                Objects.equals(status, that.status) &&
                Objects.equals(vehicle, that.vehicle) &&
                Objects.equals(validity, that.validity) &&
                Objects.equals(price, that.price) &&
                Objects.equals(purchase, that.purchase);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, product, status, vehicle, validity, price, purchase);
    }
}
