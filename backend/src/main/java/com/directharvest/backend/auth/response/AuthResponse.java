package com.directharvest.backend.auth.response;

import com.directharvest.backend.common.enums.AuthProvider;
import com.directharvest.backend.common.enums.UserRole;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response containing user and token details.")
public record AuthResponse(
        @Schema(description = "User ID", example = "1")
        Long userId,
        @Schema(description = "User name", example = "Laksh Chovatiya")
        String name,
        @Schema(description = "User email", example = "laksh@example.com")
        String email,
        @Schema(description = "User role", example = "BUYER")
        UserRole role,
        @Schema(description = "Authentication provider", example = "GOOGLE")
        AuthProvider provider,
        @Schema(description = "Access token (JWT)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,
        @Schema(description = "Refresh token (JWT)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String refreshToken,
        @Schema(description = "Access token expiry timestamp", example = "2026-04-13T12:00:00Z")
        Instant accessTokenExpiresAt,
        @Schema(description = "Refresh token expiry timestamp", example = "2026-05-13T12:00:00Z")
        Instant refreshTokenExpiresAt
) {
}

