# ForkMyFolio Backend API

ForkMyFolio is a digital portfolio platform. This repository contains the source code for its modern Spring Boot REST API backend.

## Table of Contents

- [About The Project](#about-the-project)
- [Features](#features)
- [Built With](#built-with)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation & Setup](#installation--setup)
- [Running the Application](#running-the-application)
  - [Development Profile (H2 Database)](#development-portfolioProfile-h2-database)
  - [Production Profile (PostgreSQL)](#production-portfolioProfile-postgresql)
- [API Documentation (Swagger UI)](#api-documentation-swagger-ui)
- [Docker](#docker)
  - [Building the Docker Image](#building-the-docker-image)
  - [Running with Docker](#running-with-docker)
- [Environment Variables](#environment-variables)
- [Code Structure](#code-structure)
- [Testing](#testing)
- [Contributing](#contributing)
- [License](#license)

## About The Project

This project provides the backend services for ForkMyFolio. It handles user authentication, portfolio project management, skills tracking, and contact messaging. The API is designed to be RESTful, secure, and easily consumable by a frontend application.

## Features

- User registration and JWT-based authentication with short-lived access tokens and long-lived, HttpOnly refresh tokens (Login, Logout, Token Refresh).
- Role-based access control (USER, ADMIN).
- CRUD operations for Projects (Admin only for CUD).
- CRUD operations for Skills (Admin only for CUD).
- Submission of Contact Messages.
- Auto-generated API documentation via Swagger/OpenAPI.
- Docker support for containerization.
- Separate configurations for Development (H2) and Production (PostgreSQL) environments.

## API Response Structure

All API responses (both for successful requests and errors) are wrapped in a standardized JSON structure:

```json
{
  "status": "success", // Possible values: "success", "fail", "unauthorized", "forbidden", "validation_failed", "error"
  "data": "<T>",       // The actual data payload for the request, or null if not applicable (e.g., for some errors or 204 No Content). The type 'T' varies per endpoint.
  "errors": [          // An array of error details, typically empty for successful responses.
    {
      "field": "fieldName", // Name of the field that caused the error (especially for validation). Can be "general" or "authentication" for other errors.
      "message": "A descriptive error message."
    }
  ]
}
```
This consistent structure helps in simplifying client-side handling of API responses. The specific type for the `data` field for each endpoint is detailed in the [API_Endpoints.md](./API_Endpoints.md) and can be seen in the Swagger UI schemas.

## Built With

*   [Spring Boot](https://spring.io/projects/spring-boot) (v3.2.5)
*   [Spring Security](https://spring.io/projects/spring-security) (JWT)
*   [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
*   [Java 21](https://www.oracle.com/java/technologies/javase/21-relnote-issues.html)
*   [Maven](https://maven.apache.org/)
*   [H2 Database](https://www.h2database.com) (for Development)
*   [PostgreSQL](https://www.postgresql.org/) (for Production)
*   [Lombok](https://projectlombok.org/)
*   [Springdoc OpenAPI](https://springdoc.org/) (for Swagger UI)
*   [JJWT](https://github.com/jwtk/jjwt) (for JWT handling)
*   [Docker](https://www.docker.com/)

## Getting Started

This section will guide you through setting up the project locally.

### Prerequisites

*   Java JDK 21 or later (e.g., OpenJDK, Oracle JDK)
*   Apache Maven 3.6.x or later
*   Git
*   (Optional for Production portfolioProfile) PostgreSQL server running
*   (Optional) Docker Desktop

### Installation & Setup

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/your-username/forkmyfolio-backend.git
    cd forkmyfolio-backend
    ```
    *(Note: Replace `your-username` with the actual path if applicable)*

2.  **Build the project with Maven:**
    This will download dependencies and compile the source code.
    ```bash
    mvn clean install
    ```

## Running the Application

The application can be run using different Spring profiles.

### Development Profile (H2 Database)

This is the default portfolioProfile. It uses an in-memory H2 database.
```bash
mvn spring-boot:run
```
Or, after building the JAR:
```bash
java -jar target/forkmyfolio-backend-0.0.1-SNAPSHOT.jar
```
The application will be available at `http://localhost:8080`.
H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:forkmyfolio_dev`, User: `sa`, Password: (empty))

### Production Profile (PostgreSQL)

This portfolioProfile requires a running PostgreSQL instance and proper configuration (see [Environment Variables](#environment-variables)).
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```
Or, after building the JAR:
```bash
java -jar -Dspring.profiles.active=prod target/forkmyfolio-backend-0.0.1-SNAPSHOT.jar
```

## API Documentation

### Swagger UI (Interactive)
Once the application is running, the interactive Swagger UI documentation can be accessed at:
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

The OpenAPI specification (JSON) is available at:
[http://localhost:8080/api-docs](http://localhost:8080/api-docs)

You can use the "Authorize" button on Swagger UI (top right) to authenticate using a JWT access token obtained from the `/auth/login` or `/auth/register` endpoints. The format is `Bearer <your_jwt_token>`. The refresh token mechanism is handled via HttpOnly cookies and the `/auth/refresh-token` endpoint.

### Static Endpoint Summary
For a quick human-readable summary of all API endpoints, their request/response structures, and authentication requirements, see the [API_Endpoints.md](./API_Endpoints.md) file.

## Authentication Flow

The application uses JWTs for authentication, with a system of short-lived access tokens and long-lived refresh tokens.

1.  **Login/Registration (`/auth/login`, `/auth/register`)**:
    *   Upon successful authentication or registration, the server returns:
        *   A short-lived JWT **access token** in the JSON response body. This token should be stored by the client (e.g., in memory) and sent in the `Authorization: Bearer <token>` header for subsequent requests to protected endpoints.
        *   A long-lived **refresh token** set in an `HttpOnly`, `Secure` (in production), `SameSite=Lax` cookie. This cookie is automatically handled by the browser and is not accessible to JavaScript.

2.  **Accessing Protected Resources**:
    *   The client includes the JWT access token in the `Authorization` header.
    *   The server validates this token.

3.  **Token Refresh (`/auth/refresh-token`)**:
    *   If an access token expires, the client will receive a 401 Unauthorized status.
    *   The client should then make a `POST` request to `/auth/refresh-token`. No request body is needed; the browser will automatically send the refresh token cookie.
    *   If the refresh token is valid and not expired:
        *   The server issues a new short-lived access token (returned in the JSON response body).
        *   A new refresh token is generated (rolling refresh tokens) and set in a new HttpOnly cookie, invalidating the previous one.
    *   The client then uses the new access token for subsequent requests.

4.  **Logout (`/auth/logout`)**:
    *   The client makes a `POST` request to `/auth/logout`.
    *   The server invalidates the refresh token (deletes it from the database) and sends back an instruction to clear the refresh token cookie (by setting an expired cookie).
    *   The client should also clear its stored access token.

## Docker

The project includes a `Dockerfile` for containerization.

### Building the Docker Image

To build the Docker image, navigate to the project root directory (where the `Dockerfile` is located) and run:
```bash
docker build -t forkmyfolio-backend .
```
Or, to specify a version:
```bash
docker build -t forkmyfolio-backend:0.0.1 .
```

### Running with Docker

To run the application using Docker:

**Development Profile (using H2 in-memory database):**
```bash
docker run -d -p 8080:8080 --name forkmyfolio-backend-dev forkmyfolio-backend
```
*(The default portfolioProfile in the Docker image is `dev`)*

**Production Profile (requires external PostgreSQL and environment variables):**
```bash
docker run -d -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/your-db-name \
  -e SPRING_DATASOURCE_USERNAME=your-db-user \
  -e SPRING_DATASOURCE_PASSWORD=your-db-password \
  -e JWT_SECRET=your-super-strong-base64-encoded-jwt-secret \
  -e APP_CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com \
  --name forkmyfolio-backend-prod \
  forkmyfolio-backend
```
Replace placeholder values for database connection, JWT secret, and CORS origins with your actual production values.

## Environment Variables

The application uses the following environment variables, primarily for the **production portfolioProfile**:

*   `SPRING_PROFILES_ACTIVE`: Set to `prod` to activate production configurations. Defaults to `dev` if not set (especially in Docker).
*   `SPRING_DATASOURCE_URL`: The JDBC URL for your PostgreSQL database.
    *   Example: `jdbc:postgresql://your-db-host:5432/your-db-name`
*   `SPRING_DATASOURCE_USERNAME`: The username for your PostgreSQL database.
*   `SPRING_DATASOURCE_PASSWORD`: The password for your PostgreSQL database.
*   `JWT_SECRET`: A strong, base64-encoded secret key for signing JWT **access tokens**. This **MUST** be overridden from the default for production.
    *   *Security Note*: Generate a cryptographically strong secret.
*   `JWT_ACCESS_TOKEN_EXPIRATION_MS`: Expiration time for JWT access tokens in milliseconds. Default is 1 hour (`3600000`).
*   `JWT_REFRESH_TOKEN_EXPIRATION_MS`: Expiration time for refresh tokens in milliseconds. Default is 7 days (`604800000`).
*   `APP_JWT_REFRESH_COOKIE_NAME`: Name of the HttpOnly cookie used to store the refresh token. Default is `refreshToken`.
*   `APP_COOKIE_SECURE`: Boolean (`true`/`false`) to set the `Secure` flag on cookies. Should be `true` in production (requires HTTPS). Default is `true` for prod portfolioProfile, `false` for dev.
*   `APP_COOKIE_SAMESITE`: `SameSite` attribute for cookies (e.g., `Lax`, `Strict`, `None`). Default is `Lax`.
*   `APP_CORS_ALLOWED_ORIGINS`: Comma-separated list of allowed origins for CORS.
    *   Example for production: `https://www.yourfrontend.com,https://another-frontend.com`
    *   Example for development (if not using default from `application.properties`): `http://localhost:3001,http://localhost:3002`

These variables can be set directly in your deployment environment or using a `.env` file if your deployment method supports it (e.g., Docker Compose).

For the **development portfolioProfile** (when `SPRING_PROFILES_ACTIVE=dev` or not set), the application defaults to an H2 in-memory database and uses default values from `application.properties` (including an insecure JWT secret intended only for development).

## Code Structure

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

## Testing

To run all unit and integration tests, use the following Maven command from the project root:
```bash
mvn test
```
Test reports can be found in the `target/surefire-reports` directory.

## Contributing

Contributions are welcome! Please adhere to the project's coding standards and practices.
*(More details can be added here if this were a public project: fork, branch, PR process)*

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details (if one were present).
*(Assuming Apache 2.0 based on typical Spring Boot project licenses and OpenAPI config)*
