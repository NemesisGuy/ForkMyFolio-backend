# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased] - YYYY-MM-DD

### ✨ Features

*   **Full CRUD for Qualifications**: Implemented a complete set of endpoints for users to manage their academic and professional qualifications.
    *   Enhanced the `Qualification` entity with detailed fields like `fieldOfStudy`, `level`, `credentialUrl`, and `institutionLogoUrl`.
    *   Created a full vertical slice including a controller, service, repository, mapper, and DTOs, all adhering to the project's clean architecture rules.

*   **OAuth2 Authentication**: Implemented a complete, stateless OAuth2 login flow.
    *   Users can now register and authenticate using external providers (e.g., Google).
    *   Added `HttpCookieOAuth2AuthorizationRequestRepository` to manage the authorization flow without using HTTP sessions, preserving the stateless nature of the API.
    *   Created `CustomOAuth2UserService` to process user information from providers, linking them to or creating new `User` accounts.
    *   Implemented `OAuth2AuthenticationSuccessHandler` to generate JWT and refresh tokens upon successful login, seamlessly integrating with the existing security model.
    *   Implemented `OAuth2AuthenticationFailureHandler` for graceful error handling and redirection.

*   **Custom Date Range Validation**: Added a reusable custom validator for date ranges.
    *   Created the `@ValidDateRange` annotation to ensure an end date is not before a start date.
    *   Implemented `DateRangeValidator` to provide the validation logic.
    *   Applied this validation to the `CreateExperienceRequest` and `UpdateExperienceRequest` DTOs to enforce data integrity at the API boundary.

*   **Enhanced Data Model Relationships**: Improved data model integrity by establishing clear relationships between entities.
    *   Linked `Project` and `Experience` entities to `Skill` via a many-to-many relationship, replacing previous plain-text fields.
    *   Updated `Project` and `Experience` DTOs to accept a set of `skillUuids`, allowing for robust relationship management through the API.

### ♻️ Refactoring

*   **Architectural Alignment for User Management**: Refactored the admin user management endpoints to strictly adhere to the project's clean architecture principles.
    *   All business logic for creating and updating users is now fully encapsulated within the `UserServiceImpl`.
    *   The `AdminController` is now a thin layer that delegates directly to the service, passing primitive data from DTOs rather than domain entities.
    *   Removed mapping logic from the controller and simplified the `UserMapper`, ensuring a clear separation of concerns.

*   **Service Layer Cohesion**: Updated the `UserService` interface to be perfectly in sync with its implementation (`UserServiceImpl`), reinforcing the "program to an interface" principle.

### 🚀 Infrastructure & Configuration

*   **Security Configuration**: Updated `SecurityConfig` to integrate and configure the entire OAuth2 login flow, including all custom handlers and services.
*   **CORS Configuration**: Improved and centralized CORS configuration within `SecurityConfig` to use the standard `CorsConfigurationSource` bean, enhancing security and maintainability.

### 🗃️ Database

*   **Flyway Migration (V8)**: Added a migration to enhance the `qualifications` table with new columns for richer academic data and support for ongoing studies.

*   **Flyway Migration (V7)**: Added a new database migration to support OAuth2.
    *   Added `provider` (e.g., 'LOCAL', 'GOOGLE') and `provider_id` columns to the `users` table.
    *   Modified the `password` column to be nullable to accommodate users who sign up via an external provider and do not have a locally stored password.
*   **Flyway Migrations (V4, V5)**: Added migrations for `project_skills` and `experience_skills` join tables to support the new many-to-many relationships.

---