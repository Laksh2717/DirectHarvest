package com.directharvest.backend.security.service;

import com.directharvest.backend.users.service.SecurityUserProjection;
import com.directharvest.backend.users.service.UserSecurityLookupService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final ObjectProvider<UserSecurityLookupService> userSecurityLookupServiceProvider;

    public CustomUserDetailsService(ObjectProvider<UserSecurityLookupService> userSecurityLookupServiceProvider) {
        this.userSecurityLookupServiceProvider = userSecurityLookupServiceProvider;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserSecurityLookupService lookupService = userSecurityLookupServiceProvider.getIfAvailable();
        if (lookupService == null) {
            throw new UsernameNotFoundException("User lookup service is not available yet");
        }

        String normalizedEmail = username == null ? null : username.trim().toLowerCase(Locale.ROOT);
        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            throw new UsernameNotFoundException("Email must not be blank");
        }

        SecurityUserProjection projection = lookupService.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + normalizedEmail));
        return CustomUserDetails.fromProjection(projection);
    }
}

