package com.directharvest.backend.negotiations.response;

import com.directharvest.backend.common.enums.NegotiationEventType;
import com.directharvest.backend.common.enums.NegotiationStatus;
import com.directharvest.backend.common.enums.UserRole;

import java.math.BigDecimal;
import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Represents a single event in the negotiation history.")
public record NegotiationEventResponse(
        @Schema(description = "Event ID", example = "1")
        Long id,
        @Schema(description = "Negotiation ID this event belongs to", example = "10")
        Long negotiationId,
        @Schema(description = "Type of event (CREATED, COUNTERED, ACCEPTED, REJECTED, EXPIRED)")
        NegotiationEventType eventType,
        @Schema(description = "User ID of the actor who performed the event", example = "5")
        Long actorId,
        @Schema(description = "Name of the actor", example = "John Doe")
        String actorName,
        @Schema(description = "Role of the actor (BUYER/FARMER)")
        UserRole actorRole,
        @Schema(description = "Offered price at this event", example = "110.00")
        BigDecimal offeredPrice,
        @Schema(description = "Requested quantity at this event", example = "8.0")
        BigDecimal requestedQuantity,
        @Schema(description = "Negotiation status after this event")
        NegotiationStatus statusAfter,
        @Schema(description = "Timestamp when the event was created", example = "2024-04-13T12:34:56Z")
        Instant createdAt
) {
}

