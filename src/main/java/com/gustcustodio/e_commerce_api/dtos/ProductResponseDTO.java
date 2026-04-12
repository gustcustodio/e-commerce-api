package com.gustcustodio.e_commerce_api.dtos;

import com.gustcustodio.e_commerce_api.entities.Product;

import java.util.Set;
import java.util.stream.Collectors;

public record ProductResponseDTO(Long id, String name, String description, Double price, Integer quantity, Set<CategoryResponseDTO> categories) {

    public ProductResponseDTO(Product entity) {
        this(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getQuantity(),
                entity.getCategories().stream().map(CategoryResponseDTO::new).collect(Collectors.toSet())
        );
    }

}
