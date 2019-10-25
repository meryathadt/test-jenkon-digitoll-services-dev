package com.digitoll.commons.kapsch.response.c9;

import com.digitoll.commons.model.VignettePrice;

public class SaleResponse {

    private Integer productID;
    private Long quantity;
    private VignettePrice price;
    private VignettePrice totalSum;

    public VignettePrice getTotalSum() {
        return totalSum;
    }

    public void setTotalSum(VignettePrice totalSum) {
        this.totalSum = totalSum;
    }

    public VignettePrice getPrice() {
        return price;
    }

    public void setPrice(VignettePrice price) {
        this.price = price;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Integer getProductID() {
        return productID;
    }

    public void setProductID(Integer productID) {
        this.productID = productID;
    }
}
