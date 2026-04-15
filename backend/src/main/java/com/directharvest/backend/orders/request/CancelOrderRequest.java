package com.directharvest.backend.orders.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Optional payload for cancelling an order. The cancellation reason is optional.")
public record CancelOrderRequest(
        @Schema(description = "Optional cancellation reason", example = "Buyer requested cancellation due to pickup delay")
        @Size(max = 500, message = "Cancellation reason must be at most 500 characters")
        String cancellationReason
) {
}

