package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessRuleViolationsException.class)
    public ResponseEntity<Map<String, String>> handleBusinessRuleViolationsException(BusinessRuleViolationsException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
