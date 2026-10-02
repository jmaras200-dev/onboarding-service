package com.portfolio.onboarding.infrastructure.kyc;

import com.portfolio.onboarding.domain.exception.KycServiceUnavailableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StubKycAdapterTest {

    @Test
    @DisplayName("Should successfully validate nationalId when failure simulation is disabled")
    void shouldValidateSuccessfullyWhenNoFailure() {
        StubKycAdapter adapter = new StubKycAdapter(false, 0.0);

        assertThatCode(() -> adapter.validate("NAT-SUCCESS-001"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw KycServiceUnavailableException when deterministic failure is triggered")
    void shouldThrowExceptionWhenDeterministicFailureTriggered() {
        StubKycAdapter adapter = new StubKycAdapter(false, 0.0);

        assertThatThrownBy(() -> adapter.validate("NAT-FAIL-999"))
                .isInstanceOf(KycServiceUnavailableException.class)
                .hasMessageContaining("Deterministic simulated KYC service failure");
    }

    @Test
    @DisplayName("Fallback method should wrap unexpected exceptions as KycServiceUnavailableException")
    void fallbackShouldWrapExceptions() {
        StubKycAdapter adapter = new StubKycAdapter(false, 0.0);

        assertThatThrownBy(() -> adapter.kycFallback("NAT-123", new RuntimeException("Connection timed out")))
                .isInstanceOf(KycServiceUnavailableException.class)
                .hasMessageContaining("KYC service is currently unavailable for nationalId: NAT-123");
    }

    @Test
    @DisplayName("Fallback method should rethrow KycServiceUnavailableException as-is")
    void fallbackShouldRethrowKycServiceUnavailableException() {
        StubKycAdapter adapter = new StubKycAdapter(false, 0.0);
        KycServiceUnavailableException original = new KycServiceUnavailableException("Original service error");

        assertThatThrownBy(() -> adapter.kycFallback("NAT-123", original))
                .isSameAs(original);
    }
}
