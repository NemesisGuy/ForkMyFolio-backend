# ForkMyFolio API Endpoints (v2.0.0)

This document outlines the RESTful API endpoints for the ForkMyFolio backend, versioned under `/api/v1/`. The API supports a multi-user SaaS platform with role-based access for Guests (unauthenticated), Users (`ROLE_USER`), and Admins (`ROLE_ADMIN`).

## API Response Structure

All responses are wrapped in a standardized JSON structure for predictable frontend handling:

```json
{
  "status": "success", // Values: "success", "fail", "unauthorized", "forbidden", "validation_failed", "error"
  "data": "<T>",      // Payload (type varies by endpoint) or null for errors/204 No Content
  "errors": [         // Array of error details, empty for successful responses
    {
      "field": "fieldName", // Field causing the error (e.g., "email", "general")
      "message": "Descriptive error message"
    }
  ]
}
```

## Accessing API Documentation

Interactive Swagger UI documentation is available at `http://localhost:8080/swagger-ui.html` (or equivalent production URL). Use the "Authorize" button to input a JWT as `Bearer <token>`.

---

## 1. Public Endpoints (No Authentication Required)

These endpoints are accessible without authentication and support public portfolio viewing and authentication flows.

### Authentication

| Endpoint                          | Method | Description                                                                 |
|-----------------------------------|--------|-----------------------------------------------------------------------------|
| `/api/v1/auth/register`           | POST   | Creates a new user account with `ROLE_USER`.                                |
| `/api/v1/auth/login`              | POST   | Authenticates a user, returning a JWT access token and an HttpOnly refresh token cookie. |
| `/api/v1/auth/refresh-token`      | POST   | Issues a new access token using the HttpOnly refresh token cookie.          |
| `/api/v1/auth/logout`             | POST   | Clears the refresh token cookie, logging the user out.                      |

- **POST /api/v1/auth/register**
  - **Request**:
    ```json
    {
      "username": "Jane Doe",
      "email": "jane@example.com",
      "password": "securePassword123"
    }
    ```
  - **Response** (200 OK):
    ```json
    {
      "status": "success",
      "data": {
        "user": {
          "id": 1,
          "slug": "jane-doe",
          "username": "Jane Doe",
          "email": "jane@example.com",
          "roles": ["USER"]
        },
        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
      },
      "errors": []
    }
    ```

- **POST /api/v1/auth/login**
  - **Request**:
    ```json
    {
      "email": "jane@example.com",
      "password": "securePassword123"
    }
    ```
  - **Response** (200 OK):
    ```json
    {
      "status": "success",
      "data": {
        "user": {
          "id": 1,
          "slug": "jane-doe",
          "username": "Jane Doe",
          "email": "jane@example.com",
          "roles": ["USER"]
        },
        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
      },
      "errors": []
    }
    ```
  - **Note**: Sets an HttpOnly, Secure (in production), SameSite=Lax cookie named `refreshToken`.

- **POST /api/v1/auth/refresh-token**
  - **Request**: No body; requires `refreshToken` cookie.
  - **Response** (200 OK):
    ```json
    {
      "status": "success",
      "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
      },
      "errors": []
    }
    ```
  - **Note**: Updates the `refreshToken` cookie.

- **POST /api/v1/auth/logout**
  - **Request**: No body; requires `refreshToken` cookie.
  - **Response** (200 OK):
    ```json
    {
      "status": "success",
      "data": null,
      "errors": []
    }
    ```
  - **Note**: Clears the `refreshToken` cookie.

### Public Portfolios

| Endpoint                                       | Method | Description                                                                 |
|------------------------------------------------|--------|-----------------------------------------------------------------------------|
| `/api/v1/portfolios/{slug}`                    | GET    | Retrieves the public portfolio for a user by their `slug`.                  |
| `/api/v1/portfolios/{slug}/pdf?template={templateName}` | GET    | Downloads a PDF of the user’s portfolio (optional `templateName`, e.g., `modern`, `classic`). |
| `/api/v1/portfolios/{slug}/vcard`              | GET    | Downloads a vCard (.vcf) with the user’s contact information.               |
| `/api/v1/portfolios/{slug}/contact-messages`   | POST   | Submits a contact message to the user.                                      |

