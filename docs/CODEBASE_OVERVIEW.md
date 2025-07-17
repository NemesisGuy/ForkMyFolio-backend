# Codebase Overview

This document provides a detailed overview of the project's structure, including the purpose of each package and key classes.

## Project Structure

The project follows a standard Maven project structure:

- `src/main/java/com/forkmyfolio`: Root package for all Java source code.
  - `config`: Spring configuration classes (SecurityConfig, OpenApiConfig).
  - `controller`: REST API controllers that handle incoming HTTP requests.
  - `dto`: Data Transfer Objects used for API request and response payloads.
  - `exception`: Custom exception classes and the global exception handler.
  - `model`: JPA entities representing the application's domain model (User, Project, etc.).
  - `repository`: Spring Data JPA repositories for database interactions.
  - `security`: Classes related to Spring Security, JWT handling, and custom UserDetailsService.
  - `service`: Service layer interfaces.
    - `impl`: Implementations of the service interfaces, containing business logic.
- `src/main/resources`: Application properties (`application.properties`, `application-prod.properties`), and other resources.
- `src/test/java`: Unit and integration tests.

## Key Classes and Packages

### `config`

-   **`SecurityConfig`**: Configures Spring Security, including password encoding, authentication provider, and security filter chain.
-   **`OpenApiConfig`**: Configures OpenAPI (Swagger) documentation.

### `controller`

-   **`AuthController`**: Handles authentication-related requests (`/login`, `/register`, `/refresh-token`, `/logout`).
-   **`ProjectController`**, **`SkillController`**, etc.: Handle CRUD operations for their respective domains.
-   **`GlobalExceptionHandler`**: A `@ControllerAdvice` class that handles exceptions thrown by controllers and returns a standardized error response.

### `dto`

-   Data Transfer Objects (DTOs) are used to define the structure of API request and response bodies. They are used to decouple the API layer from the domain model.

### `exception`

-   This package contains custom exception classes that are used to represent specific error conditions.

### `model`

-   JPA entities that represent the application's domain model. These classes are mapped to database tables.

### `repository`

-   Spring Data JPA repositories that provide an abstraction layer over the database.

### `security`

-   **`JwtService`**: A service that provides methods for creating and validating JWTs.
-   **`UserDetailsServiceImpl`**: Implements Spring Security's `UserDetailsService` interface to load user-specific data.
-   **`JwtAuthenticationFilter`**: A filter that intercepts incoming requests, validates the JWT, and sets the authentication context.

### `service`

-   The service layer contains the application's business logic. It is divided into interfaces and implementations.
-   **`AuthService`**: Contains the business logic for authentication.
-   **`ProjectService`**, **`SkillService`**, etc.: Contain the business logic for their respective domains.
