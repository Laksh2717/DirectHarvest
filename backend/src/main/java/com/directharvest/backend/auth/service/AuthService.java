package com.directharvest.backend.auth.service;

import com.directharvest.backend.auth.request.LoginRequest;
import com.directharvest.backend.auth.request.GoogleAuthRequest;
import com.directharvest.backend.auth.request.RegisterRequest;
import com.directharvest.backend.auth.response.AuthResponse;
import com.directharvest.backend.auth.response.MessageResponse;
import com.directharvest.backend.common.enums.AuthProvider;
import com.directharvest.backend.common.enums.UserRole;
import com.directharvest.backend.common.exception.BadRequestException;
import com.directharvest.backend.common.exception.ConflictException;
import com.directharvest.backend.common.exception.ResourceNotFoundException;
import com.directharvest.backend.common.exception.UnauthorizedException;
import com.directharvest.backend.security.jwt.JwtService;
import com.directharvest.backend.security.service.CustomUserDetails;
import com.directharvest.backend.users.entity.User;
import com.directharvest.backend.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            GoogleIdTokenVerifier googleIdTokenVerifier,
            @Value("${security.jwt.access-token-expiration-ms:86400000}") long accessTokenExpirationMs,
            @Value("${security.jwt.refresh-token-expiration-ms:2592000000}") long refreshTokenExpirationMs
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.googleIdTokenVerifier = googleIdTokenVerifier;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email is already registered");
        }

        if (request.role() == UserRole.ADMIN) {
            throw new BadRequestException("ADMIN registration is not allowed from this endpoint");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setStreet(request.street().trim());
        user.setCity(request.city().trim());
        user.setState(request.state().trim());
        user.setPincode(request.pincode().trim());
        user.setRole(request.role());
        user.setProvider(AuthProvider.LOCAL);
        user.setEnabled(true);

        User saved = userRepository.save(user);
        return issueTokensAndPersist(saved);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        String requestedRole = request.role();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password())
            );
        } catch (AuthenticationException ex) {
            throw new UnauthorizedException("Invalid email or password");
        }

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new BadRequestException("Use OAuth login for this account");
        }

        // Check if the user's role matches the requested role
        if (requestedRole == null || !user.getRole().name().equalsIgnoreCase(requestedRole)) {
            throw new UnauthorizedException("Invalid credentials for selected role");
        }

        return issueTokensAndPersist(user);
    }

    @Transactional
    public AuthResponse googleLogin(GoogleAuthRequest request) {
        GoogleIdTokenVerifier.GoogleUserInfo googleUser = googleIdTokenVerifier.verify(request.idToken().trim());
        String email = normalizeEmail(googleUser.email());

        User user = userRepository.findByGoogleId(googleUser.subject())
                .orElseGet(() -> resolveUserByEmailOrCreate(email, googleUser, request));

        return issueTokensAndPersist(user);
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        refreshToken = refreshToken.trim();
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String email = normalizeEmail(jwtService.extractUsername(refreshToken));
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        validateStoredRefreshToken(user, refreshToken);
        return issueTokensAndPersist(user);
    }

    @Transactional
    public MessageResponse logout(String refreshToken) {
        refreshToken = refreshToken.trim();
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String email = normalizeEmail(jwtService.extractUsername(refreshToken));
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        validateStoredRefreshToken(user, refreshToken);
        user.setRefreshToken(null);
        user.setRefreshTokenExpiry(null);
        userRepository.save(user);

        return new MessageResponse("Logged out successfully");
    }

    private AuthResponse issueTokensAndPersist(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                user.isEnabled()
        );

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        Instant now = Instant.now();
        Instant accessExpiresAt = now.plusMillis(accessTokenExpirationMs);
        Instant refreshExpiresAt = now.plusMillis(refreshTokenExpirationMs);

        user.setRefreshToken(hashToken(refreshToken));
        user.setRefreshTokenExpiry(refreshExpiresAt);
        userRepository.save(user);

        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getProvider(),
                accessToken,
                refreshToken,
                accessExpiresAt,
                refreshExpiresAt
        );
    }

    private User resolveUserByEmailOrCreate(
            String email,
            GoogleIdTokenVerifier.GoogleUserInfo googleUser,
            GoogleAuthRequest request
    ) {
        User existingByEmail = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (existingByEmail != null) {
            if (existingByEmail.getProvider() != AuthProvider.GOOGLE) {
                throw new ConflictException("Email already registered with LOCAL provider");
            }
            if (existingByEmail.getGoogleId() == null || existingByEmail.getGoogleId().isBlank()) {
                existingByEmail.setGoogleId(googleUser.subject());
                return userRepository.save(existingByEmail);
            }
            if (!existingByEmail.getGoogleId().equals(googleUser.subject())) {
                throw new UnauthorizedException("Google account mismatch");
            }
            return existingByEmail;
        }

        if (request.role() == null) {
            throw new BadRequestException("Role is required for first Google login");
        }
        if (request.role() == UserRole.ADMIN) {
            throw new BadRequestException("ADMIN registration is not allowed from this endpoint");
        }

        User user = new User();
        user.setName(resolveName(googleUser));
        user.setEmail(email);
        user.setProvider(AuthProvider.GOOGLE);
        user.setGoogleId(googleUser.subject());
        user.setRole(request.role());
        user.setEnabled(true);

        return userRepository.save(user);
    }

    private String resolveName(GoogleIdTokenVerifier.GoogleUserInfo googleUser) {
        String name = googleUser.name();
        if (name != null && !name.isBlank()) {
            return name.trim();
        }
        String email = normalizeEmail(googleUser.email());
        int atIndex = email.indexOf('@');
        String fallback = atIndex > 0 ? email.substring(0, atIndex) : email;
        return fallback.length() > 100 ? fallback.substring(0, 100) : fallback;
    }

    private void validateStoredRefreshToken(User user, String rawRefreshToken) {
        if (user.getRefreshToken() == null || user.getRefreshTokenExpiry() == null) {
            throw new UnauthorizedException("Refresh token not found");
        }

        if (!user.getRefreshToken().equals(hashToken(rawRefreshToken))) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        if (Instant.now().isAfter(user.getRefreshTokenExpiry())) {
            throw new UnauthorizedException("Refresh token expired");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email is required");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new UnauthorizedException("Unable to process token");
        }
    }
}
