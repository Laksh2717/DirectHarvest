package com.directharvest.backend.orders.response;

import com.directharvest.backend.common.enums.CancellationBy;
import com.directharvest.backend.common.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        Long id,
        @Schema(description = "Human-readable order ID for UI display", example = "ORD-12345")
        String displayOrderId,
        Long listingId,
        @Schema(description = "Listing crop name")
        String listingTitle,
        @Schema(description = "Linked negotiation ID reference only; negotiation payload is not included")
        Long negotiationId,
        Long buyerId,
        String buyerName,
        Long farmerId,
        String farmerName,
        BigDecimal agreedPrice,
        BigDecimal agreedQuantity,
        OrderStatus status,
        CancellationBy cancelledBy,
        String cancelledReason,
        Instant cancelledAt,
        Instant activatedAt,
        Instant completedAt,
        @Schema(description = "Whether this order has been rated")
        Boolean rated,
        @Schema(description = "Rating score for the order if rated", example = "4")
        Integer ratingScore,
        Instant createdAt,
        Instant updatedAt
) {
}

