package com.directharvest.backend.users.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record UserProfileResponse(
        String name,
        String email,
        String street,
        String city,
        String state,
        String pincode,
        @Schema(description = "Average farmer rating; null for BUYER users")
        BigDecimal averageRating,
        @Schema(description = "Farmer rating count; null for BUYER users")
        Integer ratingCount
) {
}
