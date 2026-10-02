package com.portfolio.onboarding.infrastructure.kyc;

import com.portfolio.onboarding.application.port.KycValidationPort;
import com.portfolio.onboarding.domain.exception.KycServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class StubKycAdapterCircuitBreakerIntegrationTest {

    @Autowired
    private KycValidationPort kycValidationPort;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void setUp() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(StubKycAdapter.CIRCUIT_BREAKER_INSTANCE);
        cb.reset();
    }

    @Test
    @DisplayName("Should execute through Resilience4j CircuitBreaker and invoke fallback on failure")
    void shouldInvokeFallbackThroughCircuitBreaker() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(StubKycAdapter.CIRCUIT_BREAKER_INSTANCE);
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        assertThatThrownBy(() -> kycValidationPort.validate("NAT-FAIL-INTEG"))
                .isInstanceOf(KycServiceUnavailableException.class);
    }

    @Test
    @DisplayName("Should open circuit breaker after reaching failure rate threshold")
    void shouldTransitionToOpenStateAfterThresholdExceeded() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(StubKycAdapter.CIRCUIT_BREAKER_INSTANCE);
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        for (int i = 0; i < 5; i++) {
            final String nationalId = "NAT-FAIL-" + i;
            assertThatThrownBy(() -> kycValidationPort.validate(nationalId))
                    .isInstanceOf(KycServiceUnavailableException.class);
        }

        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // When circuit is OPEN, calls are short-circuited and fallback is invoked
        assertThatThrownBy(() -> kycValidationPort.validate("NAT-ANY-999"))
                .isInstanceOf(KycServiceUnavailableException.class);
    }
}
