package com.gustcustodio.e_commerce_api.services.exceptions;

public class OrderStatusException extends RuntimeException {

    public OrderStatusException(String message) {
        super(message);
    }

    public OrderStatusException() {
        super("Order can only be updated while awaiting payment");
    }

}
