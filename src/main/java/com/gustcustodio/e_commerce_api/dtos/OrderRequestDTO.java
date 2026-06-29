package com.gustcustodio.e_commerce_api.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderRequestDTO(@NotEmpty(message = "Order must have at least one item") @Valid List<OrderItemDTO> orderItems) {
}
