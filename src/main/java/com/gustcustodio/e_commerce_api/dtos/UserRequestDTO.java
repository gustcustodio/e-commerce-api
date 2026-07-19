package com.gustcustodio.e_commerce_api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

public record UserRequestDTO(
        @NotBlank(message = "Required field")
        @Size(min = 5, max = 50, message = "The name needs to be between 5 and 50 characters")
        String name,

        @NotBlank(message = "Required field")
        @CPF(message = "CPF should be valid")
        String cpf,

        @NotBlank(message = "Required field")
        @Pattern(regexp = "^\\d{9}$", message = "Phone must be exactly 9 digits")
        String phone,

        @NotBlank(message = "Required field")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "Required field")
        @Size(min = 8, message = "The password must be at least 8 characters")
        String password
) {
}
