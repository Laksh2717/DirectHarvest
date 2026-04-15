package com.directharvest.backend.listings.request;

import com.directharvest.backend.common.validation.IndianPincode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateListingRequest(
        @Schema(example = "Wheat")
        @NotBlank(message = "Crop name is required")
        @Size(max = 120, message = "Crop name must be at most 120 characters")
        String cropName,

        @Schema(example = "Fresh harvest, moisture controlled")
        @NotNull(message = "Description is required")
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
        String pincode
) {
}
