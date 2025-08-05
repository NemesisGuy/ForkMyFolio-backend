package com.forkmyfolio.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception thrown when an attempt is made to create a resource
 * that already exists in the system (e.g., creating a user with an email
 * that is already registered).
 *
 * This exception is mapped to an HTTP 409 Conflict status.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ResourceAlreadyExistsException extends RuntimeException {

    /**
     * Constructs a new ResourceAlreadyExistsException with the specified detail message.
     *
     * @param message the detail message.
     */
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}