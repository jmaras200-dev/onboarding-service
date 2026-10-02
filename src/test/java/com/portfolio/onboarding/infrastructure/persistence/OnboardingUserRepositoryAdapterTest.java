package com.portfolio.onboarding.infrastructure.persistence;

import com.portfolio.onboarding.domain.model.Email;
import com.portfolio.onboarding.domain.model.NationalId;
import com.portfolio.onboarding.domain.model.OnboardingStatus;
import com.portfolio.onboarding.domain.model.OnboardingUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnboardingUserRepositoryAdapterTest {

    @Mock
    private SpringDataOnboardingUserRepository springDataRepository;

    private OnboardingUserRepositoryAdapter repositoryAdapter;

    @BeforeEach
    void setUp() {
        repositoryAdapter = new OnboardingUserRepositoryAdapter(springDataRepository);
    }

    @Test
    @DisplayName("Should successfully save user through SpringData repository using mapper")
    void shouldSaveUserSuccessfully() {
        OnboardingUser user = OnboardingUser.builder()
                .email(new Email("user@example.com"))
                .nationalId(new NationalId("NAT-11111"))
                .status(OnboardingStatus.PENDING_VALIDATION)
                .build();

        UUID generatedId = UUID.randomUUID();
        OnboardingUserJpaEntity savedEntity = OnboardingUserJpaEntity.builder()
                .id(generatedId)
                .email("user@example.com")
                .nationalId("NAT-11111")
                .status(OnboardingStatus.PENDING_VALIDATION)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .build();

        when(springDataRepository.save(any(OnboardingUserJpaEntity.class))).thenReturn(savedEntity);

        OnboardingUser saved = repositoryAdapter.save(user);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(generatedId);
        assertThat(saved.getEmail().value()).isEqualTo("user@example.com");

        ArgumentCaptor<OnboardingUserJpaEntity> captor = ArgumentCaptor.forClass(OnboardingUserJpaEntity.class);
        verify(springDataRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("Should propagate ObjectOptimisticLockingFailureException when concurrent update occurs")
    void shouldPropagateOptimisticLockingFailure() {
        OnboardingUser user = OnboardingUser.builder()
                .id(UUID.randomUUID())
                .email(new Email("user@example.com"))
                .nationalId(new NationalId("NAT-11111"))
                .status(OnboardingStatus.PENDING_VALIDATION)
                .version(1L)
                .build();

        when(springDataRepository.save(any(OnboardingUserJpaEntity.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(OnboardingUserJpaEntity.class, user.getId()));

        assertThatThrownBy(() -> repositoryAdapter.save(user))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);

        verify(springDataRepository).save(any(OnboardingUserJpaEntity.class));
    }

    @Test
    @DisplayName("Should delegate existsByEmail to springDataRepository unwrapping the value object")
    void shouldDelegateExistsByEmail() {
        when(springDataRepository.existsByEmail("test@example.com")).thenReturn(true);

        boolean exists = repositoryAdapter.existsByEmail(new Email("test@example.com"));

        assertThat(exists).isTrue();
        verify(springDataRepository).existsByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should delegate existsByNationalId to springDataRepository unwrapping the value object")
    void shouldDelegateExistsByNationalId() {
        when(springDataRepository.existsByNationalId("NAT-999")).thenReturn(true);

        boolean exists = repositoryAdapter.existsByNationalId(new NationalId("NAT-999"));

        assertThat(exists).isTrue();
        verify(springDataRepository).existsByNationalId("NAT-999");
    }

    @Test
    @DisplayName("Should delegate findById to springDataRepository and map result to domain")
    void shouldDelegateFindById() {
        UUID id = UUID.randomUUID();
        OnboardingUserJpaEntity jpaEntity = OnboardingUserJpaEntity.builder()
                .id(id)
                .email("test@example.com")
                .nationalId("NAT-123")
                .status(OnboardingStatus.PENDING_VALIDATION)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .build();

        when(springDataRepository.findById(id)).thenReturn(Optional.of(jpaEntity));

        Optional<OnboardingUser> found = repositoryAdapter.findById(id);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(id);
        assertThat(found.get().getEmail().value()).isEqualTo("test@example.com");
        verify(springDataRepository).findById(id);
    }
}
