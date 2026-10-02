package com.portfolio.onboarding.domain.model;

import com.portfolio.onboarding.domain.exception.DomainValidationException;

/**
 * Value Object representing a validated email address.
 */
public record Email(String value) {

    public Email {
        if (value == null || !value.matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new DomainValidationException("Invalid email: " + value);
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
