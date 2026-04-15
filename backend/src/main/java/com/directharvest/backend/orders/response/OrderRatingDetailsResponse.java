package com.directharvest.backend.orders.response;

import java.time.Instant;

public record OrderRatingDetailsResponse(
        Long id,
        Integer score,
        Long raterId,
        String raterName,
        String raterEmail,
        Long ratedUserId,
        String ratedUserName,
        String ratedUserEmail,
        Instant createdAt,
        Instant updatedAt
) {
}
