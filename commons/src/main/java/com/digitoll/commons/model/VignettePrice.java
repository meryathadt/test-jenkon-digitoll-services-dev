package com.digitoll.commons.model;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public class VignettePrice {

    @ApiModelProperty(notes = "The currency that the price is in", example = "BGN")
    private Currency currency;
    @ApiModelProperty(notes = "The cost of the vignette", example = "97")
    private BigDecimal amount;

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VignettePrice that = (VignettePrice) o;
        return Objects.equals(currency, that.currency) &&
                Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currency, amount);
    }
}

