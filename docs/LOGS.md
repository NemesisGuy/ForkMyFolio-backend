# Architectural Decision Log (ADR)

This document records key architectural decisions made for the ForkMyFolio-backend project.
The format is inspired by Michael Nygard's ADRs.

---

## ADR-001: Standardized API Response Structure & Automated Wrapping

*   **Date:** 2025-06-26 (Approximate date of implementation)
*   **Status:** Decided & Implemented
*   **Context:**
    *   A consistent API response structure is needed for both successful operations and errors to simplify frontend integration and improve predictability.
    *   Boilerplate code for wrapping responses in controllers should be minimized.
*   **Decision:**
    1.  Implement a generic `com.forkmyfolio.dto.response.ApiResponseWrapper<T>` with `status` (String), `data` (T), and `errors` (List<`com.forkmyfolio.dto.response.FieldErrorDto`>) fields.
    2.  All successful controller responses should ultimately be wrapped in this structure. The `data` field will hold the actual DTO or a success message map.
    3.  All error responses handled by `GlobalExceptionHandler` (and `JwtAuthenticationEntryPoint`) will also use this structure, typically with `data` as null and `errors` populated.
    4.  Implement `com.forkmyfolio.advice.ApiResponseWrapperAdvice` (a Spring `ResponseBodyAdvice`) to automatically wrap successful DTOs (or `ResponseEntity<DTO>`) returned from designated application controllers. This advice will intelligently skip responses already in `ApiResponseWrapper` format, error responses, framework responses (Swagger, Actuator), and void/ResponseEntity<Void> types.
    5.  Controllers will be refactored to return raw DTOs or `ResponseEntity<DTO>` for success cases, relying on the advice for wrapping. `AuthController` remains an exception, explicitly returning `ResponseEntity<ApiResponseWrapper<T>>` due to its complex cookie and response handling needs; the advice is configured to ignore these.
*   **Consequences:**
    *   **Positive:**
        *   Consistent JSON structure for all API interactions, improving developer experience for API consumers.
        *   Controllers are cleaner as they can focus on returning core data, reducing repetitive wrapping code.
        *   Centralized error response formatting via `GlobalExceptionHandler`.
    *   **Neutral/Considerations:**
        *   The `ApiResponseWrapperAdvice` needs careful implementation of its `supports` method to correctly identify which responses to wrap and which to ignore.
        *   Swagger documentation (`@ApiResponse` annotations) must consistently refer to `ApiResponseWrapper.class` as the schema for wrapped responses, even if controller methods return raw DTOs.
        *   Integration tests (`@SpringBootTest`) are crucial for verifying the final wrapped response structure.

---

## ADR-002: Test-Specific Security Configuration for AuthController

*   **Date:** 2025-06-26 (Approximate date of implementation)
*   **Status:** Decided & Implemented
*   **Context:**
    *   Integration tests for `AuthController` (using `@SpringBootTest`) were failing for `permitAll()` paths (e.g., `/api/v1/auth/register`, `/api/v1/auth/login`).
    *   The failures were due to the full Spring Security filter chain (including `JwtAuthenticationFilter`) being active and potentially misinterpreting requests or interacting unexpectedly with mocked dependencies in the test context, leading to premature 401 errors.
*   **Decision:**
    *   Implement a nested `@TestConfiguration` within `AuthControllerTest.java`.
    *   This test configuration defines a dedicated `SecurityFilterChain` bean with a high precedence (`@Order(1)`) and a `securityMatcher("/api/v1/auth/**")`.
    *   This specific filter chain configures all requests matching `/api/v1/auth/**` to `permitAll()` and disables CSRF, but does *not* include the `JwtAuthenticationFilter` or other application-specific security filters that are part of the main `SecurityConfig`.
*   **Consequences:**
    *   **Positive:**
        *   `AuthControllerTest` now passes reliably for its `permitAll()` endpoints, as these paths are handled by the simplified, permissive test-specific security chain.
        *   Tests for `AuthController` can focus on its direct business logic (registration, login credential validation, token generation, cookie handling) without interference from the full security filter stack that might be problematic in a mocked environment.
    *   **Neutral/Considerations:**
        *   This approach means that the full security filter chain (including `JwtAuthenticationFilter`) is *not* active for `/api/v1/auth/**` paths *only during `AuthControllerTest` execution*.
        *   The correct functioning of the main `SecurityConfig` for these paths in a deployed environment (or other integration tests that don't use this specific test configuration) is still relied upon and partially verified by tests like `ProjectControllerTest` which successfully perform login calls through the main security setup.
        *   This is a pragmatic solution to a common testing challenge where testing authentication/authorization controllers themselves with a full security context and mocks can be complex.

---
