# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - YYYY-MM-DD

### ✨ Features

*   **Full CRUD for Qualifications**: Implemented a complete set of endpoints for users to manage their academic and professional qualifications.
*   **OAuth2 Authentication**: Implemented a complete, stateless OAuth2 login flow for providers like Google.
*   **Custom Date Range Validation**: Added a reusable `@ValidDateRange` annotation to ensure an end date is not before a start date.
*   **Enhanced Data Model Relationships**: Linked `Project` and `Experience` entities to `Skill` via a many-to-many relationship.
*   **Standardized API Responses**: Implemented a generic `ApiResponseWrapper<T>` to provide consistent JSON structure for all API responses.

### ♻️ Refactoring

*   **Architectural Alignment for User Management**: Refactored the admin user management endpoints to strictly adhere to clean architecture principles.
*   **Service Layer Cohesion**: Updated the `UserService` interface to be perfectly in sync with its implementation.
*   **Controllers Refactoring**: Refactored controllers to return raw DTOs, allowing an `ApiResponseWrapperAdvice` to handle wrapping automatically.

### 🚀 Infrastructure & Configuration

*   **Security Configuration**: Updated `SecurityConfig` to integrate and configure the entire OAuth2 login flow.
*   **CORS Configuration**: Centralized CORS configuration within `SecurityConfig`.
*   **JWT Role Prefixing**: Corrected JWT role prefixing to use "ROLE_" in alignment with Spring Security conventions.

### 🗃️ Database

*   **Flyway Migrations**: Added several database migrations to support new features and data model changes, including:
    *   Enhancements to the `qualifications` table.
    *   Support for OAuth2 in the `users` table.
    *   Join tables for `project_skills` and `experience_skills`.

### 🐛 Fixes

*   Resolved test instability in `AuthControllerTest`.
*   Ensured `MissingRefreshTokenCookieException` is correctly handled.
*   Corrected various minor test assertion and compilation issues.

### 🗑️ Removed

*   Deleted the old `com.forkmyfolio.dto.ApiResponse` DTO, which was superseded by `ApiResponseWrapper`.
*   Removed `ErrorDetails` DTO, replaced by `ApiResponseWrapper` with `FieldErrorDto`.
