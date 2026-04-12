package com.gustcustodio.e_commerce_api.dtos;

import com.gustcustodio.e_commerce_api.entities.Category;

public record CategoryResponseDTO(Long id, String name) {

    public CategoryResponseDTO(Category entity) {
        this(entity.getId(), entity.getName());
    }

}
