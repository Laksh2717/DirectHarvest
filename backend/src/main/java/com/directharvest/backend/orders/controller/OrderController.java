package com.directharvest.backend.orders.controller;

import com.directharvest.backend.orders.request.CancelOrderRequest;
import com.directharvest.backend.orders.response.OrderDetailsResponse;
import com.directharvest.backend.orders.response.OrderResponse;
import com.directharvest.backend.orders.service.OrderService;
import com.directharvest.backend.common.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Order endpoints for accepted negotiations. Auth uses cookie-based JWT (access_token).")
@SecurityRequirement(name = "cookieAuth")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('FARMER','BUYER')")
        @Operation(summary = "Get my orders", description = "Returns all participant orders when status is omitted. If one or more status values are provided, returns only matching orders.")
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @Parameter(
                description = "Optional repeatable order-status filter. Example: ?status=CONFIRMED&status=ACTIVE",
                schema = @Schema(implementation = OrderStatus.class)
            )
            @RequestParam(name = "status", required = false) List<OrderStatus> status
    ) {
        return ResponseEntity.ok(orderService.getMyOrders(status));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('FARMER','BUYER')")
    @Operation(summary = "Get order by ID", description = "Returns one order when the authenticated user is buyer or farmer for that order.")
    public ResponseEntity<OrderDetailsResponse> getById(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getById(orderId));
    }

    @PostMapping("/{orderId}/complete")
    @Operation(summary = "Mark order completed", description = "Buyer-only action. Allowed when order is CONFIRMED or ACTIVE. Sets completedAt.")
    public ResponseEntity<OrderResponse> complete(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.completeOrder(orderId));
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Buyer or farmer can cancel only while order is CONFIRMED and within 24 hours of order creation. Optional cancellation reason can be sent in request body.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Optional cancellation payload. If omitted, the order is cancelled without a reason.",
            content = @Content(
                    schema = @Schema(implementation = CancelOrderRequest.class),
                    examples = @ExampleObject(value = "{\n  \"cancellationReason\": \"Buyer requested cancellation due to pickup delay\"\n}")
            )
    )
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long orderId, @Valid @RequestBody(required = false) CancelOrderRequest request) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId, request));
    }
}

