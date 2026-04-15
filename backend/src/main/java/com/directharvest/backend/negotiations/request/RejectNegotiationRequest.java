package com.directharvest.backend.negotiations.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Optional payload for rejecting a negotiation. The rejection reason is optional.")
public record RejectNegotiationRequest(
        @Schema(description = "Optional reason for rejecting the negotiation", example = "Offered price is too low for this quantity")
        @Size(max = 500, message = "Cancellation reason must be at most 500 characters")
        String cancellationReason
) {
}
