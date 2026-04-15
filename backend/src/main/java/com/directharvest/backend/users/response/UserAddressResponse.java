package com.directharvest.backend.users.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User address response.")
public record UserAddressResponse(
        @Schema(description = "Street address", example = "Plot 17, Ring Road")
        String street,
        @Schema(description = "City", example = "Surat")
        String city,
        @Schema(description = "State", example = "Gujarat")
        String state,
        @Schema(description = "Pincode", example = "395007")
        String pincode
) {
}
