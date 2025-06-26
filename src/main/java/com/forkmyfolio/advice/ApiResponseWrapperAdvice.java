package com.forkmyfolio.advice;

import com.forkmyfolio.controller.AuthController;
import com.forkmyfolio.controller.ContactMessageController;
import com.forkmyfolio.controller.ProjectController;
import com.forkmyfolio.controller.SkillController;
import com.forkmyfolio.controller.UserController;
import com.forkmyfolio.dto.response.ApiResponseWrapper;
import com.forkmyfolio.exception.GlobalExceptionHandler;
// Removed: import org.springdoc.core.versions.ActuatorProvider;
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
 *
 * This advice aims to provide a consistent response structure across the API.
 * It will not wrap:
 *  - Responses that are already an instance of {@link ApiResponseWrapper}.
 *  - Responses from {@link GlobalExceptionHandler} as they already produce {@link ApiResponseWrapper}.
 *  - Responses from Springdoc OpenAPI endpoints (e.g., /api-docs, /swagger-ui).
 *  - Responses from Spring Boot Actuator endpoints.
 *  - Responses that are {@link ProblemDetail} instances.
 *  - Void responses or ResponseEntity<Void>.
 */
@ControllerAdvice(assignableTypes = { // Only apply to our main controllers
        AuthController.class,
        ContactMessageController.class,
        ProjectController.class,
        SkillController.class,
        UserController.class
})
public class ApiResponseWrapperAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Do not wrap if the method already returns ApiResponseWrapper or ResponseEntity<ApiResponseWrapper>
        if (returnType.getParameterType().isAssignableFrom(ApiResponseWrapper.class)) {
            return false;
        }
        if (returnType.getParameterType().isAssignableFrom(ResponseEntity.class)) {
            // Check if ResponseEntity's generic type is ApiResponseWrapper
            if (returnType.getGenericParameterType().getTypeName().contains(ApiResponseWrapper.class.getName())) {
                return false;
            }
        }
        // Do not wrap if the controller is GlobalExceptionHandler
        if (Objects.equals(returnType.getContainingClass(), GlobalExceptionHandler.class)) {
            return false;
        }
        // Do not wrap Springdoc OpenAPI endpoints
        if (Objects.equals(returnType.getContainingClass(), OpenApiWebMvcResource.class)) {
            return false;
        }
        // Do not wrap Actuator endpoints (basic check, could be more specific)
        if (returnType.getContainingClass().getName().startsWith("org.springframework.boot.actuate")) {
            return false;
        }
        // Do not wrap if return type is void
        if (returnType.getParameterType().isAssignableFrom(Void.TYPE) || returnType.getParameterType().isAssignableFrom(Void.class) ) {
            return false;
        }
        // For ResponseEntity<Void>, the body will be null, handle in beforeBodyWrite
        if (returnType.getParameterType().isAssignableFrom(ResponseEntity.class) &&
            returnType.getGenericParameterType().getTypeName().contains(Void.class.getName())) {
            return false;
        }

        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        // If the body is null (e.g. ResponseEntity<Void>.build()), don't wrap.
        // The 'supports' method should already filter out most Void cases.
        if (body == null) {
            // If it's a ResponseEntity, it might have status codes (e.g. 204 No Content)
            // that should be preserved without a body wrapper.
            if (returnType.getParameterType().isAssignableFrom(ResponseEntity.class)) {
                return null;
            }
        }

        // Do not wrap if body is already an ApiResponseWrapper (double-check, though 'supports' should catch this)
        if (body instanceof ApiResponseWrapper) {
            return body;
        }

        // Do not wrap ProblemDetail instances (used by Spring Boot for default error responses)
        if (body instanceof ProblemDetail) {
            return body;
        }

        // For other successful responses, wrap them
        return new ApiResponseWrapper<>(body);
    }
}
