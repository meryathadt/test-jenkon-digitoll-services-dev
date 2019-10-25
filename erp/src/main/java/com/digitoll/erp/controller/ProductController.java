package com.digitoll.erp.controller;

import com.digitoll.commons.kapsch.classes.EVignetteInventoryProduct;
import com.digitoll.commons.response.ProductsResponse;
import com.digitoll.erp.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController(value = "/inventory")
public class ProductController {

    @Autowired
    private ProductService productService;

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping(value = "/product/create")
    public void createProduct(@RequestBody EVignetteInventoryProduct product) {
        productService.createInventoryProduct(product);
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping(value = "/product/bulkCreate")
    public void createBulkProducts(@RequestBody ArrayList<EVignetteInventoryProduct> products) {
        for (EVignetteInventoryProduct p: products) {
            productService.createInventoryProduct(p);
        }
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_PARTNER_ADMIN') or hasAuthority('ROLE_C9') or hasAuthority('ROLE_C2')")
    @GetMapping(value = "/products")
    public List<ProductsResponse> getProducts() {
            return productService.getProducts();
    }
}
