package com.gustcustodio.e_commerce_api.dtos;

import com.gustcustodio.e_commerce_api.entities.Order;
import com.gustcustodio.e_commerce_api.entities.OrderStatus;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

public record OrderResponseDTO(Long id, Instant orderDate, OrderStatus orderStatus, Set<OrderItemDTO> items, Double total) {

    public OrderResponseDTO(Order entity) {
        this(
                entity.getId(),
                entity.getOrderDate(),
                entity.getOrderStatus(),
                entity.getOrderItems().stream().map(OrderItemDTO::new).collect(Collectors.toSet()),
                entity.getTotalOrderPrice()
        );
    }

}
