package com.forkmyfolio.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception thrown when an error occurs during token refresh operations,
 * such as when a refresh token is expired, invalid, or not found.
 * Responds with HTTP status 401 (Unauthorized) or 403 (Forbidden) depending on the context
 * in the GlobalExceptionHandler. For simplicity here, it's a generic runtime exception.
 * Specific handling can be added in GlobalExceptionHandler.
 */
@ResponseStatus(HttpStatus.FORBIDDEN) // Or HttpStatus.UNAUTHORIZED, depending on desired client behavior
public class TokenRefreshException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new TokenRefreshException with the specified refresh token and detail message.
     *
     * @param token   The refresh token that caused the error.
     * @param message The detail message.
     */
    public TokenRefreshException(String token, String message) {
        super(String.format("Failed for [%s]: %s", token, message));
    }

    /**
     * Constructs a new TokenRefreshException with the specified detail message.
     *
     * @param message The detail message.
     */
    public TokenRefreshException(String message) {
        super(message);
    }
}
