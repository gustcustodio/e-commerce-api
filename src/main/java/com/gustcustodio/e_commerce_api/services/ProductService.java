package com.gustcustodio.e_commerce_api.services;

import com.gustcustodio.e_commerce_api.dtos.ProductResponseDTO;
import com.gustcustodio.e_commerce_api.entities.Product;
import com.gustcustodio.e_commerce_api.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO findProductWithCategories(Long id) {
        Product entity = productRepository.findProductWithCategories(id).orElseThrow();
        return new ProductResponseDTO(entity);
    }

}
