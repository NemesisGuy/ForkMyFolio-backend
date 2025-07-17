package com.forkmyfolio.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles validation errors for request bodies annotated with @Valid.
     * This provides a much more detailed error response than the Spring Boot default.
     *
     * @param ex The exception thrown when validation fails.
     * @return A ResponseEntity containing a structured error response.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation error. Check 'errors' field for details.");

        // The 'bindingResult' contains all the details about which fields failed validation.
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errorResponse.addValidationError(fieldName, errorMessage);
            log.warn("Validation error on field '{}': {}", fieldName, errorMessage);
        });

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handles validation errors for method parameters (e.g., @RequestParam, @PathVariable).
     * This is triggered by method-level validation annotations in controllers.
     *
     * @param ex The exception thrown when method validation fails.
     * @return A ResponseEntity containing a structured error response.
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleMethodValidation(HandlerMethodValidationException ex) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation error. Check 'errors' field for details.");

        ex.getAllValidationResults().forEach(result -> {
            String paramName = result.getMethodParameter().getParameterName();
            result.getResolvableErrors().forEach(error -> {
                errorResponse.addValidationError(paramName, error.getDefaultMessage());
                log.warn("Validation error on parameter '{}': {}", paramName, error.getDefaultMessage());
            });
        });

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handles all other uncaught exceptions, providing a generic but structured 500 error response.
     * This prevents exposing stack traces or default Spring error pages to the client.
     *
     * @param ex The uncaught exception.
     * @return A ResponseEntity containing a structured 500 error response.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception ex) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected internal server error occurred.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}