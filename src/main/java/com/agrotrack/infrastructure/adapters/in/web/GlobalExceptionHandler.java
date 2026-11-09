package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessRuleViolationsException.class)
    public ResponseEntity<Map<String, String>> handleBusinessRuleViolationsException(BusinessRuleViolationsException ex) {
        System.err.println("BusinessRuleViolationsException thrown: " + ex.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        System.err.println("MaxUploadSizeExceededException thrown: " + ex.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("error", "El tamaño de los archivos supera el límite máximo permitido (10MB por imagen, 25MB por solicitud)");
        return new ResponseEntity<>(response, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        System.err.println("Generic Exception caught: " + ex.getClass().getName() + " - " + ex.getMessage());
        ex.printStackTrace();
        Map<String, String> response = new HashMap<>();
        response.put("error", "Internal server error: " + ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
