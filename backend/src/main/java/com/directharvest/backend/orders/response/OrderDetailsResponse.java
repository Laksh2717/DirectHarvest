package com.directharvest.backend.orders.response;

import com.directharvest.backend.common.enums.CancellationBy;
import com.directharvest.backend.common.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detailed order response including listing, negotiation, and rating info.")
public record OrderDetailsResponse(
        @Schema(description = "Order ID", example = "1")
        Long id,
        @Schema(description = "Human-readable order ID for UI display", example = "ORD-12345")
        String displayOrderId,
        @Schema(description = "Listing ID", example = "101")
        Long listingId,
        @Schema(description = "Listing crop name", example = "Premium Wheat")
        String listingTitle,
        @Schema(description = "Listing description", example = "High quality wheat from Gujarat")
        String listingDescription,
        @Schema(description = "Listing street address", example = "Plot 17, Ring Road")
        String listingStreet,
        @Schema(description = "Listing city", example = "Surat")
        String listingCity,
        @Schema(description = "Listing state", example = "Gujarat")
        String listingState,
        @Schema(description = "Listing pincode", example = "395007")
        String listingPincode,
        @Schema(description = "Negotiation ID", example = "10")
        Long negotiationId,
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
        @Schema(description = "Farmer's average rating", example = "4.7")
        BigDecimal farmerAverageRating,
        @Schema(description = "Farmer's total rating count", example = "12")
        Integer farmerRatingCount,
        @Schema(description = "Agreed price for the order", example = "100.00")
        BigDecimal agreedPrice,
        @Schema(description = "Agreed quantity for the order", example = "5.0")
        BigDecimal agreedQuantity,
        @Schema(description = "Order status", example = "ACTIVE")
        OrderStatus status,
        @Schema(description = "Who cancelled the order, if any")
        CancellationBy cancelledBy,
        @Schema(description = "Reason for cancellation, if any", example = "Buyer requested cancellation due to pickup delay")
        String cancelledReason,
        @Schema(description = "Timestamp when order was cancelled", example = "2026-04-13T12:00:00Z")
        Instant cancelledAt,
        @Schema(description = "Timestamp when order was activated", example = "2026-04-13T13:00:00Z")
        Instant activatedAt,
        @Schema(description = "Timestamp when order was completed", example = "2026-04-14T10:00:00Z")
        Instant completedAt,
        @Schema(description = "Whether this order has been rated")
        Boolean rated,
        @Schema(description = "Rating score for the order if rated", example = "4")
        Integer ratingScore,
        @Schema(description = "Order creation timestamp", example = "2026-04-13T11:00:00Z")
        Instant createdAt,
        @Schema(description = "Order last update timestamp", example = "2026-04-13T12:30:00Z")
        Instant updatedAt,
        @Schema(description = "Negotiation details for the order")
        List<OrderNegotiationDetailsResponse> negotiations
) {
}