- **GET /api/v1/portfolios/{slug}**
  - **Example**: `/api/v1/portfolios/jane-doe`
  - **Response** (200 OK):
    ```json
    {
      "status": "success",
      "data": {
        "user": {
          "slug": "jane-doe",
          "username": "Jane Doe"
        },
        "profile": { "headline": "Software Engineer", "summary": "...", "visible": true },
        "projects": [{ "uuid": "123e4567-e89b-12d3-a456-426614174000", "title": "Project X", "visible": true }],
        "skills": [{ "uuid": "223e4567-e89b-12d3-a456-426614174001", "name": "Java", "visible": true }],
        "experiences": [],
        "qualifications": [],
        "testimonials": []
      },
      "errors": []
    }
    ```

- **POST /api/v1/portfolios/{slug}/contact-messages**
  - **Request**:
    ```json
    {
      "senderName": "John Smith",
      "senderEmail": "john@example.com",
      "message": "Interested in your work!"
    }
    ```
  - **Response** (200 OK):
    ```json
    {
      "status": "success",
      "data": {
        "uuid": "323e4567-e89b-12d3-a456-426614174002",
        "senderName": "John Smith",
        "senderEmail": "john@example.com",
        "message": "Interested in your work!"
      },
      "errors": []
    }
    ```

### Public Settings

| Endpoint         | Method | Description                                    |
|------------------|--------|------------------------------------------------|
| `/api/v1/settings` | GET    | Retrieves public application settings.         |

- **GET /api/v1/settings**
  - **Response** (200 OK):
    ```json
    {
      "status": "success",
      "data": {
        "siteName": "ForkMyFolio",
        "contactEmail": "support@forkmyfolio.com"
      },
      "errors": []
    }
    ```

---

## 2. Authenticated User Endpoints (`/api/v1/me/**`)

These endpoints require a valid JWT with `ROLE_USER` or `ROLE_ADMIN`, included in the `Authorization: Bearer <token>` header. They manage the authenticated user’s data.

### User Account & Profile

| Endpoint                     | Method | Description                                        |
|------------------------------|--------|----------------------------------------------------|
| `/api/v1/me`                 | GET    | Gets the user’s basic account details.             |
| `/api/v1/me`                 | PUT    | Updates the user’s basic account details.          |
| `/api/v1/me/profile`         | GET    | Gets the user’s detailed portfolio profile.        |
| `/api/v1/me/profile`         | PUT    | Updates the user’s detailed portfolio profile.     |
| `/api/v1/me/profile/visibility` | PUT    | Toggles the visibility of the user’s portfolio profile. |

- **PUT /api/v1/me**
  - **Request**:
    ```json
    {
      "username": "Jane Smith",
      "email": "jane.smith@example.com"
    }
    ```
  - **Response** (200 OK):
    ```json
    {
      "status": "success",
      "data": {
        "id": 1,
        "slug": "jane-smith",
        "username": "Jane Smith",
        "email": "jane.smith@example.com",
        "roles": ["USER"]
      },
      "errors": []
    }
    ```

- **PUT /api/v1/me/profile/visibility**
  - **Request**:
    ```json
    { "visible": true }
    ```
  - **Response** (200 OK):
    ```json
    {
      "status": "success",
      "data": { "headline": "Software Engineer", "summary": "...", "visible": true },
      "errors": []
    }
    ```

### Portfolio Content Management

Full CRUD operations for portfolio entities, identified by `{uuid}` (UUID string). All endpoints require `ROLE_USER` or `ROLE_ADMIN`.

#### Projects

| Endpoint                          | Method | Description                          |
|-----------------------------------|--------|--------------------------------------|
| `/api/v1/me/projects`             | GET    | Lists all user’s projects.           |
| `/api/v1/me/projects`             | POST   | Creates a new project.               |
| `/api/v1/me/projects/{uuid}`      | GET    | Gets a specific project.             |
| `/api/v1/me/projects/{uuid}`      | PUT    | Updates a specific project.          |
| `/api/v1/me/projects/{uuid}`      | DELETE | Deletes a specific project.          |
| `/api/v1/me/projects/{uuid}/visibility` | PUT    | Toggles project visibility.          |

- **POST /api/v1/me/projects**
  - **Request**:
    ```json
    {
      "title": "Project X",
      "description": "A web app",
      "visible": true
    }
    ```
  - **Response** (201 Created):
    ```json
    {
      "status": "success",
      "data": {
        "uuid": "123e4567-e89b-12d3-a456-426614174000",
        "title": "Project X",
        "description": "A web app",
        "visible": true
      },
      "errors": []
    }
    ```

