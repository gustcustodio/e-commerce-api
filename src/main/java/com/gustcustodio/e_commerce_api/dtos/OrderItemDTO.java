package com.gustcustodio.e_commerce_api.dtos;

import com.gustcustodio.e_commerce_api.entities.OrderItem;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemDTO(
        @NotNull(message = "Product ID is required")
        @Positive(message = "Product ID must be positive")
        Long productId,

        String name,

        @Positive(message = "Price should be a positive value")
        Double price,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity should be a positive value")
        Integer quantity,

        Double subTotal
) {

    public OrderItemDTO(OrderItem entity) {
        this(
                entity.getProduct().getId(),
                entity.getProduct().getName(),
                entity.getPrice(),
                entity.getQuantity(),
                entity.getSubTotalOrderItemPrice()
        );
    }

}
