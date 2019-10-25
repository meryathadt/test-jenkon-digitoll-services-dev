package com.digitoll.commons.kapsch.response.c9;

public class VignetteSingleSale {

    private String eVignetteID;
    private Integer productID;
    private String salesPartner;
    private Integer salesPartnerID;
    private VignetteSingleSalePurchase purchase;

    public String geteVignetteID() {
        return eVignetteID;
    }

    public void seteVignetteID(String eVignetteID) {
        this.eVignetteID = eVignetteID;
    }

    public Integer getProductID() {
        return productID;
    }

    public void setProductID(Integer productID) {
        this.productID = productID;
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

    public VignetteSingleSalePurchase getPurchase() {
        return purchase;
    }

    public void setPurchase(VignetteSingleSalePurchase purchase) {
        this.purchase = purchase;
    }
}
