package org.example.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorReason {
    USER_ALREADY_EXISTS(409, "User already exists"),
    INVALID_CREDENTIALS(401, "Invalid login or password"),
    UNAUTHORIZED(401, "Unauthorized access"),
    INVALID_REQUEST(400, "Invalid request"),
    INTERNAL_SERVER_ERROR(500, "Internal server error");

    private final int statusCode;
    private final String message;
}