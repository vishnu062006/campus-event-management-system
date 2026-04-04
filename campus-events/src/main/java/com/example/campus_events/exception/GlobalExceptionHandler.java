package com.example.campus_events.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

/**
 * Global exception handler — catches all exceptions in one place
 * instead of try-catch in every controller
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation errors — empty name, short password etc.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
        System.out.println("[ERROR] IllegalArgumentException: " + e.getMessage());
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    /**
     * Handles business logic errors — event full, already registered etc.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalState(IllegalStateException e) {
        System.out.println("[ERROR] IllegalStateException: " + e.getMessage());
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    /**
     * Handles any unexpected errors
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneral(Exception e) {
        System.out.println("[ERROR] Unexpected error: " + e.getMessage());
        return ResponseEntity.internalServerError()
                .body(Map.of("error", "Something went wrong. Please try again."));
    }
}