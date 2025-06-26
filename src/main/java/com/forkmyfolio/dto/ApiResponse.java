package com.forkmyfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A generic Data Transfer Object for API responses, typically used for simple
 * success or error messages.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {

    /**
     * Indicates whether the operation was successful.
     */
    private boolean success;

    /**
     * A message providing details about the outcome of the operation.
     * This could be a success message or an error detail.
     */
    private String message;
}
