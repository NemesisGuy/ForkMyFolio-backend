# ForkMyFolio Technical Documentation

This document provides a detailed overview of the ForkMyFolio backend, including setup instructions, API documentation, and architectural standards.

---

## Getting Started

This section will guide you through setting up the project locally.

### Prerequisites

*   Java JDK 21 or later (e.g., OpenJDK, Oracle JDK)
*   Apache Maven 3.6.x or later
*   Git
*   (Optional for Production profile) PostgreSQL server running
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

---

## Running the Application

The application can be run using different Spring profiles.

### Development Profile (H2 Database)

This is the default profile. It uses an in-memory H2 database.
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

This profile requires a running PostgreSQL instance and proper configuration (see [Environment Variables](#environment-variables)).
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```
Or, after building the JAR:
```bash
java -jar -Dspring.profiles.active=prod target/forkmyfolio-backend-0.0.1-SNAPSHOT.jar
```
---

## API Documentation

For the most up-to-date and interactive API documentation, please use the Swagger UI, which is available after running the application.

### Swagger UI (Interactive)
Once the application is running, the interactive Swagger UI documentation can be accessed at:
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

The OpenAPI specification (JSON) is available at:
[http://localhost:8080/api-docs](http://localhost:8080/api-docs)

You can use the "Authorize" button on Swagger UI (top right) to authenticate using a JWT access token obtained from the `/auth/login` or `/auth/register` endpoints. The format is `Bearer <your_jwt_token>`.

### API Endpoints Summary

This section provides a high-level overview of the available API endpoints.

#### 1. Public Endpoints

Accessible to anyone without authentication.

##### Authentication

| Method | Endpoint                     | Description                                |
| :----- | :--------------------------- | :----------------------------------------- |
| `POST` | `/api/v1/auth/register`      | Creates a new user account.                |
| `POST` | `/api/v1/auth/login`         | Authenticates a user and returns a token.  |
| `POST` | `/api/v1/auth/refresh-token` | Issues a new access token.                 |
| `POST` | `/api/v1/auth/logout`        | Logs the current user out.                 |

##### Public Portfolios

| Method | Endpoint                                   | Description                                        |
| :----- | :----------------------------------------- | :------------------------------------------------- |
| `GET`    | `/api/v1/portfolios/{slug}`                | Retrieves a user's public portfolio shell.  |
| `GET`    | `/api/v1/portfolios/{slug}/projects`       | Retrieves a user's public projects. |
| `GET`    | `/api/v1/portfolios/{slug}/experience`     | Retrieves a user's public experience.      |
| `GET`    | `/api/v1/portfolios/{slug}/qualifications` | Retrieves a user's public qualifications.     |
| `GET`    | `/api/v1/portfolios/{slug}/skills`         | Retrieves a user's public skills.        |
| `GET`    | `/api/v1/portfolios/{slug}/testimonials`   | Retrieves a user's public testimonials.        |
| `POST`   | `/api/v1/portfolios/{slug}/contact-messages` | Submits a contact message to a specific user.      |

##### Portfolio Downloads

| Method | Endpoint                                   | Description                                        |
| :----- | :----------------------------------------- | :------------------------------------------------- |
| `GET`    | `/api/v1/portfolios/{slug}/pdf`            | Downloads a PDF version of a user's portfolio.     |
| `GET`    | `/api/v1/portfolios/{slug}/markdown`       | Downloads a Markdown version of a user's portfolio. |
| `GET`    | `/api/v1/portfolios/{slug}/vcard`          | Downloads a vCard of a user's contact info.        |

##### Public Settings & Policies

| Method | Endpoint                                   | Description                                        |
| :----- | :----------------------------------------- | :------------------------------------------------- |
| `GET`    | `/api/v1/settings`                         | Retrieves public, system-wide application settings. |
| `GET`    | `/api/v1/settings/pdf-templates`           | Retrieves the list of available PDF templates.     |
| `GET`    | `/api/v1/policies/terms-of-service`        | Retrieves the Terms of Service.                    |
| `GET`    | `/api/v1/policies/privacy-policy`          | Retrieves the Privacy Policy.                      |

#### 2. Authenticated User Endpoints

Requires a valid user token.

##### Account & Profile (`/api/v1/me`)

| Method | Endpoint      | Description                               |
| :----- | :------------ | :---------------------------------------- |
| `GET`    | `/`           | Gets the authenticated user's account details. |
| `PUT`    | `/`           | Updates the authenticated user's account details. |
| `POST`   | `/password`   | Changes the authenticated user's password. |
| `POST`   | `/accept-terms` | Records that the user has accepted the terms. |
| `GET`    | `/profile`    | Gets the user's detailed portfolio profile. |
| `PUT`    | `/profile`    | Updates the user's detailed portfolio profile. |
| `PUT`    | `/profile/visibility` | Updates the public visibility of the profile. |

##### Portfolio Content Management (`/api/v1/me/{content-type}`)

| Method           | Endpoint            | Description                               |
| :--------------- | :------------------ | :---------------------------------------- |
| `GET`/`POST`     | `/projects`         | List all or create a new project.         |
| `GET`/`PUT`/`DELETE` | `/projects/{uuid}`  | Manage a specific project.                |
| `GET`/`POST`     | `/skills`           | List all or create a new skill relationship. |
| `GET`/`PUT`/`DELETE` | `/skills/{uuid}`    | Manage a specific skill relationship.     |
| `GET`/`POST`     | `/experiences`      | List all or create a new experience.      |
| `GET`/`PUT`/`DELETE` | `/experiences/{uuid}` | Manage a specific experience.             |
| `GET`/`POST`     | `/qualifications`   | List all or create a new qualification.   |
| `GET`/`PUT`/`DELETE` | `/qualifications/{uuid}` | Manage a specific qualification.          |
| `GET`/`POST`     | `/testimonials`     | List all or create a new testimonial.     |
| `GET`/`PUT`/`DELETE` | `/testimonials/{uuid}` | Manage a specific testimonial.            |

##### User-Specific Management

| Method   | Endpoint                 | Description                                        |
| :------- | :----------------------- | :------------------------------------------------- |
| `GET`/`PUT`  | `/settings`              | Retrieve or update personal portfolio display settings. |
| `GET`      | `/contact-messages`      | Lists all contact messages received by the user.   |
| `GET`      | `/contact-messages/unread-count` | Gets the count of unread messages. |
| `PUT`      | `/contact-messages/{uuid}` | Updates a contact message (e.g., mark as read). |
| `DELETE`   | `/contact-messages/{uuid}` | Deletes a specific contact message.                |
| `GET`      | `/backup`                | Downloads a JSON backup of the user's portfolio.   |
| `POST`     | `/backup/restore`        | Restores portfolio data from a JSON backup.        |

##### Global Skills (`/api/v1/skills`)

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/` | Get all available platform skills. |

#### 3. Admin Endpoints

Requires a valid admin token. All endpoints are prefixed with `/api/v1/admin`.

##### User Management

| Method   | Endpoint           | Description                                          |
| :------- | :----------------- | :--------------------------------------------------- |
| `GET`      | `/users`           | Lists all users in the system (paginated).           |
| `POST`     | `/users`           | Creates a new user.                                  |
| `GET`      | `/users/{userId}`  | Gets details for a single user by their ID.          |
| `PUT`      | `/users/{userId}`  | Updates a user's details, roles, and active status.  |
| `DELETE`   | `/users/{userId}`  | Deactivates (soft deletes) a user account.           |

##### Site-wide Management

| Method   | Endpoint                 | Description                                        |
| :------- | :----------------------- | :------------------------------------------------- |
| `GET`/`PUT`  | `/settings`              | Retrieve or bulk-update global application settings. |
| `GET`      | `/contact-messages`      | Lists all contact messages from all users.         |
| `DELETE`   | `/contact-messages/{uuid}` | Deletes any contact message from the system.       |
| `GET`      | `/stats`                 | Retrieves site-wide visitor statistics.            |

##### Backup & Restore

| Method   | Endpoint                 | Description                                        |
| :------- | :----------------------- | :------------------------------------------------- |
| `GET`      | `/backup`                | Downloads a full system backup.                    |
| `POST`     | `/backup/restore/system` | Restores the entire system from a backup.          |
| `POST`     | `/backup/restore/user/{userUuid}` | Restores a single user from a backup.      |
| `DELETE`   | `/backup/wipe`           | Wipes all data from the system.                    |

---

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

---

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
*(The default profile in the Docker image is `dev`)*

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

---

## Environment Variables

The application uses the following environment variables, primarily for the **production profile**:

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
*   `APP_COOKIE_SECURE`: Boolean (`true`/`false`) to set the `Secure` flag on cookies. Should be `true` in production (requires HTTPS). Default is `true` for prod profile, `false` for dev.
*   `APP_COOKIE_SAMESITE`: `SameSite` attribute for cookies (e.g., `Lax`, `Strict`, `None`). Default is `Lax`.
*   `APP_CORS_ALLOWED_ORIGINS`: Comma-separated list of allowed origins for CORS.
    *   Example for production: `https://www.yourfrontend.com,https://another-frontend.com`
    *   Example for development (if not using default from `application.properties`): `http://localhost:3001,http://localhost:3002`

These variables can be set directly in your deployment environment or using a `.env` file if your deployment method supports it (e.g., Docker Compose).

For the **development profile** (when `SPRING_PROFILES_ACTIVE=dev` or not set), the application defaults to an H2 in-memory database and uses default values from `application.properties` (including an insecure JWT secret intended only for development).

---

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

---

## Testing

### Running Tests

To run all unit and integration tests, use the following Maven command from the project root:
```bash
mvn test
```
For more detailed information on our testing strategy, best practices, and configurations (like JSON serialization for dates), please refer to our dedicated [**Testing Guide**](../../docs/TESTING.md).

Test reports can be found in the `target/surefire-reports` directory.

---

# ForkMyFolio Backend Architecture & Coding Standards

## Overview

This document describes the backend architecture, design decisions, and coding standards for the ForkMyFolio project. The aim is to maintain consistency, ensure code quality, and facilitate collaboration among developers.

---

## User Roles and Permissions

- **Visitor:** Unauthenticated users. Can only view public portfolio data.
- **User:** Authenticated users who can manage and backup their own profile data.
- **Admin:** Authenticated with elevated permissions. Can manage their own profile plus backup and restore other users’ data, including bulk operations for migrations and server moves.

---

## Project Architecture

- The backend uses **Spring Boot** with a layered architecture:
   - **Controller Layer:** REST API endpoints exposing business functionality.
   - **Service Layer:** Business logic and data enrichment.
   - **Repository Layer:** Direct data access via Spring Data JPA.
- All API responses are wrapped in a generic response format to standardize success/error handling.
- **Entities** map to database tables using JPA annotations.
- DTOs (Data Transfer Objects) are used to expose only necessary data to clients.
- Mapping between Entities and DTOs is done via **manual mappers** to keep explicit control.

---

## Manual DTO Mappers

- All entity-to-DTO and DTO-to-entity conversions are done manually (no MapStruct or other tools).
- This allows precise control over fields exposed, filtering logic, and context-based enrichment.
- Mappers reside in `com.forkmyfolio.mapper` package.
- Each mapper has methods like `toDto(Entity entity)` and `toEntity(Dto dto)`.
- When an entity has nested relations (e.g., Experience → Skill + UserSkill enrichment), the mapper receives contextual data (like a skill proficiency map) for richer DTOs.

---

## Controller Layer

- Controllers are split into multiple focused classes, grouped by resource (e.g., `PortfolioExperienceController`, `PortfolioQualificationsController`).
- Each controller uses path variables (e.g., `{slug}`) to identify user portfolios.
- Controllers only orchestrate service calls and return DTOs wrapped in standard response wrappers.
- File downloads (PDF, Markdown, vCard) skip JSON response wrapping.
- API base path uses `/api/v1`.
- Controllers use Swagger annotations (`@Operation`, `@Tag`) for auto-generating API docs.
- Access control is enforced by Spring Security roles and JWT tokens.

---

## Service Layer

- Services contain all business logic and data enrichment.
- They interact with repositories and perform transformations like skill proficiency enrichment on Experience and Project entities.
- Services also handle backup and restore operations with fine-grained control depending on user role.
- Public portfolio retrieval aggregates data into composite DTOs.
- Backup services handle:
   - **User backup:** Single user backing up their profile data.
   - **Admin backup:** Backup of single or all user profiles.
   - **User restore:** Restoring a single user’s profile.
   - **Admin restore:** Bulk restore after migrations or server changes.

---

## Data Model and Entity Design

- Entities use UUIDs as external identifiers, with internal primary keys for efficiency.
- Key relationships:
   - `User` has many `Experiences`, `Projects`, `Qualifications`, `UserSkills`, `Testimonials`.
   - `Experience` links to many global `Skills`.
   - User skill proficiency is kept in `UserSkill`, separate from `Skill`.
   - `Experience` references `Skill` entities, not `UserSkill`, to maintain clean data boundaries.
- The service layer enriches `Experience` and `Project` DTOs with user skill proficiency info during mapping.
- Visibility flags control what is exposed publicly.

---

## Backup and Restore Strategy

| Backup Type      | Who Can Perform    | What is Backed Up                   | Use Case                               |
|------------------|--------------------|-----------------------------------|--------------------------------------|
| Visitor          | N/A                | N/A                               | Not applicable                       |
| User Backup      | Authenticated User | Own profile data only              | User backing up their portfolio data |
| Admin Backup     | Admin              | All users’ profiles or self only  | Full system backup or admin self-use |
| User Restore     | Authenticated User | Restore own profile                | Restore own data after loss          |
| Admin Restore    | Admin              | Restore any/all users’ profiles   | Bulk restore after migration/server |

- Backup and restore operations are exposed via secure endpoints, with strict role validation.
- Restore operations validate input data and maintain data integrity.
- Backup data includes all necessary related entities to fully reconstruct a user profile.

---

## Swagger (OpenAPI) Documentation

- Swagger docs are auto-generated from controllers using `springdoc-openapi`.
- API docs are exposed at `/api/v1/docs`.
- All endpoints have `@Operation` annotations with summaries and descriptions.
- Models have proper schema descriptions.
- File download endpoints use `@SkipApiResponseWrapper` to avoid wrapping binary data.
- Swagger UI is accessible for easy manual API exploration.

---

## JavaDoc and Code Comments

- All public classes and methods contain detailed JavaDoc comments.
- Comments describe method purpose, parameters, return values, exceptions thrown.
- Complex logic inside services or mappers is also documented inline.
- The goal is maintainability and easing onboarding of new developers.

---

## Error Handling

- Custom exceptions like `ResourceNotFoundException` are used for domain errors.
- Global exception handlers convert exceptions into consistent API error responses.
- Validation errors produce detailed messages via Spring Validation annotations.

---

## Testing

- Unit tests cover service logic and manual mappers.
- Integration tests cover REST endpoints with mock data.
- Tests verify role-based access, error cases, and response formats.

---

## Coding Conventions

- Follow standard Java conventions (camelCase, PascalCase for classes).
- Use Lombok to reduce boilerplate (`@Getter`, `@Setter`, `@NoArgsConstructor`).
- Use explicit imports, no wildcard imports.
- DTOs live separately from entities, usually in `dto.response` or `dto.request`.
- Service interfaces followed by implementations in `service.impl`.
- Controller classes kept thin and focused on request/response orchestration.
- Use consistent naming for REST endpoints (plural resource names, nested paths).
