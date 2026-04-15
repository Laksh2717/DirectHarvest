package com.directharvest.backend.users.service;

import com.directharvest.backend.common.enums.UserRole;
import com.directharvest.backend.common.enums.NegotiationStatus;
import com.directharvest.backend.common.exception.BadRequestException;
import com.directharvest.backend.common.exception.ConflictException;
import com.directharvest.backend.common.exception.UnauthorizedException;
import com.directharvest.backend.negotiations.repository.NegotiationRepository;
import com.directharvest.backend.users.request.UpdateUserProfileRequest;
import com.directharvest.backend.users.entity.User;
import com.directharvest.backend.users.repository.UserRepository;
import com.directharvest.backend.users.response.UserAddressResponse;
import com.directharvest.backend.users.response.UserProfileResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Locale;

@Service
public class UserProfileService {

    private static final EnumSet<NegotiationStatus> ACTIVE_NEGOTIATION_STATUSES =
            EnumSet.of(NegotiationStatus.PENDING_BUYER, NegotiationStatus.PENDING_FARMER);

    private final UserRepository userRepository;
    private final NegotiationRepository negotiationRepository;

    public UserProfileService(UserRepository userRepository, NegotiationRepository negotiationRepository) {
        this.userRepository = userRepository;
        this.negotiationRepository = negotiationRepository;
    }

    @Transactional(readOnly = true)
    public UserAddressResponse getMyAddress() {
        User currentUser = getCurrentUser();
        return new UserAddressResponse(
                currentUser.getStreet(),
                currentUser.getCity(),
                currentUser.getState(),
                currentUser.getPincode()
        );
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile() {
        return toProfileResponse(getCurrentUser());
    }

    @Transactional
    public UserProfileResponse updateMyProfile(UpdateUserProfileRequest request) {
        User currentUser = getCurrentUser();
        ensureNoActiveNegotiations(currentUser.getId());

        String normalizedEmail = normalizeEmail(request.email());
        userRepository.findByEmailIgnoreCase(normalizedEmail)
                .filter(existing -> !existing.getId().equals(currentUser.getId()))
                .ifPresent(existing -> {
                    throw new ConflictException("Email is already registered");
                });

        currentUser.setName(request.name().trim());
        currentUser.setEmail(normalizedEmail);
        currentUser.setStreet(request.street().trim());
        currentUser.setCity(request.city().trim());
        currentUser.setState(request.state().trim());
        currentUser.setPincode(request.pincode().trim());

        return toProfileResponse(userRepository.save(currentUser));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Unauthorized");
        }

        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException("User not found for token"));
    }

    private UserProfileResponse toProfileResponse(User user) {
        boolean isFarmer = user.getRole() == UserRole.FARMER;
        return new UserProfileResponse(
                user.getName(),
                user.getEmail(),
                user.getStreet(),
                user.getCity(),
                user.getState(),
                user.getPincode(),
                isFarmer ? user.getAverageRating() : null,
                isFarmer ? user.getRatingCount() : null
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void ensureNoActiveNegotiations(Long userId) {
        boolean hasActiveNegotiations = negotiationRepository.existsActiveByParticipantId(userId, ACTIVE_NEGOTIATION_STATUSES);
        if (hasActiveNegotiations) {
            throw new BadRequestException("Cannot update profile while active negotiations exist");
        }
    }
}
