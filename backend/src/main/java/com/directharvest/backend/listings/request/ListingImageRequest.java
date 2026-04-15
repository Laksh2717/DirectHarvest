package com.directharvest.backend.listings.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ListingImageRequest(
        @Schema(example = "directharvest/listings/wheat_01")
        @NotBlank(message = "Cloudinary public id is required")
        @Size(max = 255, message = "Cloudinary public id must be at most 255 characters")
        String cloudinaryPublicId,

        @Schema(example = "https://res.cloudinary.com/demo/image/upload/v1/directharvest/listings/wheat_01.jpg")
        @NotBlank(message = "Cloudinary secure URL is required")
        @Size(max = 500, message = "Cloudinary secure URL must be at most 500 characters")
        String cloudinarySecureUrl,

        @Schema(example = "jpg")
        @Size(max = 20, message = "Format must be at most 20 characters")
        String format,

        @Schema(example = "1280")
        Integer width,

        @Schema(example = "720")
        Integer height,

        @Schema(example = "145678")
        Long bytes,

        @Schema(description = "Mark one image as primary", example = "true")
        Boolean primary
) {
}
