package com.portfolio.onboarding.infrastructure.web;

import com.portfolio.onboarding.application.usecase.RegisterUserCommand;
import com.portfolio.onboarding.application.usecase.RegisterUserResult;
import com.portfolio.onboarding.application.usecase.UserRegistrationUseCase;
import com.portfolio.onboarding.domain.exception.UserAlreadyExistsException;
import com.portfolio.onboarding.domain.exception.DomainValidationException;
import com.portfolio.onboarding.domain.model.OnboardingStatus;
import com.portfolio.onboarding.domain.model.OnboardingUser;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = OnboardingUserController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.security.oauth2.server.resource.autoconfigure.web.OAuth2ResourceServerWebSecurityAutoConfiguration.class,
                org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration.class
        }
)
class OnboardingUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRegistrationUseCase userRegistrationUseCase;

    @MockitoBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMappingContext;

    @Test
    @DisplayName("POST /api/v1/onboarding/users - Should return 201 Created on valid request")
    void shouldRegisterUserAndReturn201Created() throws Exception {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        RegisterUserResult result = new RegisterUserResult(
                id,
                "john.doe@example.com",
                "ID-987654",
                OnboardingStatus.PENDING_VALIDATION,
                now
        );

        when(userRegistrationUseCase.register(any(RegisterUserCommand.class))).thenReturn(result);

        String jsonPayload = """
                {
                    "email": "john.doe@example.com",
                    "nationalId": "ID-987654"
                }
                """;

        mockMvc.perform(post("/api/v1/onboarding/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.nationalId").value("ID-987654"))
                .andExpect(jsonPath("$.status").value("PENDING_VALIDATION"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/v1/onboarding/users - Should return 201 Created with MANUAL_REVIEW status when KYC fallback triggers")
    void shouldRegisterUserAndReturn201WithManualReview() throws Exception {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        RegisterUserResult result = new RegisterUserResult(
                id,
                "jane.doe@example.com",
                "ID-FAIL-999",
                OnboardingStatus.MANUAL_REVIEW,
                now
        );

        when(userRegistrationUseCase.register(any(RegisterUserCommand.class))).thenReturn(result);

        String jsonPayload = """
                {
                    "email": "jane.doe@example.com",
                    "nationalId": "ID-FAIL-999"
                }
                """;

        mockMvc.perform(post("/api/v1/onboarding/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.email").value("jane.doe@example.com"))
                .andExpect(jsonPath("$.nationalId").value("ID-FAIL-999"))
                .andExpect(jsonPath("$.status").value("MANUAL_REVIEW"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }


    @Test
    @DisplayName("POST /api/v1/onboarding/users - Should return 409 Conflict when user already exists")
    void shouldReturn409ConflictWhenUserAlreadyExists() throws Exception {
        when(userRegistrationUseCase.register(any(RegisterUserCommand.class)))
                .thenThrow(new UserAlreadyExistsException("Email already exists: existing@example.com"));

        String jsonPayload = """
                {
                    "email": "existing@example.com",
                    "nationalId": "ID-111111"
                }
                """;

        mockMvc.perform(post("/api/v1/onboarding/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Email already exists: existing@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/onboarding/users - Should return 400 Bad Request when validation fails")
    void shouldReturn400BadRequestWhenValidationFails() throws Exception {
        String invalidJsonPayload = """
                {
                    "email": "not-an-email",
                    "nationalId": ""
                }
                """;

        mockMvc.perform(post("/api/v1/onboarding/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors.email").value("Email format is invalid"))
                .andExpect(jsonPath("$.validationErrors.nationalId").value("National ID is required"));
    }

    @Test
    @DisplayName("POST /api/v1/onboarding/users - Should return 409 Conflict when optimistic locking failure occurs")
    void shouldReturn409ConflictWhenOptimisticLockingFailureOccurs() throws Exception {
        when(userRegistrationUseCase.register(any(RegisterUserCommand.class)))
                .thenThrow(new org.springframework.orm.ObjectOptimisticLockingFailureException(OnboardingUser.class, "stale-version"));

        String jsonPayload = """
                {
                    "email": "concurrent@example.com",
                    "nationalId": "ID-CONCURRENT"
                }
                """;

        mockMvc.perform(post("/api/v1/onboarding/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("Concurrent modification detected")));
    }

    @Test
    @DisplayName("POST /api/v1/onboarding/users - Should return 422 Unprocessable Entity when domain validation fails")
    void shouldReturn422UnprocessableEntityWhenDomainValidationFails() throws Exception {
        when(userRegistrationUseCase.register(any(RegisterUserCommand.class)))
                .thenThrow(new DomainValidationException("Domain validation failed for user"));

        String jsonPayload = """
                {
                    "email": "invalid.rule@example.com",
                    "nationalId": "ID-FAIL-RULE"
                }
                """;

        mockMvc.perform(post("/api/v1/onboarding/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"))
                .andExpect(jsonPath("$.message").value("Domain validation failed for user"));
    }
}
