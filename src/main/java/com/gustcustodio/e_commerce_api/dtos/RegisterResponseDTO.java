package com.gustcustodio.e_commerce_api.dtos;

import com.gustcustodio.e_commerce_api.entities.User;

public record RegisterResponseDTO(Long id, String name, String email) {

    public RegisterResponseDTO(User entity) {
        this(
                entity.getId(),
                entity.getName(),
                entity.getEmail()
        );
    }

}
