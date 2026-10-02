package com.portfolio.onboarding.application.port;

/**
 * Output port for KYC (Know Your Customer) national ID validation.
 * <p>
 * Implementations live in {@code infrastructure/kyc/} and are responsible
 * for calling the real (or simulated) external KYC service.
 *
 * @throws com.portfolio.onboarding.domain.exception.KycServiceUnavailableException
 *         if the external KYC service is down or fails
 */
public interface KycValidationPort {

    /**
     * Validates the given national ID against the external KYC provider.
     *
     * @param nationalId the national ID of the user being registered
     */
    void validate(String nationalId);
}