- **PUT /api/v1/me/projects/{uuid}/visibility**
  - **Request**:
    ```json
    { "visible": false }
    ```
  - **Response** (200 OK):
    ```json
    {
      "status": "success",
      "data": {
        "uuid": "123e4567-e89b-12d3-a456-426614174000",
        "title": "Project X",
        "visible": false
      },
      "errors": []
    }
    ```

#### Skills, Experiences, Qualifications, Testimonials

Similar CRUD and visibility toggle endpoints exist for:
- **Skills**: `/api/v1/me/skills`, `/api/v1/me/skills/{uuid}`, `/api/v1/me/skills/{uuid}/visibility`
- **Experiences**: `/api/v1/me/experiences`, `/api/v1/me/experiences/{uuid}`, `/api/v1/me/experiences/{uuid}/visibility`
- **Qualifications**: `/api/v1/me/qualifications`, `/api/v1/me/qualifications/{uuid}`, `/api/v1/me/qualifications/{uuid}/visibility`
- **Testimonials**: `/api/v1/me/testimonials`, `/api/v1/me/testimonials/{uuid}`, `/api/v1/me/testimonials/{uuid}/visibility`

### Contact Messages

| Endpoint                           | Method | Description                                    |
|------------------------------------|--------|------------------------------------------------|
| `/api/v1/me/contact-messages`      | GET    | Lists all contact messages received by the user. |
| `/api/v1/me/contact-messages/{uuid}` | DELETE | Deletes a specific contact message.             |

### Backup & Restore

| Endpoint                      | Method | Description                                      |
|-------------------------------|--------|--------------------------------------------------|
| `/api/v1/me/backup`           | GET    | Downloads a JSON backup of the user’s portfolio data. |
| `/api/v1/me/backup/restore`   | POST   | Restores portfolio data from a JSON backup.      |

---

## 3. Admin Endpoints (`/api/v1/admin/**`)

These endpoints require a valid JWT with `ROLE_ADMIN`, included in the `Authorization: Bearer <token>` header.

### User Management

| Endpoint                           | Method | Description                                    |
|------------------------------------|--------|------------------------------------------------|
| `/api/v1/admin/users`             | GET    | Lists all users (paginated).                   |
| `/api/v1/admin/users/{userId}`    | GET    | Gets details for a user by numeric ID.         |
| `/api/v1/admin/users/{userId}`    | PUT    | Updates a user’s details (e.g., role, active status). |
| `/api/v1/admin/users/{userId}`    | DELETE | Deactivates (soft deletes) a user account.     |
| `/api/v1/admin/users/{userId}/reset-password` | POST | Resets a user’s password.                     |

- **GET /api/v1/admin/users**
  - **Response** (200 OK):
    ```json
    {
      "status": "success",
      "data": [
        {
          "id": 1,
          "slug": "jane-doe",
          "username": "Jane Doe",
          "email": "jane@example.com",
          "roles": ["USER"],
          "active": true
        }
      ],
      "errors": []
    }
    ```

### Site-wide Management

| Endpoint                              | Method | Description                                    |
|---------------------------------------|--------|------------------------------------------------|
| `/api/v1/admin/settings`             | GET    | Lists all application settings.                |
| `/api/v1/admin/settings/{uuid}`      | PUT    | Updates a specific application setting.        |
| `/api/v1/admin/contact-messages`      | GET    | Lists all contact messages (paginated).        |
| `/api/v1/admin/contact-messages/{uuid}` | DELETE | Deletes any contact message.                   |
| `/api/v1/admin/stats`                | GET    | Retrieves site-wide visitor statistics.        |

---

## Notes

- **Authentication**: All `/api/v1/me/**` and `/api/v1/admin/**` endpoints require a JWT in the `Authorization: Bearer <token>` header.
- **Identifiers**: Public-facing resources use UUIDs (`{uuid}`) to hide internal database IDs. Admin endpoints use numeric `{userId}` for user management.
- **Security**: HttpOnly, Secure (in production), SameSite=Lax cookies are used for refresh tokens. Admin endpoints are rate-limited using Bucket4j.
- **Swagger UI**: Access detailed endpoint documentation and test endpoints at `http://localhost:8080/swagger-ui.html`.