package com.forkmyfolio.exception;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for sending standardized error responses from the API.
 * Includes a timestamp, message, specific error details or validation errors, and the request path.
 */
@Getter
@Setter
public class ErrorDetails {
    private LocalDateTime timestamp;
    private String message;
    private String path;
    private Map<String, List<String>> validationErrors; // For validation errors
    private String errorDetails; // For other specific error details

    /**
     * Constructor for general errors.
     * @param timestamp Timestamp of the error.
     * @param message General error message.
     * @param path Request path where the error occurred.
     * @param errorDetails Specific details about the error.
     */
    public ErrorDetails(LocalDateTime timestamp, String message, String path, String errorDetails) {
        this.timestamp = timestamp;
        this.message = message;
        this.path = path;
        this.errorDetails = errorDetails;
    }

    /**
     * Constructor for validation errors.
     * @param timestamp Timestamp of the error.
     * @param message General error message (e.g., "Validation Failed").
     * @param path Request path where the error occurred.
     * @param validationErrors Map of field names to lists of validation error messages.
     */
    public ErrorDetails(LocalDateTime timestamp, String message, String path, Map<String, List<String>> validationErrors) {
        this.timestamp = timestamp;
        this.message = message;
        this.path = path;
        this.validationErrors = validationErrors;
    }
}
