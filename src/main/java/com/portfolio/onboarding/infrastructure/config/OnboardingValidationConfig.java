package com.portfolio.onboarding.infrastructure.config;

import com.portfolio.onboarding.application.validation.OnboardingValidationEngine;
import com.portfolio.onboarding.domain.validation.FraudCheckRule;
import com.portfolio.onboarding.domain.validation.IdentityVerificationRule;
import com.portfolio.onboarding.domain.validation.OnboardingValidationRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Infrastructure configuration registering the OnboardingValidationEngine bean
 * and all domain validation rules as explicit Spring beans.
 */
@Configuration
public class OnboardingValidationConfig {

    @Bean
    public FraudCheckRule fraudCheckRule() {
        return new FraudCheckRule();
    }

    @Bean
    public IdentityVerificationRule identityVerificationRule() {
        return new IdentityVerificationRule();
    }

    @Bean
    public OnboardingValidationEngine onboardingValidationEngine(List<OnboardingValidationRule> validationRules) {
        return new OnboardingValidationEngine(validationRules);
    }
}
