package com.directharvest.backend.shared.cloudinary.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Cloudinary signed upload response.")
public record CloudinarySignedUploadResponse(
        @Schema(description = "Cloudinary cloud name", example = "demo")
        String cloudName,
        @Schema(description = "Cloudinary API key", example = "1234567890")
        String apiKey,
        @Schema(description = "Timestamp for the signature", example = "1713000000")
        long timestamp,
        @Schema(description = "Signature for the upload", example = "abcdef1234567890")
        String signature,
        @Schema(description = "Target folder for upload", example = "sample")
        String folder,
        @Schema(description = "Public ID for the upload", example = "sample/abc123")
        String publicId,
        @Schema(description = "Upload URL", example = "https://api.cloudinary.com/v1_1/demo/image/upload")
        String uploadUrl
) {
}

