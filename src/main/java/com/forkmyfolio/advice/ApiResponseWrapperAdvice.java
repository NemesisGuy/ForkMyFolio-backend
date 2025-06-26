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

    private static final org.apache.commons.logging.Log logger = org.apache.commons.logging.LogFactory.getLog(ApiResponseWrapperAdvice.class);

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        String controllerName = returnType.getContainingClass().getName();
        // String methodName = returnType.getMethod().getName(); // Corrected for logging previously
        String returnTypeName = returnType.getParameterType().getName();
        // logger.info("ApiResponseWrapperAdvice.supports called for: " + controllerName + "#" + methodName + ", returnType: " + returnTypeName);

        // Do not wrap if the method already returns ApiResponseWrapper or ResponseEntity<ApiResponseWrapper>
        if (returnType.getParameterType().isAssignableFrom(ApiResponseWrapper.class)) {
            // logger.info("ApiResponseWrapperAdvice: supports=false (already ApiResponseWrapper)");
            return false;
        }
        if (returnType.getParameterType().isAssignableFrom(ResponseEntity.class)) {
            if (returnType.getGenericParameterType().getTypeName().contains(ApiResponseWrapper.class.getName())) {
                // logger.info("ApiResponseWrapperAdvice: supports=false (already ResponseEntity<ApiResponseWrapper>)");
                return false;
            }
        }
        // Do not wrap if the controller is GlobalExceptionHandler
        if (Objects.equals(returnType.getContainingClass(), GlobalExceptionHandler.class)) {
            // logger.info("ApiResponseWrapperAdvice: supports=false (GlobalExceptionHandler)");
            return false;
        }
        // Do not wrap Springdoc OpenAPI endpoints
        if (Objects.equals(returnType.getContainingClass(), OpenApiWebMvcResource.class) ||
            controllerName.startsWith("org.springdoc")) { // Broader check for springdoc
            // logger.info("ApiResponseWrapperAdvice: supports=false (Springdoc OpenAPI endpoint)");
            return false;
        }
        // Do not wrap Actuator endpoints
        if (controllerName.startsWith("org.springframework.boot.actuate")) {
            // logger.info("ApiResponseWrapperAdvice: supports=false (Actuator endpoint)");
            return false;
        }
        // Do not wrap if return type is void or ResponseEntity<Void>
        if (returnTypeName.equals("void") || returnType.getParameterType().isAssignableFrom(Void.class)) {
             // logger.info("ApiResponseWrapperAdvice: supports=false (Void return type)");
            return false;
        }
        if (returnType.getParameterType().isAssignableFrom(ResponseEntity.class) &&
            returnType.getGenericParameterType().getTypeName().contains(Void.class.getName())) {
            // logger.info("ApiResponseWrapperAdvice: supports=false (ResponseEntity<Void>)");
            return false;
        }
        // Do not wrap if body is ProblemDetail (RFC 7807)
        // This check is more effective in beforeBodyWrite, but we can try to infer from returnType if it's ResponseEntity<ProblemDetail>
        if (returnType.getParameterType().isAssignableFrom(ResponseEntity.class) &&
            returnType.getGenericParameterType().getTypeName().contains(ProblemDetail.class.getName())) {
            // logger.info("ApiResponseWrapperAdvice: supports=false (ResponseEntity<ProblemDetail>)");
            return false;
        }

        // logger.info("ApiResponseWrapperAdvice: supports=true for " + controllerName + "#" + methodName);
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
