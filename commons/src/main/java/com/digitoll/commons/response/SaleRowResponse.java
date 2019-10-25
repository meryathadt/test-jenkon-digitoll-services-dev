package com.digitoll.commons.response;

import com.digitoll.commons.util.BasicUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.Objects;

public class SaleRowResponse {

    private String remoteClientId;
    private Boolean active;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date createdOn;

    private KapschPropertiesResponse kapschProperties = new KapschPropertiesResponse();

    public SaleRowResponse(){}

    public SaleRowResponse(SaleRowDTO saleRowDTO){
        BasicUtils.copyNonNullProps(saleRowDTO,this);
        BasicUtils.copyNonNullProps(saleRowDTO.getKapschProperties(),this.getKapschProperties());
        BasicUtils.copyNonNullProps(saleRowDTO.getKapschProperties().getProduct(),this.getKapschProperties().getProduct());
        BasicUtils.copyNonNullProps(saleRowDTO.getKapschProperties().getVehicle(),this.getKapschProperties().getVehicle());

    }
    public String getRemoteClientId() {
        return remoteClientId;
    }

    public void setRemoteClientId(String remoteClientId) {
        this.remoteClientId = remoteClientId;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public KapschPropertiesResponse getKapschProperties() {
        return kapschProperties;
    }

    public void setKapschProperties(KapschPropertiesResponse kapschProperties) {
        this.kapschProperties = kapschProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SaleRowResponse that = (SaleRowResponse) o;
        return Objects.equals(remoteClientId, that.remoteClientId) &&
                Objects.equals(active, that.active) &&
                Objects.equals(kapschProperties, that.kapschProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(remoteClientId, active, kapschProperties);
    }
}
