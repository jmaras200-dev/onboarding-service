package com.portfolio.onboarding.domain.repository;

import com.portfolio.onboarding.domain.model.Email;
import com.portfolio.onboarding.domain.model.NationalId;
import com.portfolio.onboarding.domain.model.OnboardingUser;

import java.util.Optional;
import java.util.UUID;

public interface OnboardingUserRepository {

    OnboardingUser save(OnboardingUser user);

    boolean existsByEmail(Email email);

    boolean existsByNationalId(NationalId nationalId);

    Optional<OnboardingUser> findById(UUID id);
}
