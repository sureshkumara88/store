package com.example.store.controller;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String ERROR = "error";
    private static final String MESSAGE = "message";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(ERROR, "validation_failed");
        body.put(MESSAGE, "One or more fields are invalid");
        body.put(
                "details",
                ex.getBindingResult().getFieldErrors().stream()
                        .map(fe -> Map.of(
                                MESSAGE, fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid request"))
                        .toList());
        return body;
    }

    @ExceptionHandler({DataIntegrityViolationException.class, DataAccessException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleData(Exception ex) {
        return Map.of(ERROR, "data_access_error", MESSAGE, ex.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleRse(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(Map.of(
                        ERROR,
                        ex.getStatusCode().toString(),
                        MESSAGE,
                        ex.getReason() == null ? ERROR : ex.getReason()));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleGeneric(Exception ex) {
        return Map.of(ERROR, "unexpected_error", MESSAGE, ex.getMessage());
    }
}
