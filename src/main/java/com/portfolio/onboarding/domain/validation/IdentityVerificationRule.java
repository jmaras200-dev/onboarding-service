package com.portfolio.onboarding.domain.validation;

import com.portfolio.onboarding.domain.model.OnboardingUser;

/**
 * Domain validation rule that verifies the identity of an onboarding user.
 *
 * <p><b>⚠️ STUB — not production-ready.</b> This implementation always returns
 * {@code true}, meaning every user passes identity verification unconditionally.
 * This stub exists to satisfy the {@link OnboardingValidationEngine} rule-set
 * while the real identity-verification integration is pending.
 *
 * <p>TODO: Replace with a real implementation that cross-references government
 *          identity databases or a third-party identity provider (e.g. via an
 *          application-layer port) before this service goes to production.
 *          Verify that the stub behaviour is covered by a unit test that
 *          explicitly documents the {@code return true} contract.
 */
public class IdentityVerificationRule implements OnboardingValidationRule {

    @Override
    public boolean isValid(OnboardingUser user) {
        // TODO: replace with real identity-verification logic before production release.
        return true;
    }
}
