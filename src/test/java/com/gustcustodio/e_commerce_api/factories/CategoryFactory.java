package com.gustcustodio.e_commerce_api.factories;

import com.gustcustodio.e_commerce_api.dtos.CategoryRequestDTO;
import com.gustcustodio.e_commerce_api.dtos.CategoryResponseDTO;
import com.gustcustodio.e_commerce_api.entities.Category;

public class CategoryFactory {

    public static Category createCategory() {
        return new Category(1L, "Books");
    }

    public static Category createInvalidCategory() {
        return new Category(1000L, "Invalid Category");
    }

    public static CategoryResponseDTO createCategoryResponseDTO() {
        return new CategoryResponseDTO(createCategory());
    }

    public static CategoryRequestDTO createCategoryRequestDTO() {
        return new CategoryRequestDTO("Books");
    }

}
