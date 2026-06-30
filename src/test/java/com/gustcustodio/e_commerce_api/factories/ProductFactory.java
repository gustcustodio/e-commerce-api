package com.gustcustodio.e_commerce_api.factories;

import com.gustcustodio.e_commerce_api.dtos.ProductRequestDTO;
import com.gustcustodio.e_commerce_api.dtos.ProductResponseDTO;
import com.gustcustodio.e_commerce_api.entities.Product;

public class ProductFactory {

    public static Product createValidProduct() {
        Product product = new Product(1L, "Valid Product", "Valid Product Description", 100.0, 10);
        product.getCategories().add(CategoryFactory.createCategory());
        return product;
    }

    public static Product createInvalidProduct() {
        Product product = new Product(1000L, null, null, 100.00, 10);
        return product;
    }

    public static ProductRequestDTO createProductRequestDTO() {
        return new ProductRequestDTO(createValidProduct());
    }

    public static ProductResponseDTO createProductResponseDTO() {
        return new ProductResponseDTO(createValidProduct());
    }

}
