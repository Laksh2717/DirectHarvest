package com.directharvest.backend.users.service;

import com.directharvest.backend.common.enums.UserRole;

public record SecurityUserProjection(
        Long id,
        String email,
        String password,
        UserRole role,
        boolean enabled
) {
}

