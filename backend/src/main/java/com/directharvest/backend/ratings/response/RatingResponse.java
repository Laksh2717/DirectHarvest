package com.directharvest.backend.ratings.response;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Rating details response.")
public record RatingResponse(
        @Schema(description = "Rating ID", example = "1")
        Long id,
        @Schema(description = "Order ID this rating is for", example = "10")
        Long orderId,
        @Schema(description = "Listing ID this rating is for", example = "101")
        Long listingId,
        @Schema(description = "User ID of the rater", example = "5")
        Long raterId,
        @Schema(description = "Name of the rater", example = "Alice")
        String raterName,
        @Schema(description = "Farmer user ID being rated", example = "7")
        Long farmerId,
        @Schema(description = "Farmer name", example = "Bob")
        String farmerName,
        @Schema(description = "Rating score (1-5)", example = "5")
        Integer score,
        @Schema(description = "Timestamp when the rating was created", example = "2026-04-13T11:00:00Z")
        Instant createdAt,
        @Schema(description = "Timestamp when the rating was last updated", example = "2026-04-13T12:30:00Z")
        Instant updatedAt
) {
}

