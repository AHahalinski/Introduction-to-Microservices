package com.epam.microservices.resourceservice.controller;

import com.epam.microservices.resourceservice.exception.InvalidContentTypeException;
import com.epam.microservices.resourceservice.exception.InvalidMp3Exception;
import com.epam.microservices.resourceservice.exception.ResourceNotFoundException;
import com.epam.microservices.resourceservice.exception.SongServiceCommunicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private static final String HTTP_CODE_400 = "400";
    private static final String HTTP_CODE_404 = "404";
    private static final String HTTP_CODE_500 = "500";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put(ERROR_MESSAGE, ex.getMessage());
        error.put(ERROR_CODE, HTTP_CODE_404);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidMp3Exception.class)
    public ResponseEntity<Map<String, String>> handleInvalidMp3Exception(InvalidMp3Exception ex) {
        log.error("Invalid MP3: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put(ERROR_MESSAGE, ex.getMessage());
        error.put(ERROR_CODE, HTTP_CODE_400);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidContentTypeException.class)
    public ResponseEntity<Map<String, String>> handleInvalidContentTypeException(InvalidContentTypeException ex) {
        log.error("Invalid Content-Type: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put(ERROR_MESSAGE, ex.getMessage());
        error.put(ERROR_CODE, HTTP_CODE_400);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Invalid argument: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put(ERROR_MESSAGE, ex.getMessage());
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

    @ExceptionHandler(SongServiceCommunicationException.class)
    public ResponseEntity<Map<String, String>> handleSongServiceCommunicationException(SongServiceCommunicationException ex) {
        log.error("Song service communication error: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put(ERROR_MESSAGE, "Failed to communicate with Song Service");
        error.put(ERROR_CODE, HTTP_CODE_500);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
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

