package com.directharvest.backend.negotiations.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload for creating a new negotiation.")
public record CreateNegotiationRequest(
        @Schema(description = "ID of the listing to negotiate on", example = "101")
        @NotNull(message = "Listing id is required")
        Long listingId,

        @Schema(description = "Offered price for the negotiation", example = "100.00")
        @NotNull(message = "Offered price is required")
        @DecimalMin(value = "0.01", message = "Offered price must be greater than 0")
        BigDecimal offeredPrice,

        @Schema(description = "Requested quantity for the negotiation", example = "5.0")
        @NotNull(message = "Requested quantity is required")
        @DecimalMin(value = "0.01", message = "Requested quantity must be greater than 0")
        BigDecimal requestedQuantity
) {
}

