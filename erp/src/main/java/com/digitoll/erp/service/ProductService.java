package com.digitoll.erp.service;

import com.digitoll.commons.kapsch.classes.EVignetteInventoryProduct;
import com.digitoll.erp.repository.KapschProductRepository;
import com.digitoll.commons.response.ProductsResponse;
import com.digitoll.erp.component.TranslationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private KapschProductRepository kapschProductRepository;
    
    @Autowired
    private TranslationComponent translationComponent;

    public void createInventoryProduct(EVignetteInventoryProduct product) {
        kapschProductRepository.save(product);
    }

    public List<ProductsResponse> getProducts() {
        List<EVignetteInventoryProduct> products = kapschProductRepository.findAll();
        List<ProductsResponse> productsResponses = new ArrayList<>();
        
        products.forEach(p->{
            productsResponses.add(translationComponent.translateProduct(p, null));
        });
        return productsResponses;
    }

    public ProductsResponse getProduct(Integer kapschProductId) {
        EVignetteInventoryProduct product = kapschProductRepository.findOneById(kapschProductId);
        return translationComponent.translateProduct(product, null);
    }

}
