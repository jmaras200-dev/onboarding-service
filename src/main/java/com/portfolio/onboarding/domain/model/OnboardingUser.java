package com.portfolio.onboarding.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model representing a user going through the onboarding process.
 * This is a pure POJO with zero framework dependencies.
 * <p>
 * Infrastructure-owned fields ({@code id}, {@code version}, {@code createdAt},
 * {@code updatedAt}) are populated exclusively via the Lombok {@code @Builder}
 * in {@link com.portfolio.onboarding.infrastructure.persistence.OnboardingUserMapper#toDomain}.
 * No public setters are exposed — all mutation goes through explicit builder
 * reconstruction or the domain behavior methods below.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingUser {

    private UUID id;
    private Email email;
    private NationalId nationalId;
    private OnboardingStatus status;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Behavior-driven state transitions ──

    public void approve() {
        this.status = OnboardingStatus.APPROVED;
    }

    public void reject() {
        this.status = OnboardingStatus.REJECTED;
    }

    public void markForManualReview() {
        this.status = OnboardingStatus.MANUAL_REVIEW;
    }
}
