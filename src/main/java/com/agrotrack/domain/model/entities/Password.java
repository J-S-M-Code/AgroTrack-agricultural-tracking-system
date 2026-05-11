package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;

import java.util.regex.Pattern;

public class Password {
    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{9,}$";
    private static final Pattern PATTERN = Pattern.compile(PASSWORD_PATTERN);

    private final String value;

    public Password(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new BusinessRuleViolationsException("La contraseña no puede estar vacia.");
        }
        if (!PATTERN.matcher(password).matches()) {
            throw new BusinessRuleViolationsException("La contraseña debe ser mayor a 8 digitos y contener al menos una mayúscula, una minúscula y un número.");
        }
        if (hasIncreasingNumberSequence(password)){
            throw new BusinessRuleViolationsException("Por razones de seguridad, la contraseña no debe contener secuencias de números crecientes (ej. 123).");
        }
    }

    private boolean hasIncreasingNumberSequence(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            char c1 = password.charAt(i);
            char c2 = password.charAt(i + 1);
            char c3 = password.charAt(i + 2);

            if(Character.isDigit(c1) && Character.isDigit(c2) && Character.isDigit(c3)){
                if((c2 == c1 + 1) && (c3 == c2 + 1)){
                    return true;
                }
            }
        }
        return false;
    }

    public String getValue() {
        return value;
    }
}
