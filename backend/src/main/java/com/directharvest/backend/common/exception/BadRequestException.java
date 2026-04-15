package com.directharvest.backend.common.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseAppException {
    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}

