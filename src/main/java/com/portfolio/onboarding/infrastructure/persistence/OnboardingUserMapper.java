package com.portfolio.onboarding.infrastructure.persistence;

import com.portfolio.onboarding.domain.model.Email;
import com.portfolio.onboarding.domain.model.NationalId;
import com.portfolio.onboarding.domain.model.OnboardingUser;

/**
 * Maps between the domain model {@link OnboardingUser} and the JPA entity
 * {@link OnboardingUserJpaEntity}. This mapper ensures the domain layer
 * remains free of persistence concerns.
 */
public final class OnboardingUserMapper {

    private OnboardingUserMapper() {
        // Utility class
    }

    /**
     * Converts a domain model to a JPA entity for persistence.
     */
    public static OnboardingUserJpaEntity toJpaEntity(OnboardingUser domain) {
        return OnboardingUserJpaEntity.builder()
                .id(domain.getId())
                .email(domain.getEmail().value())
                .nationalId(domain.getNationalId().value())
                .status(domain.getStatus())
                .version(domain.getVersion())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    /**
     * Converts a JPA entity back to the domain model.
     */
    public static OnboardingUser toDomain(OnboardingUserJpaEntity entity) {
        return OnboardingUser.builder()
                .id(entity.getId())
                .email(new Email(entity.getEmail()))
                .nationalId(new NationalId(entity.getNationalId()))
                .status(entity.getStatus())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
