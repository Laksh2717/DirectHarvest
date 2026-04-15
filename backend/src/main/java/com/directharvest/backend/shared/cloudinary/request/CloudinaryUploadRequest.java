package com.directharvest.backend.shared.cloudinary.request;

import jakarta.validation.constraints.NotBlank;

public record CloudinaryUploadRequest(
        @NotBlank(message = "File is required")
        String file
) {
}

