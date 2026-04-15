package com.directharvest.backend.users.request;

import com.directharvest.backend.common.validation.IndianPincode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        @Schema(example = "Laksh Chovatiya")
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @Schema(example = "laksh@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Email is invalid")
        @Size(max = 150, message = "Email must be at most 150 characters")
        String email,

        @Schema(example = "Plot 17, Ring Road")
        @NotBlank(message = "Street is required")
        @Size(max = 150, message = "Street must be at most 150 characters")
        String street,

        @Schema(example = "Surat")
        @NotBlank(message = "City is required")
        @Size(max = 150, message = "City must be at most 150 characters")
        String city,

        @Schema(example = "Gujarat")
        @NotBlank(message = "State is required")
        @Size(max = 150, message = "State must be at most 150 characters")
        String state,

        @Schema(example = "395007", pattern = "^[0-9]{6}$")
        @NotBlank(message = "Pincode is required")
        @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be a valid Indian pincode (6 digits)")
        @IndianPincode
        String pincode
) {
}
