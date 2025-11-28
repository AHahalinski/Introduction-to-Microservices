package com.epam.microservices.songservice.controller;

import com.epam.microservices.songservice.exception.SongAlreadyExistsException;
import com.epam.microservices.songservice.exception.SongNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String ERROR_CODE = "errorCode";
    private static final String HTTP_CODE_404 = "404";
    private static final String HTTP_CODE_409 = "409";
    private static final String HTTP_CODE_400 = "400";
    private static final String DETAILS = "details";
    private static final String HTTP_CODE_500 = "500";

    @ExceptionHandler(SongNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleSongNotFoundException(SongNotFoundException ex) {
        log.error("Song not found: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put(ERROR_MESSAGE, ex.getMessage());
        error.put(ERROR_CODE, HTTP_CODE_404);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(SongAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleSongAlreadyExistsException(SongAlreadyExistsException ex) {
        log.error("Song already exists: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put(ERROR_MESSAGE, ex.getMessage());
        error.put(ERROR_CODE, HTTP_CODE_409);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Invalid argument: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put(ERROR_MESSAGE, ex.getMessage());
        error.put(ERROR_CODE, HTTP_CODE_400);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation error");
        Map<String, Object> error = new HashMap<>();
        Map<String, String> details = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            details.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        error.put(ERROR_MESSAGE, "Validation error");
        error.put(DETAILS, details);
        error.put(ERROR_CODE, HTTP_CODE_400);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String invalidValue = ex.getValue() != null ? ex.getValue().toString() : "null";
        String errorMessage = String.format("Invalid value '%s' for ID. Must be a positive integer", invalidValue);
        
        log.error("Invalid argument type: {}", errorMessage);
        Map<String, String> error = new HashMap<>();
        error.put(ERROR_MESSAGE, errorMessage);
        error.put(ERROR_CODE, HTTP_CODE_400);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        log.error("Internal server error", ex);
        Map<String, String> error = new HashMap<>();
        error.put(ERROR_MESSAGE, "An error occurred on the server");
        error.put(ERROR_CODE, HTTP_CODE_500);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}


