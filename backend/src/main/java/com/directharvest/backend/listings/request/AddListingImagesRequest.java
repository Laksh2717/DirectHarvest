package com.directharvest.backend.listings.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AddListingImagesRequest(
        @Schema(description = "Images to append to the listing")
        @NotEmpty(message = "At least one image is required")
        @Size(max = 5, message = "At most 5 images are allowed")
        List<@Valid ListingImageRequest> images
) {
}
