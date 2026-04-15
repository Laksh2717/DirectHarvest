package com.directharvest.backend.users.service;

import java.util.Optional;

public interface UserSecurityLookupService {
    Optional<SecurityUserProjection> findByEmail(String email);
}

