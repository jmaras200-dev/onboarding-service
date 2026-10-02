package com.portfolio.onboarding.domain.validation;

import com.portfolio.onboarding.domain.model.OnboardingUser;

/**
 * Strategy interface representing a domain validation rule for user onboarding.
 */
public interface OnboardingValidationRule {

    /**
     * Evaluates whether the given user satisfies this validation rule.
     *
     * @param user the onboarding user being evaluated
     * @return true if valid, false otherwise
     */
    boolean isValid(OnboardingUser user);
}
