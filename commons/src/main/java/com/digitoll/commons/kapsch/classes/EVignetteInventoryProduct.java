package com.digitoll.commons.kapsch.classes;

import com.digitoll.commons.model.VignettePrice;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@Document(collection = "products")
public class EVignetteInventoryProduct extends EVignetteProduct {
	
    @NotNull
	private VignettePrice price;

	public VignettePrice getPrice() {
		return price;
	}

	public void setPrice(VignettePrice price) {
		this.price = price;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		EVignetteInventoryProduct that = (EVignetteInventoryProduct) o;
		return Objects.equals(price, that.price);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), price);
	}
}
