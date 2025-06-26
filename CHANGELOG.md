# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html) (conceptually, as it's pre-1.0).

## [Unreleased] - YYYY-MM-DD

*Actual date to be filled upon release/tagging. Current changes are part of ongoing development towards an initial version.*

### Added
- Standardized all API responses using a generic `ApiResponseWrapper<T>` to provide consistent JSON structure for `status`, `data`, and `errors`.
- Implemented `ApiResponseWrapperAdvice` (a Spring `ResponseBodyAdvice`) to automatically wrap successful DTO responses from controllers, reducing boilerplate.
- Introduced `FieldErrorDto` for structured error reporting within `ApiResponseWrapper`.
- Added `API_Endpoints.md` providing a human-readable summary of all REST API endpoints.
- Enhanced controller integration test coverage (`ProjectControllerTest`, `SkillControllerTest`, `ContactMessageControllerTest`, `UserControllerTest`) to:
    - Verify the full `ApiResponseWrapper<T>` structure for successful GET, POST, PUT operations.
    - Verify `ApiResponseWrapper<T>` structure for error scenarios (400, 401, 403, 404) handled by `GlobalExceptionHandler`.
    - Confirm correct 204 No Content responses for DELETE operations.
- Added `MissingRefreshTokenCookieException` and its handler in `GlobalExceptionHandler`.

### Changed
- Refactored controllers (`ProjectController`, `SkillController`, `ContactMessageController`, `UserController`) to return raw DTOs or `ResponseEntity<DTO>`, allowing `ApiResponseWrapperAdvice` to handle wrapping.
- `AuthController` methods now explicitly return `ResponseEntity<ApiResponseWrapper<T>>` and are correctly skipped by the advice.
- `GlobalExceptionHandler` updated to consistently use `ApiResponseWrapper` for all handled exceptions.
- `JwtAuthenticationEntryPoint` updated to return errors in `ApiResponseWrapper` format.
- DELETE operations in `ProjectController` and `SkillController` now return `204 No Content`.
- Swagger documentation (`@ApiResponse` schemas) updated to consistently point to `ApiResponseWrapper.class` and reflect 204 responses for DELETEs.

### Fixed
- Corrected JWT role prefixing in `User.getAuthorities()` to use "ROLE_" (e.g., "ROLE_USER"), aligning with Spring Security conventions and fixing related tests.
- Resolved test instability in `AuthControllerTest` by implementing a test-specific `SecurityFilterChain` for `/api/v1/auth/**` paths.
- Ensured `MissingRefreshTokenCookieException` is correctly handled by `GlobalExceptionHandler` by removing `@ResponseStatus` from the exception class.
- Corrected various minor test assertion and compilation issues during refactoring.

### Removed
- Deleted the old, simpler `com.forkmyfolio.dto.ApiResponse` DTO as its functionality is superseded by `ApiResponseWrapper`.
- Removed `ErrorDetails` DTO, replaced by `ApiResponseWrapper` with `FieldErrorDto`.
