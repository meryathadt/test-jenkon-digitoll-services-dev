package com.digitoll.commons.model;

import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class CashTerminalPos {

    @ApiModelProperty(notes = "The address of the POS, which will also be used as it's name",
            example = "София, ****, ж.к. Люлин, ул. Дж.Неру **")
    private String address;
    private String lat;
    private String lng;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CashTerminalPos that = (CashTerminalPos) o;
        return Objects.equals(address, that.address) &&
                Objects.equals(lat, that.lat) &&
                Objects.equals(lng, that.lng);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, lat, lng);
    }
}