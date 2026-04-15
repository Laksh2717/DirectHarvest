package com.directharvest.backend.auth.controller;

import com.directharvest.backend.auth.request.LoginRequest;
import com.directharvest.backend.auth.request.GoogleAuthRequest;
import com.directharvest.backend.auth.request.LogoutRequest;
import com.directharvest.backend.auth.request.RefreshTokenRequest;
import com.directharvest.backend.auth.request.RegisterRequest;
import com.directharvest.backend.auth.response.AuthResponse;
import com.directharvest.backend.auth.response.MessageResponse;
import com.directharvest.backend.auth.service.AuthService;
import com.directharvest.backend.auth.service.AuthCookieService;
import com.directharvest.backend.common.exception.UnauthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Authentication endpoints. Tokens are set as HttpOnly cookies.")
@SecurityRequirements
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService authCookieService;

    public AuthController(AuthService authService, AuthCookieService authCookieService) {
        this.authService = authService;
        this.authCookieService = authCookieService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Creates a new user and sets access_token + refresh_token cookies.")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return applyAuthCookies(ResponseEntity.status(HttpStatus.CREATED), authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates user and sets access_token + refresh_token cookies.")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return applyAuthCookies(ResponseEntity.ok(), authService.login(request));
    }

    @PostMapping("/google")
    @Operation(summary = "Google login", description = "Authenticates with Google ID token and sets access_token + refresh_token cookies.")
    public ResponseEntity<AuthResponse> google(@Valid @RequestBody GoogleAuthRequest request) {
        return applyAuthCookies(ResponseEntity.ok(), authService.googleLogin(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh tokens", description = "Reads refresh token from HttpOnly cookie first (request body token fallback), rotates tokens, and sets fresh cookies.")
    public ResponseEntity<AuthResponse> refresh(
            @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletRequest httpRequest
    ) {
        String refreshToken = resolveRefreshToken(request, httpRequest);
        return applyAuthCookies(ResponseEntity.ok(), authService.refreshToken(refreshToken));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Reads refresh token from HttpOnly cookie first (request body token fallback), invalidates it, and clears auth cookies.")
    public ResponseEntity<MessageResponse> logout(
            @RequestBody(required = false) LogoutRequest request,
            HttpServletRequest httpRequest
    ) {
        String refreshToken = resolveRefreshToken(request, httpRequest);
        MessageResponse response = authService.logout(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authCookieService.clearAccessTokenCookie().toString())
                .header(HttpHeaders.SET_COOKIE, authCookieService.clearRefreshTokenCookie().toString())
                .body(response);
    }

    private ResponseEntity<AuthResponse> applyAuthCookies(ResponseEntity.BodyBuilder builder, AuthResponse response) {
        return builder
                .header(HttpHeaders.SET_COOKIE, authCookieService.accessTokenCookie(response.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, authCookieService.refreshTokenCookie(response.refreshToken()).toString())
                .body(response);
    }

    private String resolveRefreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        String cookieToken = authCookieService.resolveRefreshToken(httpRequest);
        if (cookieToken != null) {
            return cookieToken;
        }
        if (request != null && request.refreshToken() != null && !request.refreshToken().isBlank()) {
            return request.refreshToken().trim();
        }
        throw new UnauthorizedException("Refresh token is required");
    }

    private String resolveRefreshToken(LogoutRequest request, HttpServletRequest httpRequest) {
        String cookieToken = authCookieService.resolveRefreshToken(httpRequest);
        if (cookieToken != null) {
            return cookieToken;
        }
        if (request != null && request.refreshToken() != null && !request.refreshToken().isBlank()) {
            return request.refreshToken().trim();
        }
        throw new UnauthorizedException("Refresh token is required");
    }
}
