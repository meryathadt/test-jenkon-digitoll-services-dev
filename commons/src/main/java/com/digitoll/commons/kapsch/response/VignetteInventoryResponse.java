package com.digitoll.commons.kapsch.response;

import com.digitoll.commons.kapsch.classes.EVignetteInventoryProduct;
import java.util.List;


public class VignetteInventoryResponse {
	private List<EVignetteInventoryProduct> products;

	public List<EVignetteInventoryProduct> getProducts() {
		return products;
	}

	public void setProducts(List<EVignetteInventoryProduct> products) {
		this.products = products;
	}
}
