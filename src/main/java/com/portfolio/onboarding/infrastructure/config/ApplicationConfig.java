package com.portfolio.onboarding.infrastructure.config;

import com.portfolio.onboarding.application.port.KycValidationPort;
import com.portfolio.onboarding.application.usecase.UserRegistrationUseCase;
import com.portfolio.onboarding.application.validation.OnboardingValidationEngine;
import com.portfolio.onboarding.domain.repository.OnboardingUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Infrastructure configuration that wires application-layer use cases as
 * explicit {@code @Bean} definitions.
 * <p>
 * Because {@link UserRegistrationUseCase} carries no {@code @Service} annotation,
 * it is not picked up by classpath scanning. Registering it here keeps the
 * application layer framework-free while still participating in the Spring
 * application context via explicit dependency injection.
 */
@Configuration
public class ApplicationConfig {

    @Bean
    public UserRegistrationUseCase userRegistrationUseCase(
            OnboardingUserRepository userRepository,
            KycValidationPort kycValidationPort,
            OnboardingValidationEngine validationEngine) {
        return new UserRegistrationUseCase(userRepository, kycValidationPort, validationEngine);
    }
}
