package com.directharvest.backend.listings.request;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record UpdateListingPriceRequest(
        @DecimalMin(value = "0.01", message = "Price per kg must be greater than 0")
        BigDecimal pricePerKg
) {
}

