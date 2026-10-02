package com.portfolio.onboarding.domain.exception;

public class KycServiceUnavailableException extends RuntimeException {

    public KycServiceUnavailableException(String message) {
        super(message);
    }

    public KycServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
