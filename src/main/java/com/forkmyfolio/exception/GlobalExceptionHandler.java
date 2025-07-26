package com.forkmyfolio.exception;

import com.forkmyfolio.aop.SkipApiResponseWrapper;
import com.forkmyfolio.advice.ApiResponseWrapper;
import com.forkmyfolio.dto.response.FieldErrorDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice(basePackages = "com.forkmyfolio.controller")
@Slf4j
public class GlobalExceptionHandler implements ResponseBodyAdvice<Object> {

    // --- ResponseBodyAdvice implementation (for success wrapping) ---

    /**
     * Determines if this advice should be applied. It will not be applied to methods
     * annotated with @SkipApiResponseWrapper, which is used for file downloads.
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return !returnType.hasMethodAnnotation(SkipApiResponseWrapper.class);
    }

    /**
     * Wraps successful responses in the standard ApiResponseWrapper before the body is written.
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        // Do not wrap if the body is already our wrapper, a Spring ProblemDetail, or a file download.
        if (body instanceof ApiResponseWrapper || body instanceof ProblemDetail || body instanceof byte[]) {
            return body;
        }

        return new ApiResponseWrapper<>(body);
    }

    // --- ExceptionHandler implementations (for error handling) ---

    /**
     * Handles validation errors for request bodies (@Valid).
     * Returns a standard ApiResponseWrapper with validation details.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseWrapper<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<FieldErrorDto> errors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    log.warn("Validation error on field '{}': {}", fieldName, errorMessage);
                    return new FieldErrorDto(fieldName, errorMessage);
                })
                .collect(Collectors.toList());
        return new ApiResponseWrapper<>(errors, "validation_failed");
    }

    /**
     * Handles validation errors for method parameters (@RequestParam, etc.).
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseWrapper<Object> handleMethodValidation(HandlerMethodValidationException ex) {
        List<FieldErrorDto> errors = ex.getAllValidationResults().stream()
                .flatMap(result -> {
                    String paramName = result.getMethodParameter().getParameterName();
                    return result.getResolvableErrors().stream().map(error -> {
                        log.warn("Validation error on parameter '{}': {}", paramName, error.getDefaultMessage());
                        return new FieldErrorDto(paramName, error.getDefaultMessage());
                    });
                })
                .collect(Collectors.toList());
        return new ApiResponseWrapper<>(errors, "validation_failed");
    }

    /**
     * A final catch-all for any unexpected exceptions.
     * Returns a generic error message to avoid leaking implementation details.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponseWrapper<Object> handleAllUncaughtException(Exception ex) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        List<FieldErrorDto> errors = List.of(new FieldErrorDto("general", "An unexpected internal server error occurred."));
        return new ApiResponseWrapper<>(errors, "error");
    }
}