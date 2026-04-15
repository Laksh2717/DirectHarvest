package com.directharvest.backend.auth;

import com.directharvest.backend.auth.request.GoogleAuthRequest;
import com.directharvest.backend.auth.response.AuthResponse;
import com.directharvest.backend.auth.service.AuthService;
import com.directharvest.backend.auth.service.GoogleIdTokenVerifier;
import com.directharvest.backend.common.enums.AuthProvider;
import com.directharvest.backend.common.enums.UserRole;
import com.directharvest.backend.common.exception.BadRequestException;
import com.directharvest.backend.common.exception.ConflictException;
import com.directharvest.backend.security.jwt.JwtService;
import com.directharvest.backend.users.entity.User;
import com.directharvest.backend.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceGoogleLoginTest {

    private static final AtomicLong USER_SEQUENCE = new AtomicLong(100);

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private GoogleIdTokenVerifier googleIdTokenVerifier;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                authenticationManager,
                jwtService,
                googleIdTokenVerifier,
                86400000L,
                2592000000L
        );

    }

    @Test
    void googleLogin_firstLoginWithoutRole_throwsBadRequest() {
        when(googleIdTokenVerifier.verify("google-token"))
                .thenReturn(new GoogleIdTokenVerifier.GoogleUserInfo("google-sub-1", "google.user@test.com", "Google User"));
        when(userRepository.findByGoogleId("google-sub-1")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("google.user@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.googleLogin(new GoogleAuthRequest("google-token", null)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Role is required");
    }

    @Test
    void googleLogin_firstLoginCreatesGoogleUser_andIssuesTokens() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            if (user.getId() == null) {
                user.setId(USER_SEQUENCE.incrementAndGet());
            }
            return user;
        });
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");

        when(googleIdTokenVerifier.verify("google-token"))
                .thenReturn(new GoogleIdTokenVerifier.GoogleUserInfo("google-sub-2", "google.user@test.com", "Google User"));
        when(userRepository.findByGoogleId("google-sub-2")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("google.user@test.com")).thenReturn(Optional.empty());

        AuthResponse response = authService.googleLogin(new GoogleAuthRequest("google-token", UserRole.BUYER));

        assertThat(response.email()).isEqualTo("google.user@test.com");
        assertThat(response.role()).isEqualTo(UserRole.BUYER);
        assertThat(response.provider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.accessTokenExpiresAt()).isAfter(Instant.now().minusSeconds(1));
    }

    @Test
    void googleLogin_whenLocalUserWithSameEmailExists_throwsConflict() {
        when(googleIdTokenVerifier.verify("google-token"))
                .thenReturn(new GoogleIdTokenVerifier.GoogleUserInfo("google-sub-3", "google.user@test.com", "Google User"));
        when(userRepository.findByGoogleId("google-sub-3")).thenReturn(Optional.empty());

        User localUser = new User();
        localUser.setId(1L);
        localUser.setEmail("google.user@test.com");
        localUser.setProvider(AuthProvider.LOCAL);
        localUser.setRole(UserRole.BUYER);
        localUser.setPassword("encoded");
        localUser.setEnabled(true);

        when(userRepository.findByEmailIgnoreCase("google.user@test.com")).thenReturn(Optional.of(localUser));

        assertThatThrownBy(() -> authService.googleLogin(new GoogleAuthRequest("google-token", UserRole.BUYER)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("LOCAL provider");
    }
}

