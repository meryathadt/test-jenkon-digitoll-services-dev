package com.digitoll.commons.request;

import com.digitoll.commons.model.Vehicle;
import com.digitoll.commons.model.SaleRowProperties;

import java.time.LocalDateTime;
import java.util.Objects;

public class SaleRowRequest implements SaleRowDTOProperties, SaleRowProperties {

    private LocalDateTime activationDate;
    private String email;
    private Integer kapschProductId;
    private Vehicle.KapschVehicle vehicle;
    private String remoteClientId;

    @Override
    public LocalDateTime getActivationDate() {
        return activationDate;
    }

    @Override
    public void setActivationDate(LocalDateTime activationDate) {
        this.activationDate = activationDate;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public Integer getKapschProductId() {
        return kapschProductId;
    }

    @Override
    public void setKapschProductId(Integer kapschProductId) {
        this.kapschProductId = kapschProductId;
    }

    @Override
    public Vehicle.KapschVehicle getVehicle() {
        return vehicle;
    }

    @Override
    public void setVehicle(Vehicle.KapschVehicle vehicle) {
        this.vehicle = vehicle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SaleRowRequest that = (SaleRowRequest) o;
        return Objects.equals(activationDate, that.activationDate) &&
                Objects.equals(email, that.email) &&
                Objects.equals(kapschProductId, that.kapschProductId) &&
                Objects.equals(vehicle, that.vehicle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activationDate, email, kapschProductId, vehicle);
    }

    @Override
    public String toString() {
        return "SaleRowRequest{" +
                "activationDate=" + activationDate +
                ", email='" + email + '\'' +
                ", kapschProductId=" + kapschProductId +
                ", vehicle=" + vehicle +
                '}';
    }

    public String getRemoteClientId() {
        return remoteClientId;
    }

    public void setRemoteClientId(String remoteClientId) {
        this.remoteClientId = remoteClientId;
    }
}
