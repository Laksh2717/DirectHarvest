package com.directharvest.backend.dashboard.response;

public record BuyerKpisResponse(
        Long totalActiveNegotiations,
        Long activeOrders,
        Long totalCompletedOrders,
        Long totalCancelledOrders
) {}
