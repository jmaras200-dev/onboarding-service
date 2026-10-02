package com.portfolio.onboarding.infrastructure.config;

import com.portfolio.onboarding.application.validation.OnboardingValidationEngine;
import com.portfolio.onboarding.domain.model.Email;
import com.portfolio.onboarding.domain.model.NationalId;
import com.portfolio.onboarding.domain.model.OnboardingUser;
import com.portfolio.onboarding.domain.validation.FraudCheckRule;
import com.portfolio.onboarding.domain.validation.IdentityVerificationRule;
import com.portfolio.onboarding.domain.validation.OnboardingValidationRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OnboardingValidationEngineIntegrationTest {

    @Autowired
    private OnboardingValidationEngine validationEngine;

    @Autowired
    private List<OnboardingValidationRule> rules;

    @Test
    @DisplayName("Should inject all OnboardingValidationRule beans into OnboardingValidationEngine")
    void shouldInjectAllRulesIntoValidationEngine() {
        assertThat(validationEngine).isNotNull();
        assertThat(rules).isNotEmpty();

        List<Class<? extends OnboardingValidationRule>> ruleClasses = validationEngine.getValidationRules()
                .stream()
                .map(OnboardingValidationRule::getClass)
                .toList();

        assertThat(ruleClasses)
                .contains(IdentityVerificationRule.class, FraudCheckRule.class);

        OnboardingUser user = OnboardingUser.builder()
                .email(new Email("spring.engine@example.com"))
                .nationalId(new NationalId("NAT-SPRING-01"))
                .build();

        boolean result = validationEngine.runAllValidations(user);
        assertThat(result).isTrue();
    }
}
