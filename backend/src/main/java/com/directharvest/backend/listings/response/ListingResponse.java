package com.directharvest.backend.listings.response;

import com.directharvest.backend.common.enums.ListingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Listing details response.")
public record ListingResponse(
        @Schema(description = "Listing ID", example = "101")
        Long id,
        @Schema(description = "Farmer user ID", example = "7")
        Long farmerId,
        @Schema(description = "Farmer name", example = "Bob")
        String farmerName,
        @Schema(description = "Farmer email", example = "bob@example.com")
        String farmerEmail,
        @Schema(description = "Farmer's average rating", example = "4.7")
        BigDecimal farmerRating,
        @Schema(description = "Farmer's total rating count", example = "12")
        Integer farmerRatingCount,
        @Schema(description = "Crop name", example = "Wheat")
        String cropName,
        @Schema(description = "Available quantity", example = "120.50")
        BigDecimal quantity,
        @Schema(description = "Initial quantity listed", example = "150.00")
        BigDecimal initialQuantity,
        @Schema(description = "Price per kg", example = "27.75")
        BigDecimal pricePerKg,
        @Schema(description = "Listing description", example = "Fresh harvest, moisture controlled")
        String description,
        @Schema(description = "Street address", example = "Plot 17, Ring Road")
        String street,
        @Schema(description = "City", example = "Ahmedabad")
        String city,
        @Schema(description = "State", example = "Gujarat")
        String state,
        @Schema(description = "Pincode", example = "380001")
        String pincode,
        @Schema(description = "Listing status", example = "ACTIVE")
        ListingStatus status,
        @Schema(description = "Listing images")
        List<ListingImageResponse> images,
        @Schema(description = "Listing creation timestamp", example = "2026-04-13T11:00:00Z")
        Instant createdAt,
        @Schema(description = "Listing last update timestamp", example = "2026-04-13T12:30:00Z")
        Instant updatedAt
) {
}

