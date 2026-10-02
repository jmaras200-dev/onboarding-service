package com.portfolio.onboarding.application.usecase;

public record RegisterUserCommand(
    String email,
    String nationalId
) {
}
