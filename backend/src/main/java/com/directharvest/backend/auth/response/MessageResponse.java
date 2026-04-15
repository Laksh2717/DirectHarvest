package com.directharvest.backend.auth.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Simple message response.")
public record MessageResponse(
	@Schema(description = "Message text", example = "Operation successful")
	String message
) {
}

