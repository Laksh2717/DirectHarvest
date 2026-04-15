package com.directharvest.backend.common.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends BaseAppException {
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}

