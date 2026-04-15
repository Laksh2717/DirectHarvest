package com.directharvest.backend.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseAppException {
    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}

