package com.directharvest.backend.listings.request;

import com.directharvest.backend.common.validation.IndianPincode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CreateListingRequest(
        @Schema(example = "Wheat")
        @NotBlank(message = "Crop name is required")
        @Size(max = 120, message = "Crop name must be at most 120 characters")
        String cropName,

        @Schema(example = "120.50")
        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
        BigDecimal quantity,

        @Schema(example = "27.75")
        @NotNull(message = "Price per kg is required")
        @DecimalMin(value = "0.01", message = "Price per kg must be greater than 0")
        BigDecimal pricePerKg,

        @Schema(example = "Fresh harvest, moisture controlled")
        @Size(max = 1000, message = "Description must be at most 1000 characters")
        String description,

        @Schema(example = "Plot 17, Ring Road")
        @NotBlank(message = "Street address is required")
        @Size(max = 255, message = "Street must be at most 255 characters")
        String street,

        @Schema(example = "Ahmedabad")
        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City must be at most 100 characters")
        String city,

        @Schema(example = "Gujarat")
        @NotBlank(message = "State is required")
        @Size(max = 100, message = "State must be at most 100 characters")
        String state,

        @Schema(description = "Valid Indian pincode (6 digits)", example = "380001")
        @NotBlank(message = "Pincode is required")
        @IndianPincode
        String pincode,

        @Schema(description = "Optional Cloudinary-backed listing images")
        @Size(max = 5, message = "At most 5 images are allowed")
        @Valid
        List<ListingImageRequest> images
) {
}
