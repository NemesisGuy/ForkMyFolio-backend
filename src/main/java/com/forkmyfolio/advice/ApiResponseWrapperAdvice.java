package com.forkmyfolio.advice;

import com.forkmyfolio.controller.*;
import com.forkmyfolio.dto.response.ApiResponseWrapper;
import com.forkmyfolio.exception.GlobalExceptionHandler;
import org.springdoc.webmvc.api.OpenApiWebMvcResource;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Objects;

/**
 * A {@link ControllerAdvice} that implements {@link ResponseBodyAdvice} to automatically
 * wrap successful responses from specified controllers into an {@link ApiResponseWrapper}.
 * <p>
 * This advice aims to provide a consistent response structure across the API.
 * It will not wrap:
 * - Responses that are already an instance of {@link ApiResponseWrapper}.
 * - Responses from {@link GlobalExceptionHandler} as they already produce {@link ApiResponseWrapper}.
 * - Responses from Springdoc OpenAPI endpoints (e.g., /api-docs, /swagger-ui).
 * - Responses from Spring Boot Actuator endpoints.
 * - Responses that are {@link ProblemDetail} instances.
 * - Void responses or ResponseEntity<Void>.
 */
@ControllerAdvice(basePackages = "com.forkmyfolio.controller") // Apply to all controllers in this package and subpackages
public class ApiResponseWrapperAdvice implements ResponseBodyAdvice<Object> {

    private static final org.apache.commons.logging.Log logger = org.apache.commons.logging.LogFactory.getLog(ApiResponseWrapperAdvice.class);

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Exclude PdfController as it handles file downloads directly.
        if (returnType.getContainingClass().equals(PdfController.class)) {
            return false;
        }

        // Exclude methods that already return an ApiResponseWrapper.
        if (returnType.getParameterType().isAssignableFrom(ApiResponseWrapper.class)) {
            return false;
        }

        // Exclude methods returning void.
        if (returnType.getParameterType() == Void.TYPE || returnType.getParameterType() == Void.class) {
            return false;
        }

        // For ResponseEntity, inspect the generic type.
        if (returnType.getParameterType().isAssignableFrom(ResponseEntity.class) &&
                (returnType.getGenericParameterType().getTypeName().contains(ApiResponseWrapper.class.getName()) ||
                 returnType.getGenericParameterType().getTypeName().contains(Void.class.getName()) ||
                 returnType.getGenericParameterType().getTypeName().contains(ProblemDetail.class.getName()))) {
            // Exclude ResponseEntity<ApiResponseWrapper>, ResponseEntity<Void>, and ResponseEntity<ProblemDetail>.
           return false;
        }

        // By default, wrap everything else.
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        // String controllerName = returnType.getContainingClass().getName();
        // String methodName = returnType.getMethod().getName(); // Corrected for logging previously
        // logger.info("ApiResponseWrapperAdvice.beforeBodyWrite called for: " + controllerName + "#" + methodName + ", body type: " + (body != null ? body.getClass().getName() : "null"));

        if (body == null) {
            // This case for ResponseEntity<Void> should ideally be caught by `supports` method.
            // If somehow it reaches here and body is null for a non-void ResponseEntity,
            // or a raw null DTO was returned, wrapping it as {data: null} is reasonable.
            if (!(returnType.getParameterType().isAssignableFrom(ResponseEntity.class) &&
                    returnType.getGenericParameterType().getTypeName().contains(Void.class.getName()))) {
                logger.warn("ApiResponseWrapperAdvice: body is null for a type that was expected to be wrapped: " + returnType.getParameterType().getName() + ". Wrapping with null data.");
            }
            return new ApiResponseWrapper<>(null); // Wrap null body if not ResponseEntity<Void>
        }

        if (body instanceof ApiResponseWrapper) {
            // logger.info("ApiResponseWrapperAdvice: body is already ApiResponseWrapper, not wrapping again.");
            return body;
        }

        if (body instanceof ProblemDetail) {
            // logger.info("ApiResponseWrapperAdvice: body is ProblemDetail, not wrapping.");
            return body;
        }

        // logger.info("ApiResponseWrapperAdvice: Wrapping body of type " + body.getClass().getName());
        return new ApiResponseWrapper<>(body);
    }
}
