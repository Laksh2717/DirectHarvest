package com.directharvest.backend.auth.request;

import com.directharvest.backend.common.enums.UserRole;
import jakarta.validation.constraints.NotBlank;

public record GoogleAuthRequest(
        @NotBlank(message = "Google id token is required")
        String idToken,
        UserRole role
) {
}

