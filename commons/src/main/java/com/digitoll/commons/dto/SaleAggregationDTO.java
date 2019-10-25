package com.digitoll.commons.dto;

import com.digitoll.commons.aggregation.AggregatedResult;
import com.digitoll.commons.util.BasicUtils;

import java.math.BigDecimal;
import java.util.Objects;

public class SaleAggregationDTO {

    public static final String INACTIVE = "Inactive";
    public static final String ACTIVE = "Active";
    public static final String CANCELLED = "Cancelled";
    private String purchaseDate;
    private Integer count;
    private Integer kapschProductId;
    private BigDecimal totalAmount;
    private String productName;
    private String partnerName;
    private String posName;
    private String registrationDate;
    private String status;

    public SaleAggregationDTO() {
    }

    public SaleAggregationDTO(AggregatedResult aggregatedResult) {
        BasicUtils.copyNonNullProps(aggregatedResult, this);
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getKapschProductId() {
        return kapschProductId;
    }

    public void setKapschProductId(Integer kapschProductId) {
        this.kapschProductId = kapschProductId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public String getPosName() {
        return posName;
    }

    public void setPosName(String posName) {
        this.posName = posName;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        switch (status) {
            case 1:
                this.status = INACTIVE;
                break;
            case 2:
                this.status = ACTIVE;
                break;
            case 3:
                this.status = CANCELLED;
                break;
            default:
                this.status = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SaleAggregationDTO that = (SaleAggregationDTO) o;
        return Objects.equals(purchaseDate, that.purchaseDate) &&
                Objects.equals(count, that.count) &&
                Objects.equals(kapschProductId, that.kapschProductId) &&
                Objects.equals(totalAmount, that.totalAmount) &&
                Objects.equals(productName, that.productName) &&
                Objects.equals(partnerName, that.partnerName) &&
                Objects.equals(posName, that.posName) &&
                Objects.equals(registrationDate, that.registrationDate) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(purchaseDate, count, kapschProductId, totalAmount, productName, partnerName, posName, registrationDate, status);
    }
}
