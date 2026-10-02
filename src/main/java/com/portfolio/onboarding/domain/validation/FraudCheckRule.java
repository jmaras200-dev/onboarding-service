package com.portfolio.onboarding.domain.validation;

import com.portfolio.onboarding.domain.model.OnboardingUser;

/**
 * Domain validation rule that checks an onboarding user for fraud indicators.
 *
 * <p><b>⚠️ STUB — not production-ready.</b> This implementation always returns
 * {@code true}, meaning every user passes the fraud check unconditionally.
 * This stub exists to satisfy the {@link OnboardingValidationEngine} rule-set
 * while the real fraud-scoring integration is pending.
 *
 * <p>TODO: Replace with a real implementation that calls the fraud-scoring
 *          domain service or anti-fraud microservice before this service
 *          goes to production. Verify that the stub behaviour is covered by
 *          a unit test that explicitly documents the {@code return true} contract.
 */
public class FraudCheckRule implements OnboardingValidationRule {

    @Override
    public boolean isValid(OnboardingUser user) {
        // TODO: replace with real fraud-scoring logic before production release.
        return true;
    }
}
