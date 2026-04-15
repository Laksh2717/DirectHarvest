package com.directharvest.backend.common.exception;

import org.springframework.http.HttpStatus;

public class BaseAppException extends RuntimeException {
    private final HttpStatus status;

    public BaseAppException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public BaseAppException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

