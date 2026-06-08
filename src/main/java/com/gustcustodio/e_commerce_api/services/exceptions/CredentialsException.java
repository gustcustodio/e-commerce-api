package com.gustcustodio.e_commerce_api.services.exceptions;

public class CredentialsException extends RuntimeException {

    public CredentialsException(String message) {
        super(message);
    }

    public CredentialsException() {
        super("Invalid credentials");
    }

}
