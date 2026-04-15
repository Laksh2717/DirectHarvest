package com.directharvest.backend.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload for refresh token request.")
public record RefreshTokenRequest(
        @Schema(description = "Refresh token to rotate", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String refreshToken
) {
}

