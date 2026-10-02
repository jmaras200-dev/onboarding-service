package com.portfolio.onboarding.application.usecase;

import com.portfolio.onboarding.application.port.KycValidationPort;
import com.portfolio.onboarding.application.validation.OnboardingValidationEngine;
import com.portfolio.onboarding.domain.exception.DomainValidationException;
import com.portfolio.onboarding.domain.exception.KycServiceUnavailableException;
import com.portfolio.onboarding.domain.exception.UserAlreadyExistsException;
import com.portfolio.onboarding.domain.model.Email;
import com.portfolio.onboarding.domain.model.NationalId;
import com.portfolio.onboarding.domain.model.OnboardingStatus;
import com.portfolio.onboarding.domain.model.OnboardingUser;
import com.portfolio.onboarding.domain.repository.OnboardingUserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application use case orchestrating the user registration flow.
 * <p>
 * This is a plain Java class with no Spring annotations — it is free of any
 * framework coupling and is wired as a {@code @Bean} in
 * {@code infrastructure/config/ApplicationConfig}. The {@code @Transactional}
 * boundary lives on the repository adapter in the infrastructure layer.
 *
 * <h3>Architecture notes</h3>
 * <ul>
 *   <li>No {@code @Service} — registered explicitly via {@code @Bean}.</li>
 *   <li>No {@code @Transactional} — transaction boundary delegated to
 *       {@link com.portfolio.onboarding.infrastructure.persistence.OnboardingUserRepositoryAdapter}.</li>
 *   <li>Returns {@link RegisterUserResult}, not the raw domain entity, satisfying
 *       the output-port contract and keeping {@code infrastructure/} from importing
 *       {@code domain/model/OnboardingUser} via the controller.</li>
 * </ul>
 */
@RequiredArgsConstructor
public class UserRegistrationUseCase {

    private static final Logger log = LoggerFactory.getLogger(UserRegistrationUseCase.class);

    private final OnboardingUserRepository userRepository;
    private final KycValidationPort kycValidationPort;
    private final OnboardingValidationEngine validationEngine;

    public RegisterUserResult register(RegisterUserCommand command) {
        // Value-object constructors are the single source of truth for format validation.
        // They throw DomainValidationException on null/invalid input — no redundant guards needed here.
        Email email = new Email(command.email().trim());
        NationalId nationalId = new NationalId(command.nationalId().trim());

        if (userRepository.existsByEmail(email)) {
            log.warn("Registration rejected: email {} already exists", email);
            throw new UserAlreadyExistsException("Email already exists: " + email);
        }

        if (userRepository.existsByNationalId(nationalId)) {
            log.warn("Registration rejected: nationalId {} already exists", nationalId);
            throw new UserAlreadyExistsException("National ID already exists: " + nationalId);
        }

        OnboardingStatus status = OnboardingStatus.PENDING_VALIDATION;
        try {
            kycValidationPort.validate(nationalId.value());
        } catch (KycServiceUnavailableException e) {
            log.warn("KYC service unavailable for nationalId: {}. Gracefully registering user with MANUAL_REVIEW status. Cause: {}",
                    nationalId, e.getMessage());
            status = OnboardingStatus.MANUAL_REVIEW;
        }

        OnboardingUser user = OnboardingUser.builder()
                .email(email)
                .nationalId(nationalId)
                .status(status)
                .build();

        if (!validationEngine.runAllValidations(user)) {
            log.warn("Onboarding rejected for email {}: dynamic domain validation failed", email);
            throw new DomainValidationException("Onboarding validation failed for user: " + email);
        }

        OnboardingUser savedUser = userRepository.save(user);
        log.info("Onboarding user registered successfully with id: {} and status: {}", savedUser.getId(), savedUser.getStatus());

        return new RegisterUserResult(
                savedUser.getId(),
                savedUser.getEmail().value(),
                savedUser.getNationalId().value(),
                savedUser.getStatus(),
                savedUser.getCreatedAt()
        );
    }
}
