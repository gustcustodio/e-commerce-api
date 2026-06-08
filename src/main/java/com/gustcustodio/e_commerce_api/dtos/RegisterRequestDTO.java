package com.gustcustodio.e_commerce_api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank(message = "Required field")
        @Size(min = 5, max = 50, message = "The name needs to be between 5 and 50 characters")
        String name,

        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "Required field")
        @Size(min = 8, message = "The password must be at least 8 characters")
        String password
) {
}
