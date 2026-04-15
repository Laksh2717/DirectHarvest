package com.directharvest.backend.common.response;

public record FieldErrorResponse(
        String field,
        String message,
        Object rejectedValue
) {
}

