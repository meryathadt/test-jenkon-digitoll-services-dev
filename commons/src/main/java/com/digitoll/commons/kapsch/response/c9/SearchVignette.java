package com.digitoll.commons.kapsch.response.c9;

public class SearchVignette {

    private String id;
    private String posID;
    private String salesPartner;
    private Integer salesPartnerID;
    private Integer status;

    private SearchVignettePrice price;
    private SearchVignetteProduct product;
    private SearchVignettePurchase purchase;
    private SearchVignetteValidity validity;
    private SearchVignetteVehicle vehicle;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPosID() {
        return posID;
    }

    public void setPosID(String posID) {
        this.posID = posID;
    }

    public String getSalesPartner() {
        return salesPartner;
    }

    public void setSalesPartner(String salesPartner) {
        this.salesPartner = salesPartner;
    }

    public Integer getSalesPartnerID() {
        return salesPartnerID;
    }

    public void setSalesPartnerID(Integer salesPartnerID) {
        this.salesPartnerID = salesPartnerID;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public SearchVignettePrice getPrice() {
        return price;
    }

    public void setPrice(SearchVignettePrice price) {
        this.price = price;
    }

    public SearchVignetteProduct getProduct() {
        return product;
    }

    public void setProduct(SearchVignetteProduct product) {
        this.product = product;
    }

    public SearchVignettePurchase getPurchase() {
        return purchase;
    }

    public void setPurchase(SearchVignettePurchase purchase) {
        this.purchase = purchase;
    }

    public SearchVignetteValidity getValidity() {
        return validity;
    }

    public void setValidity(SearchVignetteValidity validity) {
        this.validity = validity;
    }

    public SearchVignetteVehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(SearchVignetteVehicle vehicle) {
        this.vehicle = vehicle;
    }
}
