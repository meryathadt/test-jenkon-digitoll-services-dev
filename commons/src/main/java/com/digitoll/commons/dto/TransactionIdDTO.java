package com.digitoll.commons.dto;

import javax.validation.constraints.NotBlank;
import java.util.Objects;

public class TransactionIdDTO {

    @NotBlank
    private String transactionId;

    private String saleId;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getSaleId() {
        return saleId;
    }

    public void setSaleId(String saleId) {
        this.saleId = saleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionIdDTO that = (TransactionIdDTO) o;
        return Objects.equals(transactionId, that.transactionId) &&
                Objects.equals(saleId, that.saleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, saleId);
    }
}
