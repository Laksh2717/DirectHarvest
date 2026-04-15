package com.directharvest.backend.negotiations.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload for submitting a counter offer in a negotiation.")
public record CounterOfferRequest(
        @Schema(description = "Offered price for the counter offer", example = "120.50")
        @NotNull(message = "Offered price is required")
        @DecimalMin(value = "0.01", message = "Offered price must be greater than 0")
        BigDecimal offeredPrice,

        @Schema(description = "Requested quantity for the counter offer", example = "10.0")
        @NotNull(message = "Requested quantity is required")
        @DecimalMin(value = "0.01", message = "Requested quantity must be greater than 0")
        BigDecimal requestedQuantity
) {
}

