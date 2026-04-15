package com.directharvest.backend.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload for user logout request.")
public record LogoutRequest(
        @Schema(description = "Refresh token to be invalidated", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String refreshToken
) {
}

