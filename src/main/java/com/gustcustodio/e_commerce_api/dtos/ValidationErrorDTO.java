package com.gustcustodio.e_commerce_api.dtos;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ValidationErrorDTO extends CustomErrorDTO {

    List<FieldMessageDTO> errors = new ArrayList<>();

    public ValidationErrorDTO(Instant timestamp, Integer status, String error, String path) {
        super(timestamp, status, error, path);
    }

    public List<FieldMessageDTO> getErrors() {
        return errors;
    }

    public void setErrors(String fieldName, String message) {
        errors.add(new FieldMessageDTO(fieldName, message));
    }

}
