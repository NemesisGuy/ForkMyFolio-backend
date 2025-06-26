package com.forkmyfolio.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when the refresh token cookie is missing from the request.
 */
// @ResponseStatus(HttpStatus.UNAUTHORIZED) // Removed to let GlobalExceptionHandler fully control the response
public class MissingRefreshTokenCookieException extends RuntimeException {

    /**
     * Constructs a new MissingRefreshTokenCookieException with the specified detail message.
     *
     * @param message the detail message.
     */
    public MissingRefreshTokenCookieException(String message) {
        super(message);
    }
}
