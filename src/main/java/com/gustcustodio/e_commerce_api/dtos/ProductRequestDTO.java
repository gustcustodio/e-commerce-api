package com.gustcustodio.e_commerce_api.dtos;

import com.gustcustodio.e_commerce_api.entities.Product;
import jakarta.validation.constraints.*;

import java.util.Set;
import java.util.stream.Collectors;

public record ProductRequestDTO(
        @NotBlank(message = "Required field")
        @Size(min = 5, max = 50, message = "The name needs to be between 5 and 50 characters")
        String name,

        @NotBlank(message = "Required field")
        @Size(min = 10, message = "The description must have at least 10 characters")
        String description,

        @Positive(message = "Price should be a positive value")
        Double price,

        @NotNull
        @PositiveOrZero
        Integer quantity,

        @Size(min = 1, message = "The product must have at least one category")
        Set<CategoryResponseDTO> categories
) {

    public ProductRequestDTO(Product entity) {
        this(
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getQuantity(),
                entity.getCategories().stream().map(CategoryResponseDTO::new).collect(Collectors.toSet())
        );
    }

}
