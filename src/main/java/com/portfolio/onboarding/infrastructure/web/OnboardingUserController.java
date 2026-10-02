package com.portfolio.onboarding.infrastructure.web;

import com.portfolio.onboarding.application.usecase.RegisterUserCommand;
import com.portfolio.onboarding.application.usecase.RegisterUserResult;
import com.portfolio.onboarding.application.usecase.UserRegistrationUseCase;
import com.portfolio.onboarding.infrastructure.web.dto.ErrorResponse;
import com.portfolio.onboarding.infrastructure.web.dto.OnboardingUserResponse;
import com.portfolio.onboarding.infrastructure.web.dto.RegisterUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Onboarding", description = "Endpoints for registering and onboarding new users")
@RestController
@RequestMapping("/api/v1/onboarding/users")
@RequiredArgsConstructor
public class OnboardingUserController {

    private final UserRegistrationUseCase userRegistrationUseCase;

    @Operation(
            summary = "Register a new user",
            description = "Creates a new onboarding record for the provided user. " +
                          "The user's KYC status is initialised asynchronously after registration."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = OnboardingUserResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload — missing or malformed fields " +
                                  "(e.g. blank email, invalid email format, missing national ID)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict — a user with the same e-mail or national ID already exists, " +
                                  "or an optimistic-locking collision was detected",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<OnboardingUserResponse> registerUser(@Valid @RequestBody RegisterUserRequest request) {
        RegisterUserCommand command = new RegisterUserCommand(request.email(), request.nationalId());
        RegisterUserResult result = userRegistrationUseCase.register(command);
        OnboardingUserResponse response = OnboardingUserResponse.fromResult(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
