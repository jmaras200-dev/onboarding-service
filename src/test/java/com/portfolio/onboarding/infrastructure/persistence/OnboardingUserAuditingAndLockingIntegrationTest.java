package com.portfolio.onboarding.infrastructure.persistence;

import com.portfolio.onboarding.domain.model.OnboardingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class OnboardingUserAuditingAndLockingIntegrationTest {

    @Autowired
    private SpringDataOnboardingUserRepository repository;

    @Autowired
    private OnboardingUserRepositoryAdapter adapter;

    @Test
    @DisplayName("Should automatically populate createdDate, lastModifiedDate and initialize version to 0")
    void shouldPopulateAuditFieldsAndVersionOnInsert() {
        OnboardingUserJpaEntity entity = OnboardingUserJpaEntity.builder()
                .email("audit-test@example.com")
                .nationalId("NAT-AUDIT-001")
                .status(OnboardingStatus.PENDING_VALIDATION)
                .build();

        OnboardingUserJpaEntity saved = repository.save(entity);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getVersion()).isEqualTo(0L);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should update lastModifiedDate and increment version on update")
    void shouldIncrementVersionAndUpdateLastModifiedDateOnUpdate() throws InterruptedException {
        OnboardingUserJpaEntity entity = OnboardingUserJpaEntity.builder()
                .email("audit-update@example.com")
                .nationalId("NAT-AUDIT-002")
                .status(OnboardingStatus.PENDING_VALIDATION)
                .build();

        OnboardingUserJpaEntity saved = repository.saveAndFlush(entity);
        Long initialVersion = saved.getVersion();
        LocalDateTime initialModified = saved.getUpdatedAt();

        Thread.sleep(50);

        saved.setStatus(OnboardingStatus.APPROVED);
        OnboardingUserJpaEntity updated = repository.saveAndFlush(saved);

        assertThat(updated.getVersion()).isEqualTo(initialVersion + 1);
        assertThat(updated.getStatus()).isEqualTo(OnboardingStatus.APPROVED);
        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(initialModified);
        assertThat(updated.getCreatedAt()).isEqualTo(saved.getCreatedAt());
    }

    @Test
    @DisplayName("Should throw ObjectOptimisticLockingFailureException when updating entity with stale version")
    void shouldThrowOptimisticLockingFailureOnConcurrentUpdate() {
        OnboardingUserJpaEntity entity = OnboardingUserJpaEntity.builder()
                .email("stale-version@example.com")
                .nationalId("NAT-AUDIT-003")
                .status(OnboardingStatus.PENDING_VALIDATION)
                .build();

        OnboardingUserJpaEntity saved = repository.saveAndFlush(entity);

        OnboardingUserJpaEntity staleCopy = OnboardingUserJpaEntity.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .nationalId(saved.getNationalId())
                .status(saved.getStatus())
                .version(saved.getVersion() + 99L)
                .createdAt(saved.getCreatedAt())
                .build();

        assertThatThrownBy(() -> repository.saveAndFlush(staleCopy))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
}
