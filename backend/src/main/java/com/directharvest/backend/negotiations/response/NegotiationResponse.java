package com.directharvest.backend.negotiations.response;

import com.directharvest.backend.common.enums.NegotiationStatus;
import com.directharvest.backend.common.enums.ProposedBy;
import com.directharvest.backend.common.enums.UserRole;

import java.math.BigDecimal;
import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Negotiation details response.")
public record NegotiationResponse(
        @Schema(description = "Negotiation ID", example = "10")
        Long id,
        @Schema(description = "Listing ID", example = "101")
        Long listingId,
        @Schema(description = "Title of the listing", example = "Premium Wheat")
        String listingTitle,
        @Schema(description = "Buyer user ID", example = "5")
        Long buyerId,
        @Schema(description = "Buyer name", example = "Alice")
        String buyerName,
        @Schema(description = "Buyer email", example = "alice@example.com")
        String buyerEmail,
        @Schema(description = "Farmer user ID", example = "7")
        Long farmerId,
        @Schema(description = "Farmer name", example = "Bob")
        String farmerName,
        @Schema(description = "Farmer email", example = "bob@example.com")
        String farmerEmail,
        @Schema(description = "Offered price", example = "100.00")
        BigDecimal offeredPrice,
        @Schema(description = "Requested quantity", example = "5.0")
        BigDecimal requestedQuantity,
        @Schema(description = "Negotiation status")
        NegotiationStatus status,
        @Schema(description = "Who proposed the negotiation/counter", example = "BUYER")
        ProposedBy proposedBy,
        @Schema(description = "Reason for cancellation, if any", example = "Offered price too low")
        String cancellationReason,
        @Schema(description = "Role of user who cancelled, if any")
        UserRole cancelledBy,
        @Schema(description = "Negotiation expiry timestamp", example = "2024-04-20T12:00:00Z")
        java.time.Instant expiresAt,
        @Schema(description = "Negotiation creation timestamp", example = "2024-04-13T12:00:00Z")
        Instant createdAt,
        @Schema(description = "Negotiation last update timestamp", example = "2024-04-13T12:10:00Z")
        Instant updatedAt
) {
}

