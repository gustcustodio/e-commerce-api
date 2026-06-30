package com.gustcustodio.e_commerce_api.factories;

import com.gustcustodio.e_commerce_api.entities.*;

import java.time.Instant;

public class OrderFactory {

    public static Order createValidOrder() {
        User client = UserFactory.createUserAdminAndClient();
        Order order = new Order(1L, Instant.now(), OrderStatus.WAITING_PAYMENT, client);
        OrderItem orderItem = new OrderItem(order, ProductFactory.createValidProduct(), 2, 10.0);
        order.getOrderItems().add(orderItem);
        return order;
    }

}
