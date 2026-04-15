package com.directharvest.backend.orders.response;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderNegotiationDetailsResponse(
        BigDecimal price,
        BigDecimal qty,
        Instant createdAt
) {
}
