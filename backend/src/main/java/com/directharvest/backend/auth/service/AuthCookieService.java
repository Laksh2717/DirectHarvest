package com.directharvest.backend.auth.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthCookieService {

    public static final String ACCESS_TOKEN_COOKIE = "accessToken";
    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    private final Duration accessTokenMaxAge;
    private final Duration refreshTokenMaxAge;
    private final boolean secure;

    public AuthCookieService(
            @Value("${security.jwt.access-token-expiration-ms:86400000}") long accessTokenExpirationMs,
            @Value("${security.jwt.refresh-token-expiration-ms:2592000000}") long refreshTokenExpirationMs,
            @Value("${security.cookies.secure:false}") boolean secure
    ) {
        this.accessTokenMaxAge = Duration.ofMillis(accessTokenExpirationMs);
        this.refreshTokenMaxAge = Duration.ofMillis(refreshTokenExpirationMs);
        this.secure = secure;
    }

    public ResponseCookie accessTokenCookie(String token) {
        return baseCookie(ACCESS_TOKEN_COOKIE, token, accessTokenMaxAge).build();
    }

    public ResponseCookie refreshTokenCookie(String token) {
        return baseCookie(REFRESH_TOKEN_COOKIE, token, refreshTokenMaxAge).build();
    }

    public ResponseCookie clearAccessTokenCookie() {
        return baseCookie(ACCESS_TOKEN_COOKIE, "", Duration.ZERO).build();
    }

    public ResponseCookie clearRefreshTokenCookie() {
        return baseCookie(REFRESH_TOKEN_COOKIE, "", Duration.ZERO).build();
    }

    public String resolveAccessToken(HttpServletRequest request) {
        return resolveCookie(request, ACCESS_TOKEN_COOKIE);
    }

    public String resolveRefreshToken(HttpServletRequest request) {
        return resolveCookie(request, REFRESH_TOKEN_COOKIE);
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .sameSite("Lax")
                .maxAge(maxAge);
    }

    private String resolveCookie(HttpServletRequest request, String cookieName) {
        if (request == null || request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }
        return null;
    }
}

