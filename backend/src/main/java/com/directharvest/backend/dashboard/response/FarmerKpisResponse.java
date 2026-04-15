package com.directharvest.backend.dashboard.response;

public record FarmerKpisResponse(
        Long totalActiveListings,
        Long activeOrders,
        Long totalCompletedOrders,
        Double averageRating,
        Long ratingCount,
        Long totalCancelledOrders
) {}
