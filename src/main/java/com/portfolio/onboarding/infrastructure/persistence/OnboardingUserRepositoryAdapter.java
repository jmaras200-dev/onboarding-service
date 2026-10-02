package com.portfolio.onboarding.infrastructure.persistence;

import com.portfolio.onboarding.domain.model.Email;
import com.portfolio.onboarding.domain.model.NationalId;
import com.portfolio.onboarding.domain.model.OnboardingUser;
import com.portfolio.onboarding.domain.repository.OnboardingUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure adapter implementing the domain repository port.
 * <p>
 * {@code @Transactional} is placed here — at the outermost infrastructure
 * boundary — rather than on the application use case. This is the correct
 * Clean Architecture placement: the transaction is a persistence concern,
 * not a business concern.
 */
@Repository
@RequiredArgsConstructor
public class OnboardingUserRepositoryAdapter implements OnboardingUserRepository {

    private final SpringDataOnboardingUserRepository springDataRepository;

    @Override
    @Transactional
    public OnboardingUser save(OnboardingUser user) {
        OnboardingUserJpaEntity jpaEntity = OnboardingUserMapper.toJpaEntity(user);
        OnboardingUserJpaEntity savedEntity = springDataRepository.save(jpaEntity);
        return OnboardingUserMapper.toDomain(savedEntity);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return springDataRepository.existsByEmail(email.value());
    }

    @Override
    public boolean existsByNationalId(NationalId nationalId) {
        return springDataRepository.existsByNationalId(nationalId.value());
    }

    @Override
    public Optional<OnboardingUser> findById(UUID id) {
        return springDataRepository.findById(id)
                .map(OnboardingUserMapper::toDomain);
    }
}
