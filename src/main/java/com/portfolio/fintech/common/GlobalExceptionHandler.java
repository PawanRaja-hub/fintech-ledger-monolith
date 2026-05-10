package com.portfolio.fintech.common;

import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<Map<String, Object>> business(BusinessException ex) {
        return error(ex.status(), ex.getMessage());
    }

    @ExceptionHandler({OptimisticLockException.class})
    ResponseEntity<Map<String, Object>> optimisticLock(Exception ex) {
        return error(HttpStatus.CONFLICT, "Concurrent update detected. Retry with the same idempotency key.");
    }

    @ExceptionHandler({DataIntegrityViolationException.class})
    ResponseEntity<Map<String, Object>> dataIntegrity(Exception ex) {
        return error(HttpStatus.CONFLICT, "Request violates a uniqueness or integrity rule.");
    }

    @ExceptionHandler({AccessDeniedException.class})
    ResponseEntity<Map<String, Object>> denied(Exception ex) {
        return error(HttpStatus.FORBIDDEN, "You are not allowed to perform this operation.");
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    ResponseEntity<Map<String, Object>> validation(Exception ex) {
        return error(HttpStatus.BAD_REQUEST, "Validation failed: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> generic(Exception ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error. Check logs for correlation details.");
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
