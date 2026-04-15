package com.directharvest.backend.orders.request;

import com.directharvest.backend.common.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload for updating the status of an order.")
public record UpdateOrderStatusRequest(
        @Schema(description = "New status for the order", example = "COMPLETED")
        @NotNull(message = "Status is required")
        OrderStatus status
) {
}

