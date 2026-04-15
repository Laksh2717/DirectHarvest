package com.directharvest.backend.orders.response;

import com.directharvest.backend.common.enums.NegotiationEventType;
import com.directharvest.backend.common.enums.NegotiationStatus;
import com.directharvest.backend.common.enums.UserRole;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderNegotiationEventDetailsResponse(
        Long id,
        NegotiationEventType eventType,
        Long actorId,
        String actorName,
        String actorEmail,
        UserRole actorRole,
        BigDecimal offeredPrice,
        BigDecimal requestedQuantity,
        NegotiationStatus statusAfter,
        Instant createdAt
) {
}
