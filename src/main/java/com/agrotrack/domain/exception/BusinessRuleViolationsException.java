package com.agrotrack.domain.exception;

public class BusinessRuleViolationsException extends RuntimeException {
    public BusinessRuleViolationsException(String message) {
        super(message);
    }
}
