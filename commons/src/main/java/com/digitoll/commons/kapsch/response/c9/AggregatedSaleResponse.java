package com.digitoll.commons.kapsch.response.c9;

import java.util.List;

public class AggregatedSaleResponse {

    private List<SaleResponse> sales;
    private Integer salesPartnerID;

    public List<SaleResponse> getSales() {
        return sales;
    }

    public void setSales(List<SaleResponse> sales) {
        this.sales = sales;
    }

    public Integer getSalesPartnerID() {
        return salesPartnerID;
    }

    public void setSalesPartnerID(Integer salesPartnerID) {
        this.salesPartnerID = salesPartnerID;
    }
}
