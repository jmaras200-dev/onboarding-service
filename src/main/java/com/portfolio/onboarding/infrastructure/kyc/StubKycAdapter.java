package com.portfolio.onboarding.infrastructure.kyc;

import com.portfolio.onboarding.application.port.KycValidationPort;
import com.portfolio.onboarding.domain.exception.KycServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Stub implementation of {@link KycValidationPort} used in non-production profiles
 * while a real KYC integration is not yet available.
 *
 * <h3>Activation</h3>
 * This bean is only registered when the property
 * {@code kyc.stub.enabled=true} is set (e.g. in {@code application-local.yml}
 * or {@code application-test.yml}).  In production the property must be absent
 * or {@code false}, and a real implementation of {@link KycValidationPort} must
 * be provided instead.
 *
 * <h3>Simulation behaviour</h3>
 * <ul>
 *   <li>Deterministic failure: any {@code nationalId} containing {@code "FAIL"}
 *       (case-insensitive) throws {@link KycServiceUnavailableException}.</li>
 *   <li>Random failure: controlled by {@code kyc.stub.failure-rate} (default 0.0).
 *       Set to a non-zero value only in explicit chaos/load-test environments.</li>
 *   <li>Simulated latency: controlled by {@code kyc.stub.simulate-latency} (default false).</li>
 * </ul>
 *
 * <p>TODO: Provide a {@code @Profile("production")} implementation backed by the
 *          real external KYC provider before going live.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "kyc.stub.enabled", havingValue = "true")
public class StubKycAdapter implements KycValidationPort {

    public static final String CIRCUIT_BREAKER_INSTANCE = "kycService";

    private final boolean simulateLatencyEnabled;
    private final double failureRate;

    public StubKycAdapter() {
        this(false, 0.0);
    }

    public StubKycAdapter(boolean simulateLatencyEnabled, double failureRate) {
        this.simulateLatencyEnabled = simulateLatencyEnabled;
        this.failureRate = failureRate;
    }

    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_INSTANCE, fallbackMethod = "kycFallback")
    public void validate(String nationalId) {
        executeValidation(nationalId);
    }

    private void executeValidation(String nationalId) {
        log.info("Starting stub KYC validation for nationalId: {}", nationalId);

        if (simulateLatencyEnabled) {
            simulateLatency();
        }

        simulateFailure(nationalId);

        log.info("Stub KYC validation succeeded for nationalId: {}", nationalId);
    }

    private void simulateLatency() {
        try {
            long delayMs = ThreadLocalRandom.current().nextLong(50, 150);
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new KycServiceUnavailableException("KYC validation interrupted during latency simulation", e);
        }
    }

    private void simulateFailure(String nationalId) {
        if (nationalId != null && nationalId.toUpperCase().contains("FAIL")) {
            throw new KycServiceUnavailableException("Deterministic simulated KYC service failure for: " + nationalId);
        }

        if (failureRate > 0 && ThreadLocalRandom.current().nextDouble() < failureRate) {
            throw new KycServiceUnavailableException("Simulated random KYC service failure for: " + nationalId);
        }
    }

    public void kycFallback(String nationalId, Throwable throwable) {
        log.warn("Circuit breaker fallback triggered for nationalId [{}]. Reason: {}",
                nationalId, throwable.getMessage());
        if (throwable instanceof KycServiceUnavailableException kycException) {
            throw kycException;
        }
        throw new KycServiceUnavailableException(
                "KYC service is currently unavailable for nationalId: " + nationalId, throwable);
    }
}
