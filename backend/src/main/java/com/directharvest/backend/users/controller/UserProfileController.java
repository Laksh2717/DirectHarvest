package com.directharvest.backend.users.controller;

import com.directharvest.backend.users.request.UpdateUserProfileRequest;
import com.directharvest.backend.users.response.UserAddressResponse;
import com.directharvest.backend.users.response.UserProfileResponse;
import com.directharvest.backend.users.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "User profile endpoints")
@SecurityRequirement(name = "cookieAuth")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get my profile", description = "Returns authenticated user profile details with rating fields for FARMER users.")
    public ResponseEntity<UserProfileResponse> getMyProfile() {
        return ResponseEntity.ok(userProfileService.getMyProfile());
    }

    @GetMapping("/me/address")
    @Operation(summary = "Get my address", description = "Returns address of authenticated user in street, city, state, pincode format.")
    public ResponseEntity<UserAddressResponse> getMyAddress() {
        return ResponseEntity.ok(userProfileService.getMyAddress());
    }

    @PatchMapping("/me")
    @Operation(summary = "Update my profile", description = "Updates name, email, and address fields for authenticated FARMER/BUYER user.")
    public ResponseEntity<UserProfileResponse> updateMyProfile(@Valid @RequestBody UpdateUserProfileRequest request) {
        return ResponseEntity.ok(userProfileService.updateMyProfile(request));
    }
}
