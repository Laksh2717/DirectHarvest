package com.directharvest.backend.ratings.controller;

import com.directharvest.backend.ratings.request.CreateRatingRequest;
import com.directharvest.backend.ratings.response.RatingResponse;
import com.directharvest.backend.ratings.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Ratings", description = "Buyer ratings for farmers after completed orders. Auth uses cookie-based JWT (access_token).")
@SecurityRequirement(name = "cookieAuth")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping("/orders/{orderId}/rating")
    @Operation(summary = "Rate farmer for an order", description = "Only the buyer of a COMPLETED order can submit a 1-5 score for the farmer. Duplicate ratings for the same buyer/farmer/listing are blocked.")
    public ResponseEntity<RatingResponse> createRating(
            @PathVariable Long orderId,
            @Valid @RequestBody CreateRatingRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingService.createForOrder(orderId, request));
    }

    @GetMapping("/users/{userId}/ratings")
    @Operation(summary = "Get ratings received by user", description = "Returns ratings received by the specified user, typically a farmer.")
    public ResponseEntity<List<RatingResponse>> getUserRatings(@PathVariable Long userId) {
        return ResponseEntity.ok(ratingService.getRatingsForUser(userId));
    }
}

