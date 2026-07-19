package com.gustcustodio.e_commerce_api.controllers.handler;

import com.gustcustodio.e_commerce_api.dtos.CustomErrorDTO;
import com.gustcustodio.e_commerce_api.dtos.ValidationErrorDTO;
import com.gustcustodio.e_commerce_api.services.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class ControllerExceptionHandler {

    // ! --------------------  HTTP 400 ERROR -------------------- ! //

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<CustomErrorDTO> toDatabaseException(DatabaseException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        CustomErrorDTO customErrorDTO = new CustomErrorDTO(Instant.now(), status.value(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(customErrorDTO);
    }

    @ExceptionHandler(CredentialsException.class)
    public ResponseEntity<CustomErrorDTO> toCredentialsException(CredentialsException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        CustomErrorDTO customErrorDTO = new CustomErrorDTO(Instant.now(), status.value(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(customErrorDTO);
    }

    @ExceptionHandler(OrderStatusException.class)
    public ResponseEntity<CustomErrorDTO> toOrderStatusException(OrderStatusException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        CustomErrorDTO customErrorDTO = new CustomErrorDTO(Instant.now(), status.value(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(customErrorDTO);
    }

    // ! -------------------- HTTP 403 ERROR -------------------- ! //

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<CustomErrorDTO> toForbiddenException(ForbiddenException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        CustomErrorDTO customErrorDTO = new CustomErrorDTO(Instant.now(), status.value(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(customErrorDTO);
    }

    // ! -------------------- HTTP 404 ERROR -------------------- ! //

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CustomErrorDTO> toResourceNotFoundException(ResourceNotFoundException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        CustomErrorDTO customErrorDTO = new CustomErrorDTO(Instant.now(), status.value(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(customErrorDTO);
    }

    // ! -------------------- HTTP 422 ERROR -------------------- ! //

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorDTO> toMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        ValidationErrorDTO validationErrorDTO = new ValidationErrorDTO(Instant.now(), status.value(), e.getMessage(), request.getRequestURI());
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            validationErrorDTO.setErrors(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.status(status).body(validationErrorDTO);
    }

}
