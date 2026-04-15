package com.directharvest.backend.shared.cloudinary.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Cloudinary upload response.")
public record CloudinaryUploadResponse(
        @Schema(description = "Cloudinary public ID", example = "sample/abc123")
        String publicId,
        @Schema(description = "Secure URL to the uploaded asset", example = "https://res.cloudinary.com/demo/image/upload/v1234567890/sample.jpg")
        String secureUrl,
        @Schema(description = "File format", example = "jpg")
        String format,
        @Schema(description = "Image width in pixels", example = "800")
        Integer width,
        @Schema(description = "Image height in pixels", example = "600")
        Integer height,
        @Schema(description = "File size in bytes", example = "204800")
        Long bytes
) {
}

