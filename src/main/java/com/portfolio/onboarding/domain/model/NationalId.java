package com.portfolio.onboarding.domain.model;

import com.portfolio.onboarding.domain.exception.DomainValidationException;

/**
 * Value Object representing a validated national identification number.
 */
public record NationalId(String value) {

    public NationalId {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException("National ID must not be null or blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
