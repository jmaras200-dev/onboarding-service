package com.portfolio.onboarding.infrastructure.web.dto;

import com.portfolio.onboarding.application.usecase.RegisterUserResult;
import com.portfolio.onboarding.domain.model.OnboardingStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * HTTP response DTO for the user registration endpoint.
 * <p>
 * Maps from {@link RegisterUserResult} — the application-layer output port —
 * so this infrastructure DTO never reaches into {@code domain/model/} directly.
 */
public record OnboardingUserResponse(
        UUID id,
        String email,
        String nationalId,
        OnboardingStatus status,
        LocalDateTime createdAt
) {
    public static OnboardingUserResponse fromResult(RegisterUserResult result) {
        return new OnboardingUserResponse(
                result.id(),
                result.email(),
                result.nationalId(),
                result.status(),
                result.createdAt()
        );
    }
}
