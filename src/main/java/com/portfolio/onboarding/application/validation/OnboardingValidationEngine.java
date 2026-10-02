package com.portfolio.onboarding.application.validation;

import com.portfolio.onboarding.domain.model.OnboardingUser;
import com.portfolio.onboarding.domain.validation.OnboardingValidationRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Dynamic validation engine implementing the Strategy pattern for the onboarding flow.
 * Sequentially executes all registered validation rules against an onboarding user.
 * <p>
 * This is a plain Java class with no framework annotations. It is wired as a
 * {@code @Bean} in {@code infrastructure/config/OnboardingValidationConfig}.
 * Logging uses {@code LoggerFactory} directly to keep the dependency explicit
 * rather than relying on the Lombok {@code @Slf4j} annotation processor.
 */
public class OnboardingValidationEngine {

    private static final Logger log = LoggerFactory.getLogger(OnboardingValidationEngine.class);

    private final List<OnboardingValidationRule> validationRules;

    public OnboardingValidationEngine(List<OnboardingValidationRule> validationRules) {
        this.validationRules = validationRules != null ? List.copyOf(validationRules) : List.of();
    }

    /**
     * Iterates through all registered validation rules.
     * If any rule returns false, validation halts and returns false.
     *
     * @param user the onboarding user to validate
     * @return true if all validation rules return true, false otherwise
     */
    public boolean runAllValidations(OnboardingUser user) {
        if (user == null) {
            log.warn("OnboardingUser is null; dynamic validation failed");
            return false;
        }

        log.debug("Running {} validation rule(s) for user: {}", validationRules.size(), user.getEmail());

        for (OnboardingValidationRule rule : validationRules) {
            if (!rule.isValid(user)) {
                log.warn("Validation rule {} rejected user: {}", rule.getClass().getSimpleName(), user.getEmail());
                return false;
            }
        }

        log.debug("All validation rules passed for user: {}", user.getEmail());
        return true;
    }

    public List<OnboardingValidationRule> getValidationRules() {
        return validationRules;
    }
}
