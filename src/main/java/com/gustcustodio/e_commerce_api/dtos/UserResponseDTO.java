package com.gustcustodio.e_commerce_api.dtos;

import com.gustcustodio.e_commerce_api.entities.User;

public record UserResponseDTO(Long id, String name, String cpf, String phone, String email) {

    public UserResponseDTO(User entity) {
        this(
                entity.getId(),
                entity.getName(),
                entity.getCpf(),
                entity.getPhone(),
                entity.getEmail()
        );
    }

}
