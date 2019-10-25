package com.digitoll.commons.kapsch.request;

import com.digitoll.commons.model.Vehicle;

import java.util.Date;

public class VignetteRegistrationRequest {
	private Integer productId;
	private Vehicle vehicle;
	private VignetteRegistrationValidity validity;

    public VignetteRegistrationRequest() {
    }
    
    public static class VignetteRegistrationValidity {

        public VignetteRegistrationValidity() {
        }
        
        private Date requestedValidityStartDate;

        public Date getRequestedValidityStartDate() {
            return requestedValidityStartDate;
        }

        public void setRequestedValidityStartDate(Date requestedValidityStartDate) {
            this.requestedValidityStartDate = requestedValidityStartDate;
        }
    }    

    public VignetteRegistrationRequest withProductId(Integer productId){
    	this.setProductId(productId);
    	return this;
	}

	public VignetteRegistrationRequest withVehicle(Vehicle vehicle){
		this.setVehicle(vehicle);
		return this;
	}

	public VignetteRegistrationRequest withDate(Date validityStartDate){
		VignetteRegistrationValidity validity = new VignetteRegistrationValidity();
		validity.setRequestedValidityStartDate(validityStartDate);
		this.setValidity(validity);
		return this;
	}

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public VignetteRegistrationValidity getValidity() {
		return validity;
	}

	public void setValidity(VignetteRegistrationValidity validity) {
		this.validity = validity;
	}
}

