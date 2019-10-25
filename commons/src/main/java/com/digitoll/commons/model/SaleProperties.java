package com.digitoll.commons.model;

import com.digitoll.commons.request.SaleRowRequest;

import java.util.List;

public interface SaleProperties {
    //erp - {"userId":"5d263939f0f430170b944b41","partnerId":"5d23188b09cadc1c48d07640","posId":"5d24550609cadc1fa8988508","saleRows":[{"activationDate":"2019-07-23T09:00:00.000Z","email":"pepsa@mail.bg","kapschProductId":102,"vehicle":{"countryCode":"BG","lpn":"CB1111KK"}}]}
    public String getPosId();

    public void setPosId(String posId);

    public String getUserId();

    public void setUserId(String userId);

    public List<SaleRowRequest> getSaleRows();

    public void setSaleRows(List<SaleRowRequest> saleRows);
}
