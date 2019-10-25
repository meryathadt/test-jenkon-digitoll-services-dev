package com.digitoll.commons.kapsch.response;

import com.digitoll.commons.model.Vehicle;
import com.digitoll.commons.model.VignettePrice;
import com.digitoll.commons.kapsch.classes.EVignetteProduct;
import com.digitoll.commons.kapsch.classes.VignettePurchase;
import com.digitoll.commons.kapsch.classes.VignetteValidity;

import java.util.Objects;

public class VignetteRegistrationResponseContent {
	private String id;
	private EVignetteProduct product;
	private Integer status;
	private Vehicle vehicle;
	private VignetteValidity validity;
	private VignettePrice price;
	private VignettePurchase purchase;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public EVignetteProduct getProduct() {
		return product;
	}

	public void setProduct(EVignetteProduct product) {
		this.product = product;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public VignetteValidity getValidity() {
		return validity;
	}

	public void setValidity(VignetteValidity validity) {
		this.validity = validity;
	}

	public VignettePrice getPrice() {
		return price;
	}

	public void setPrice(VignettePrice price) {
		this.price = price;
	}

	public VignettePurchase getPurchase() {
		return purchase;
	}

	public void setPurchase(VignettePurchase purchase) {
		this.purchase = purchase;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		VignetteRegistrationResponseContent that = (VignetteRegistrationResponseContent) o;
		return Objects.equals(id, that.id) &&
				Objects.equals(product, that.product) &&
				Objects.equals(status, that.status) &&
				Objects.equals(vehicle, that.vehicle) &&
				Objects.equals(validity, that.validity) &&
				Objects.equals(price, that.price) &&
				Objects.equals(purchase, that.purchase);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, product, status, vehicle, validity, price, purchase);
	}
}
