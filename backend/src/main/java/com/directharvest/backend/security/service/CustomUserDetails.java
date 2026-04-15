package com.directharvest.backend.security.service;

import com.directharvest.backend.common.enums.UserRole;
import com.directharvest.backend.users.service.SecurityUserProjection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class CustomUserDetails implements UserDetails {
    private final Long id;
    private final String email;
    private final String password;
    private final UserRole role;
    private final boolean enabled;

    public CustomUserDetails(Long id, String email, String password, UserRole role, boolean enabled) {
        this.id = id;
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.password = password;
        this.role = Objects.requireNonNull(role, "role must not be null");
        this.enabled = enabled;
    }

    public static CustomUserDetails fromProjection(SecurityUserProjection projection) {
        return new CustomUserDetails(
                projection.id(),
                projection.email(),
                projection.password(),
                projection.role(),
                projection.enabled()
        );
    }

    public Long getId() {
        return id;
    }

    public UserRole getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}

