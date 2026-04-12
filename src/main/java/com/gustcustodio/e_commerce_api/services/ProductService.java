package com.gustcustodio.e_commerce_api.services;

import com.gustcustodio.e_commerce_api.repositories.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

}
