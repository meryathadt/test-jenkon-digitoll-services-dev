package com.digitoll.commons.request;

import com.digitoll.commons.model.SaleProperties;
import com.digitoll.commons.request.SaleRowRequest;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;

public class SaleRequest implements SaleProperties {
    //
    @ApiModelProperty(notes = "The ID of the POS that " +
            "made was selected when creating the sale",
            example="5d24550609cadc1fa8988508")
    private String posId;
    @ApiModelProperty(notes = "The ID of the user that made the request",
            example="5d263939f0f430170b944b41")
    private String userId;
    private List<SaleRowRequest> saleRows;

    @Override
    public String getPosId() {
        return posId;
    }

    @Override
    public void setPosId(String posId) {
        this.posId = posId;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public List<SaleRowRequest> getSaleRows() {
        return saleRows;
    }

    public void setSaleRows(List<SaleRowRequest> saleRows) {
        this.saleRows = saleRows;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SaleRequest that = (SaleRequest) o;
        return Objects.equals(posId, that.posId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(saleRows, that.saleRows);
    }

    @Override
    public int hashCode() {
        return Objects.hash(posId, userId, saleRows);
    }

    @Override
    public String toString() {
        return "SaleRequest{" +
                "posId='" + posId + '\'' +
                ", userId='" + userId + '\'' +
                ", saleRows=" + saleRows +
                '}';
    }
}
