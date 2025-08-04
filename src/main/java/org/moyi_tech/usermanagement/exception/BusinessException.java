package org.moyi_tech.usermanagement.exception;

import org.springframework.http.HttpStatus;

public abstract class BusinessException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;
    private final Object details;

    protected BusinessException(String errorCode, String message, HttpStatus httpStatus) {
        this(errorCode, message, httpStatus, null);
    }

    protected BusinessException(String errorCode, String message, HttpStatus httpStatus, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = details;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public Object getDetails() {
        return details;
    }
}