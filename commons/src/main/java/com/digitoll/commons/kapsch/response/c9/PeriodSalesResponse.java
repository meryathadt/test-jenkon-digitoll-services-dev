package com.digitoll.commons.kapsch.response.c9;

import java.util.List;

public class PeriodSalesResponse {
    
    private List<VignetteSingleSale> singleSales;
    private String lastRecord;
    private boolean hasMoreRecords;

    public List<VignetteSingleSale> getSingleSales() {
        return singleSales;
    }

    public void setSingleSales(List<VignetteSingleSale> singleSales) {
        this.singleSales = singleSales;
    }

    public String getLastRecord() {
        return lastRecord;
    }

    public void setLastRecord(String lastRecord) {
        this.lastRecord = lastRecord;
    }

    public boolean getHasMoreRecords() {
        return hasMoreRecords;
    }

    public void setHasMoreRecords(boolean hasMoreRecords) {
        this.hasMoreRecords = hasMoreRecords;
    }
}
