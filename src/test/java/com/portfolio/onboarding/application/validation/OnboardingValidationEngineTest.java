package com.portfolio.onboarding.application.validation;

import com.portfolio.onboarding.domain.model.Email;
import com.portfolio.onboarding.domain.model.NationalId;
import com.portfolio.onboarding.domain.model.OnboardingStatus;
import com.portfolio.onboarding.domain.model.OnboardingUser;
import com.portfolio.onboarding.domain.validation.FraudCheckRule;
import com.portfolio.onboarding.domain.validation.IdentityVerificationRule;
import com.portfolio.onboarding.domain.validation.OnboardingValidationRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OnboardingValidationEngineTest {

    @Test
    @DisplayName("Should return true when all validation rules evaluate to true")
    void shouldReturnTrueWhenAllRulesPass() {
        OnboardingValidationRule rule1 = mock(OnboardingValidationRule.class);
        OnboardingValidationRule rule2 = mock(OnboardingValidationRule.class);
        when(rule1.isValid(any())).thenReturn(true);
        when(rule2.isValid(any())).thenReturn(true);

        OnboardingValidationEngine engine = new OnboardingValidationEngine(List.of(rule1, rule2));
        OnboardingUser user = OnboardingUser.builder()
                .email(new Email("test@example.com"))
                .nationalId(new NationalId("ID-12345"))
                .status(OnboardingStatus.PENDING_VALIDATION)
                .build();

        boolean result = engine.runAllValidations(user);

        assertThat(result).isTrue();
        verify(rule1).isValid(user);
        verify(rule2).isValid(user);
    }

    @Test
    @DisplayName("Should return false and short-circuit when a validation rule fails")
    void shouldReturnFalseWhenAnyRuleFails() {
        OnboardingValidationRule rule1 = mock(OnboardingValidationRule.class);
        OnboardingValidationRule rule2 = mock(OnboardingValidationRule.class);
        when(rule1.isValid(any())).thenReturn(false);

        OnboardingValidationEngine engine = new OnboardingValidationEngine(List.of(rule1, rule2));
        OnboardingUser user = OnboardingUser.builder()
                .email(new Email("fraud@example.com"))
                .nationalId(new NationalId("ID-99999"))
                .status(OnboardingStatus.PENDING_VALIDATION)
                .build();

        boolean result = engine.runAllValidations(user);

        assertThat(result).isFalse();
        verify(rule1).isValid(user);
        verifyNoInteractions(rule2);
    }

    @Test
    @DisplayName("Should return true when rules list is empty")
    void shouldReturnTrueWhenRulesListIsEmpty() {
        OnboardingValidationEngine engine = new OnboardingValidationEngine(List.of());
        OnboardingUser user = OnboardingUser.builder()
                .email(new Email("test@example.com"))
                .nationalId(new NationalId("ID-12345"))
                .build();

        boolean result = engine.runAllValidations(user);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should handle null rules list gracefully and return true")
    void shouldHandleNullRulesListGracefully() {
        OnboardingValidationEngine engine = new OnboardingValidationEngine(null);
        OnboardingUser user = OnboardingUser.builder()
                .email(new Email("test@example.com"))
                .nationalId(new NationalId("ID-12345"))
                .build();

        boolean result = engine.runAllValidations(user);

        assertThat(result).isTrue();
        assertThat(engine.getValidationRules()).isEmpty();
    }

    @Test
    @DisplayName("Should return false when user is null")
    void shouldReturnFalseWhenUserIsNull() {
        OnboardingValidationRule rule = mock(OnboardingValidationRule.class);
        OnboardingValidationEngine engine = new OnboardingValidationEngine(List.of(rule));

        boolean result = engine.runAllValidations(null);

        assertThat(result).isFalse();
        verifyNoInteractions(rule);
    }

    @Test
    @DisplayName("Dummy domain rules IdentityVerificationRule and FraudCheckRule should return true")
    void shouldVerifyDummyRulesReturnTrue() {
        IdentityVerificationRule identityRule = new IdentityVerificationRule();
        FraudCheckRule fraudRule = new FraudCheckRule();

        OnboardingUser user = OnboardingUser.builder()
                .email(new Email("valid@example.com"))
                .nationalId(new NationalId("NAT-OK"))
                .build();

        assertThat(identityRule.isValid(user)).isTrue();
        assertThat(fraudRule.isValid(user)).isTrue();

        OnboardingValidationEngine engine = new OnboardingValidationEngine(List.of(identityRule, fraudRule));
        assertThat(engine.runAllValidations(user)).isTrue();
    }
}
