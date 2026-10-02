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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRegistrationUseCaseTest {

    @Mock
    private OnboardingUserRepository userRepository;

    @Mock
    private KycValidationPort kycValidationPort;

    @Mock
    private OnboardingValidationEngine validationEngine;

    private UserRegistrationUseCase userRegistrationUseCase;

    @BeforeEach
    void setUp() {
        userRegistrationUseCase = new UserRegistrationUseCase(userRepository, kycValidationPort, validationEngine);
    }

    @Test
    @DisplayName("Should successfully register a new user with PENDING_VALIDATION status when KYC passes")
    void shouldRegisterUserSuccessfully() {
        // Arrange
        RegisterUserCommand command = new RegisterUserCommand("alice@example.com", "NAT-12345");
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(userRepository.existsByNationalId(any(NationalId.class))).thenReturn(false);

        UUID generatedId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        when(validationEngine.runAllValidations(any(OnboardingUser.class))).thenReturn(true);
        when(userRepository.save(any(OnboardingUser.class))).thenAnswer(invocation -> {
            OnboardingUser user = invocation.getArgument(0);
            return OnboardingUser.builder()
                    .id(generatedId)
                    .email(user.getEmail())
                    .nationalId(user.getNationalId())
                    .status(user.getStatus())
                    .createdAt(now)
                    .build();
        });

        // Act
        RegisterUserResult result = userRegistrationUseCase.register(command);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(generatedId);
        assertThat(result.email()).isEqualTo("alice@example.com");
        assertThat(result.nationalId()).isEqualTo("NAT-12345");
        assertThat(result.status()).isEqualTo(OnboardingStatus.PENDING_VALIDATION);
        assertThat(result.createdAt()).isEqualTo(now);

        verify(kycValidationPort).validate("NAT-12345");

        ArgumentCaptor<OnboardingUser> captor = ArgumentCaptor.forClass(OnboardingUser.class);
        verify(userRepository).save(captor.capture());
        OnboardingUser captured = captor.getValue();
        assertThat(captured.getEmail().value()).isEqualTo("alice@example.com");
        assertThat(captured.getNationalId().value()).isEqualTo("NAT-12345");
        assertThat(captured.getStatus()).isEqualTo(OnboardingStatus.PENDING_VALIDATION);
    }

    @Test
    @DisplayName("Should gracefully register user with MANUAL_REVIEW status when KYC throws KycServiceUnavailableException")
    void shouldRegisterUserWithManualReviewWhenKycFails() {
        // Arrange
        RegisterUserCommand command = new RegisterUserCommand("dave@example.com", "NAT-FAIL-123");
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(userRepository.existsByNationalId(any(NationalId.class))).thenReturn(false);
        doThrow(new KycServiceUnavailableException("External KYC service is down"))
                .when(kycValidationPort).validate("NAT-FAIL-123");

        UUID generatedId = UUID.randomUUID();
        when(validationEngine.runAllValidations(any(OnboardingUser.class))).thenReturn(true);
        when(userRepository.save(any(OnboardingUser.class))).thenAnswer(invocation -> {
            OnboardingUser user = invocation.getArgument(0);
            return OnboardingUser.builder()
                    .id(generatedId)
                    .email(user.getEmail())
                    .nationalId(user.getNationalId())
                    .status(user.getStatus())
                    .createdAt(LocalDateTime.now())
                    .build();
        });

        // Act
        RegisterUserResult result = userRegistrationUseCase.register(command);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(generatedId);
        assertThat(result.status()).isEqualTo(OnboardingStatus.MANUAL_REVIEW);

        verify(kycValidationPort).validate("NAT-FAIL-123");

        ArgumentCaptor<OnboardingUser> captor = ArgumentCaptor.forClass(OnboardingUser.class);
        verify(userRepository).save(captor.capture());
        OnboardingUser captured = captor.getValue();
        assertThat(captured.getEmail().value()).isEqualTo("dave@example.com");
        assertThat(captured.getNationalId().value()).isEqualTo("NAT-FAIL-123");
        assertThat(captured.getStatus()).isEqualTo(OnboardingStatus.MANUAL_REVIEW);
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when email is already registered")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Arrange
        RegisterUserCommand command = new RegisterUserCommand("duplicate@example.com", "NAT-99999");
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userRegistrationUseCase.register(command))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email already exists: duplicate@example.com");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(kycValidationPort);
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when nationalId is already registered")
    void shouldThrowExceptionWhenNationalIdAlreadyExists() {
        // Arrange
        RegisterUserCommand command = new RegisterUserCommand("bob@example.com", "NAT-EXISTS");
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(userRepository.existsByNationalId(any(NationalId.class))).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userRegistrationUseCase.register(command))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("National ID already exists: NAT-EXISTS");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(kycValidationPort);
    }

    @Test
    @DisplayName("Should throw DomainValidationException when email is blank — value-object constructor is the guard")
    void shouldThrowDomainValidationExceptionWhenEmailIsBlank() {
        // The redundant IllegalArgumentException guards have been removed (M-1 fix).
        // Email's compact constructor is the single validation source and throws DomainValidationException.
        RegisterUserCommand command = new RegisterUserCommand("   ", "NAT-12345");

        assertThatThrownBy(() -> userRegistrationUseCase.register(command))
                .isInstanceOf(DomainValidationException.class);

        verifyNoInteractions(userRepository);
        verifyNoInteractions(kycValidationPort);
    }

    @Test
    @DisplayName("Should throw DomainValidationException when nationalId is blank — value-object constructor is the guard")
    void shouldThrowDomainValidationExceptionWhenNationalIdIsBlank() {
        // The redundant IllegalArgumentException guards have been removed (M-1 fix).
        // NationalId's compact constructor is the single validation source and throws DomainValidationException.
        RegisterUserCommand command = new RegisterUserCommand("carol@example.com", "");

        assertThatThrownBy(() -> userRegistrationUseCase.register(command))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("National ID must not be null or blank");

        verifyNoInteractions(userRepository);
        verifyNoInteractions(kycValidationPort);
    }

    @Test
    @DisplayName("Should handle ObjectOptimisticLockingFailureException when concurrent modification occurs during save")
    void shouldHandleOptimisticLockingFailureException() {
        RegisterUserCommand command = new RegisterUserCommand("concurrent@example.com", "NAT-CONCURRENT");
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(userRepository.existsByNationalId(any(NationalId.class))).thenReturn(false);

        when(validationEngine.runAllValidations(any(OnboardingUser.class))).thenReturn(true);
        when(userRepository.save(any(OnboardingUser.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(OnboardingUser.class, "123"));

        assertThatThrownBy(() -> userRegistrationUseCase.register(command))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);

        verify(userRepository).save(any(OnboardingUser.class));
    }

    @Test
    @DisplayName("Should throw DomainValidationException and reject registration when dynamic validation fails")
    void shouldThrowDomainValidationExceptionWhenDynamicValidationFails() {
        // Arrange
        RegisterUserCommand command = new RegisterUserCommand("eva@example.com", "NAT-FAIL-RULE");
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(userRepository.existsByNationalId(any(NationalId.class))).thenReturn(false);
        when(validationEngine.runAllValidations(any(OnboardingUser.class))).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userRegistrationUseCase.register(command))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Onboarding validation failed for user: eva@example.com");

        verify(kycValidationPort).validate("NAT-FAIL-RULE");
        verify(validationEngine).runAllValidations(any(OnboardingUser.class));
        verify(userRepository, never()).save(any());
    }
}
