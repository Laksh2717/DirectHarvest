package com.directharvest.backend.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseAppException {
    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}

