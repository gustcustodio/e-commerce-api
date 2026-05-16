package com.gustcustodio.e_commerce_api.constructors;

import com.gustcustodio.e_commerce_api.dtos.CategoryRequestDTO;
import com.gustcustodio.e_commerce_api.dtos.CategoryResponseDTO;
import com.gustcustodio.e_commerce_api.dtos.ProductRequestDTO;
import com.gustcustodio.e_commerce_api.dtos.ProductResponseDTO;
import com.gustcustodio.e_commerce_api.entities.Category;
import com.gustcustodio.e_commerce_api.entities.Product;

public class Factory {

    public static Product createProduct() {
        Product product = new Product(1L, "Valid Product", "Valid Product Description", 100.0, 10);
        product.getCategories().add(createCategory());
        return product;
    }

    public static ProductRequestDTO createProductRequestDTO() {
        return new ProductRequestDTO(createProduct());
    }

    public static ProductResponseDTO createProductResponseDTO() {
        return new ProductResponseDTO(createProduct());
    }

    public static Category createCategory() {
        return new Category(1L, "Books");
    }

    public static CategoryResponseDTO createCategoryResponseDTO() {
        return new CategoryResponseDTO(createCategory());
    }

    public static CategoryRequestDTO createCategoryRequestDTO() {
        return new CategoryRequestDTO("Books");
    }

}
