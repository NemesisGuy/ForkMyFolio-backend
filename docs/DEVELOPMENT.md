# Local Development Guide

This document provides instructions for setting up, running, and configuring the project for local development.

## Getting Started

This section will guide you through setting up the project locally.

### Prerequisites

*   Java JDK 21 or later (e.g., OpenJDK, Oracle JDK)
*   Apache Maven 3.6.x or later
*   Git
*   (Optional for Production portfolioProfile) PostgreSQL server running

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