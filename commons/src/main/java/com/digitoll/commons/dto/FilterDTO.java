package com.digitoll.commons.dto;

import java.util.Date;

public class FilterDTO {

    private String lpn;
    private Date activationFrom;
    private Date activationTo;
    private Date validityStartFrom;
    private Date validityStartTo;
    private Date validityEndFrom;
    private Date validityEndTo;
    private Date registrationFrom;
    private Date registrationTo;

    public String getLpn() {
        return lpn;
    }

    public void setLpn(String lpn) {
        this.lpn = lpn;
    }

    public Date getActivationFrom() {
        return activationFrom;
    }

    public void setActivationFrom(Date activationFrom) {
        this.activationFrom = activationFrom;
    }

    public Date getActivationTo() {
        return activationTo;
    }

    public void setActivationTo(Date activationTo) {
        this.activationTo = activationTo;
    }

    public Date getValidityStartFrom() {
        return validityStartFrom;
    }

    public void setValidityStartFrom(Date validityStartFrom) {
        this.validityStartFrom = validityStartFrom;
    }

    public Date getValidityStartTo() {
        return validityStartTo;
    }

    public void setValidityStartTo(Date validityStartTo) {
        this.validityStartTo = validityStartTo;
    }

    public Date getValidityEndFrom() {
        return validityEndFrom;
    }

    public void setValidityEndFrom(Date validityEndFrom) {
        this.validityEndFrom = validityEndFrom;
    }

    public Date getValidityEndTo() {
        return validityEndTo;
    }

    public void setValidityEndTo(Date validityEndTo) {
        this.validityEndTo = validityEndTo;
    }

    public Date getRegistrationFrom() {
        return registrationFrom;
    }

    public void setRegistrationFrom(Date registrationFrom) {
        this.registrationFrom = registrationFrom;
    }

    public Date getRegistrationTo() {
        return registrationTo;
    }

    public void setRegistrationTo(Date registrationTo) {
        this.registrationTo = registrationTo;
    }
}
