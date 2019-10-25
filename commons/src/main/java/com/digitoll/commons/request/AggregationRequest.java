package com.digitoll.commons.request;

import com.digitoll.commons.enumeration.VignetteValidityType;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

public class AggregationRequest {
    @ApiModelProperty(notes = "List of string parameters that you want to group by in AggregationResult.class",
            example = "[partnerId, posId, purchaseDate]")
    private String[] groupingFields;
    private Date validityStartDate;
    private Date validityEndDate;
    private String lpn;
    private String partnerId;
    private String posId;
    private String vignetteId;
    private String saleId;
    private String vehicleId;
    private String userId;
    private String partnerName;
    private String posName;
    private String userName;
    private VignetteValidityType validityType;
    private String email;
    private Boolean active;
    private Date createdOn;
    private Date fromRegistrationDate;
    private Date toRegistrationDate;
    private Date fromActivationDate;
    private Date toActivationDate;
    private String remoteClientId;
    private Integer category;
    private String dateGroupingBases;

    public Date getValidityStartDate() {
        return validityStartDate;
    }

    public void setValidityStartDate(Date validityStartDate) {
        this.validityStartDate = validityStartDate;
    }


    public Date getValidityEndDate() {
        return validityEndDate;
    }

    public void setValidityEndDate( Date validityEndDate) {
        this.validityEndDate = validityEndDate;
    }


    public String getLpn() {
        return lpn;
    }

    public void setLpn( String lpn) {
        this.lpn = lpn;
    }


    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId( String partnerId) {
        this.partnerId = partnerId;
    }


    public String getPosId() {
        return posId;
    }

    public void setPosId( String posId) {
        this.posId = posId;
    }


    public String getVignetteId() {
        return vignetteId;
    }

    public void setVignetteId( String vignetteId) {
        this.vignetteId = vignetteId;
    }


    public String getSaleId() {
        return saleId;
    }

    public void setSaleId( String saleId) {
        this.saleId = saleId;
    }


    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId( String vehicleId) {
        this.vehicleId = vehicleId;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId( String userId) {
        this.userId = userId;
    }


    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName( String partnerName) {
        this.partnerName = partnerName;
    }


    public String getPosName() {
        return posName;
    }

    public void setPosName( String posName) {
        this.posName = posName;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName( String userName) {
        this.userName = userName;
    }


    public VignetteValidityType getValidityType() {
        return validityType;
    }

    public void setValidityType( VignetteValidityType validityType) {
        this.validityType = validityType;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail( String email) {
        this.email = email;
    }


    public Boolean getActive() {
        return active;
    }

    public void setActive( Boolean active) {
        this.active = active;
    }


    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn( Date createdOn) {
        this.createdOn = createdOn;
    }


    public Date getFromRegistrationDate() {
        return fromRegistrationDate;
    }

    public void setFromRegistrationDate( Date fromRegistrationDate) {
        this.fromRegistrationDate = fromRegistrationDate;
    }


    public Date getToRegistrationDate() {
        return toRegistrationDate;
    }

    public void setToRegistrationDate( Date toRegistrationDate) {
        this.toRegistrationDate = toRegistrationDate;
    }


    public Date getFromActivationDate() {
        return fromActivationDate;
    }

    public void setFromActivationDate( Date fromActivationDate) {
        this.fromActivationDate = fromActivationDate;
    }


    public Date getToActivationDate() {
        return toActivationDate;
    }

    public void setToActivationDate( Date toActivationDate) {
        this.toActivationDate = toActivationDate;
    }


    public String getRemoteClientId() {
        return remoteClientId;
    }

    public void setRemoteClientId( String remoteClientId) {
        this.remoteClientId = remoteClientId;
    }


    public Integer getCategory() {
        return category;
    }

    public void setCategory( Integer category) {
        this.category = category;
    }

    public String[] getGroupingFields() {
        return groupingFields;
    }

    public void setGroupingFields(String[] groupingFields) {
        this.groupingFields = groupingFields;
    }

    public String getDateGroupingBases() {
        return dateGroupingBases;
    }

    public void setDateGroupingBases(String dateGroupingBases) {
        this.dateGroupingBases = dateGroupingBases;
    }
}
