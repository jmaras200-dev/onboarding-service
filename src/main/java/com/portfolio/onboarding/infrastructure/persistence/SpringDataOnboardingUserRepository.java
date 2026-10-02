package com.portfolio.onboarding.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataOnboardingUserRepository extends JpaRepository<OnboardingUserJpaEntity, UUID> {

    boolean existsByEmail(String email);

    boolean existsByNationalId(String nationalId);
}
