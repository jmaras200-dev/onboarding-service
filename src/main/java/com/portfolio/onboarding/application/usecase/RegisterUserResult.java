package com.portfolio.onboarding.application.usecase;

import com.portfolio.onboarding.domain.model.OnboardingStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Output DTO returned by {@link UserRegistrationUseCase#register(RegisterUserCommand)}.
 * <p>
 * Keeping this record in the {@code application} layer ensures the controller
 * in {@code infrastructure/web} never holds a reference to the raw domain entity,
 * satisfying the Ports &amp; Adapters output-port contract.
 */
public record RegisterUserResult(
        UUID id,
        String email,
        String nationalId,
        OnboardingStatus status,
        LocalDateTime createdAt
) {
}
