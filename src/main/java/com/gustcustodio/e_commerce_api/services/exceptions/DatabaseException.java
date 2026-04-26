package com.gustcustodio.e_commerce_api.services.exceptions;

public class DatabaseException extends RuntimeException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException() {
        super("Integrity violation");
    }

}
