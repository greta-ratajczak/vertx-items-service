package org.example.exceptions;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final ErrorReason errorReason;
    private final int statusCode;

    public ApiException(ErrorReason errorReason) {
        super(errorReason.getMessage());
        this.errorReason = errorReason;
        this.statusCode = errorReason.getStatusCode();
    }
}