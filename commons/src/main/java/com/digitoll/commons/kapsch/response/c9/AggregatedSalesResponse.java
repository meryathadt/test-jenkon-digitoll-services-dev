package com.digitoll.commons.kapsch.response.c9;
import java.util.List;

public class AggregatedSalesResponse {

    private List<AggregatedSaleResponse> salesPerPartners;

    public List<AggregatedSaleResponse> getSalesPerPartners() {
        return salesPerPartners;
    }

    public void setSalesPerPartners(
            List<AggregatedSaleResponse> salesPerPartners) {
        this.salesPerPartners = salesPerPartners;
    }
}
