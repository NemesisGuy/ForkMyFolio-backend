package com.forkmyfolio.exception;

import com.forkmyfolio.dto.response.ApiResponseWrapper;
import com.forkmyfolio.dto.response.FieldErrorDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application.
 * Uses {@link ControllerAdvice} to centralize exception handling logic across all controllers.
 * Extends {@link ResponseEntityExceptionHandler} to customize handling of Spring MVC exceptions.
 * All responses are wrapped in {@link ApiResponseWrapper}.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles {@link ResourceNotFoundException}.
     *
     * @param ex The exception.
     * @return A {@link ResponseEntity} containing an {@link ApiResponseWrapper} with error details and an HTTP 404 Not Found status.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseWrapper<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        FieldErrorDto error = new FieldErrorDto("resource", ex.getMessage());
        ApiResponseWrapper<Object> apiResponse = new ApiResponseWrapper<>(Collections.singletonList(error), "fail");
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles {@link EmailAlreadyExistsException}.
     *
     * @param ex The exception.
     * @return A {@link ResponseEntity} containing an {@link ApiResponseWrapper} with error details and an HTTP 400 Bad Request status.
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponseWrapper<Object>> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        FieldErrorDto error = new FieldErrorDto("email", ex.getMessage());
        ApiResponseWrapper<Object> apiResponse = new ApiResponseWrapper<>(Collections.singletonList(error), "fail");
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles {@link UsernameNotFoundException} from Spring Security.
     *
     * @param ex The exception.
     * @return A {@link ResponseEntity} containing an {@link ApiResponseWrapper} with error details and an HTTP 404 Not Found status.
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponseWrapper<Object>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        FieldErrorDto error = new FieldErrorDto("user", ex.getMessage());
        ApiResponseWrapper<Object> apiResponse = new ApiResponseWrapper<>(Collections.singletonList(error), "fail");
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles {@link AccessDeniedException} from Spring Security (authorization failure).
     *
     * @param ex The exception.
     * @return A {@link ResponseEntity} containing an {@link ApiResponseWrapper} with error details and an HTTP 403 Forbidden status.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseWrapper<Object>> handleAccessDeniedException(AccessDeniedException ex) {
        FieldErrorDto error = new FieldErrorDto("authorization", "Access Denied: You do not have permission to access this resource.");
        ApiResponseWrapper<Object> apiResponse = new ApiResponseWrapper<>(Collections.singletonList(error), "forbidden");
        return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handles {@link TokenRefreshException} for issues with refresh tokens.
     *
     * @param ex The exception.
     * @return A {@link ResponseEntity} containing an {@link ApiResponseWrapper} with error details and an HTTP 401 Unauthorized status.
     */
    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<ApiResponseWrapper<Object>> handleTokenRefreshException(TokenRefreshException ex) {
        FieldErrorDto error = new FieldErrorDto("refreshToken", ex.getMessage());
        ApiResponseWrapper<Object> apiResponse = new ApiResponseWrapper<>(Collections.singletonList(error), "unauthorized");
        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles {@link AuthenticationException} which is a base class for authentication failures.
     * This can catch issues like BadCredentialsException.
     *
     * @param ex The exception.
     * @return A {@link ResponseEntity} containing an {@link ApiResponseWrapper} with error details and an HTTP 401 Unauthorized status.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponseWrapper<Object>> handleAuthenticationException(AuthenticationException ex) {
        logger.warn("Authentication failed: " + ex.getMessage());
        FieldErrorDto error = new FieldErrorDto("authentication", "Authentication failed: " + ex.getMessage());
        ApiResponseWrapper<Object> apiResponse = new ApiResponseWrapper<>(Collections.singletonList(error), "unauthorized");
        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Customizes the response for {@link MethodArgumentNotValidException} (validation failures).
     * This method is overridden from {@link ResponseEntityExceptionHandler}.
     *
     * @param ex      The exception.
     * @param headers The headers to be written to the response.
     * @param status  The selected response status.
     * @param request The current request.
     * @return A {@link ResponseEntity} containing an {@link ApiResponseWrapper} with detailed validation errors and an HTTP 400 Bad Request status.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        List<FieldErrorDto> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new FieldErrorDto(fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(Collectors.toList());

        ApiResponseWrapper<Object> apiResponse = new ApiResponseWrapper<>(fieldErrors, "validation_failed");
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles any other unhandled {@link Exception}.
     * This acts as a fallback for unexpected errors.
     *
     * @param ex The exception.
     * @return A {@link ResponseEntity} containing an {@link ApiResponseWrapper} with error details and an HTTP 500 Internal Server Error status.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseWrapper<Object>> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("An unexpected error occurred: Path: " + request.getDescription(false) + " Exception: " + ex.getClass().getName(), ex);
        FieldErrorDto error = new FieldErrorDto("general", "An unexpected internal server error occurred. Please try again later.");
        ApiResponseWrapper<Object> apiResponse = new ApiResponseWrapper<>(Collections.singletonList(error), "error");
        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles MissingRefreshTokenCookieException, typically when the refresh token cookie is not found.
     * Returns HTTP 401 Unauthorized.
     *
     * @param ex      The MissingRefreshTokenCookieException instance.
     * @param request The current web request.
     * @return A {@link ResponseEntity} containing an {@link ApiResponseWrapper} with error details and an HTTP 401 Unauthorized status.
     */
    @ExceptionHandler(MissingRefreshTokenCookieException.class)
    public ResponseEntity<ApiResponseWrapper<Object>> handleMissingRefreshTokenCookieException(MissingRefreshTokenCookieException ex, WebRequest request) {
        logger.warn("Entered handleMissingRefreshTokenCookieException for: " + ex.getMessage(), ex);
        List<FieldErrorDto> errors = Collections.singletonList(new FieldErrorDto("refreshToken", ex.getMessage()));
        ApiResponseWrapper<Object> apiResponse = new ApiResponseWrapper<>(errors, "unauthorized");
        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }
}
