package com.directharvest.backend.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import com.directharvest.backend.common.enums.UserRole;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload for user login request.")
public record LoginRequest(
        @Schema(description = "User email address", example = "laksh@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Email is invalid")
        String email,

        @Schema(description = "User password", example = "MyPassword123")
        @NotBlank(message = "Password is required")
        String password,

        @Schema(description = "User role (BUYER or FARMER)", example = "BUYER")
        @NotBlank(message = "Role is required")
        String role
) {
}

