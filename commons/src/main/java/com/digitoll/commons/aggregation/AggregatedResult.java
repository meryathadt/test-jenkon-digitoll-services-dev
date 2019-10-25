package com.digitoll.commons.aggregation;

import org.bson.types.Decimal128;

import java.math.BigDecimal;
import java.util.Objects;

public class AggregatedResult {

    private Object _id;
    private String purchaseDate;
    private Integer count;
    private Integer kapschProductId;
    private Decimal128 totalAmount;
    private String productName;
    private String partnerName;
    private String posName;
    private String partnerId;
    private String posId;
    private String registrationDate;
    private Integer status;


    public AggregatedResult(){

    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getPosId() {
        return posId;
    }

    public void setPosId(String posId) {
        this.posId = posId;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount.bigDecimalValue();
    }

    public void setTotalAmount(Decimal128 totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AggregatedResult that = (AggregatedResult) o;
        return
                Objects.equals(count, that.count) &&
                Objects.equals(totalAmount, that.totalAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, totalAmount);
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getKapschProductId() {
        return kapschProductId;
    }

    public void setKapschProductId(Integer kapschProductId) {
        this.kapschProductId = kapschProductId;
    }

    public String getPosName() {
        return posName;
    }

    public void setPosName(String posName) {
        this.posName = posName;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public Object get_id() {
        return _id;
    }

    public void set_id(Object _id) {
        this._id = _id;
    }
}