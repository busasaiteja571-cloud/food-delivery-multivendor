package com.fooddelivery.food_delivery_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

// @RestControllerAdvice applies these handlers GLOBALLY, across every
// @RestController in the app — we don't need try/catch in every method.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Catches any IllegalArgumentException thrown anywhere in a Controller
    // or Service call chain (like our duplicate-email and login checks)
    // and converts it into a clean JSON 400 response instead of a 500.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
    
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage()));
    }
    
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT) // 409 — the request is valid, but conflicts with current state
                .body(Map.of("error", ex.getMessage()));
    }
}