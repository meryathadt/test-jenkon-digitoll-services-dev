package com.digitoll.commons.response;

import com.digitoll.commons.util.BasicUtils;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SaleResponse {
    private String id;
    private BigDecimal total;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date createdOn;
    private boolean active = false;
    private String email;
    private List<SaleRowResponse> saleRows = new ArrayList<>();

    public SaleResponse(){}

    public SaleResponse(SaleDTO saleDTO){
        BasicUtils.copyNonNullProps(saleDTO,this);
        for(SaleRowDTO srd : saleDTO.getSaleRows()){
            SaleRowResponse srr = new SaleRowResponse(srd);
            this.saleRows.add(srr);
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<SaleRowResponse> getSaleRows() {
        return saleRows;
    }
}
