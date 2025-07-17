# API Documentation

This document provides a comprehensive overview of the ForkMyFolio API, including its structure, endpoints, and authentication flow.

## Table of Contents

- [API Response Structure](#api-response-structure)
- [Swagger UI (Interactive Documentation)](#swagger-ui-interactive-documentation)
- [API Endpoints](#api-endpoints)
  - [Public Endpoints](#public-endpoints)
  - [Authentication Endpoints](#authentication-endpoints)
  - [Admin Endpoints](#admin-endpoints)
- [Authentication Flow](#authentication-flow)

---

## API Response Structure

All API responses (both for successful requests and errors) are wrapped in a standardized JSON structure:

```json
{
  "status": "success", // Possible values: "success", "fail", "unauthorized", "forbidden", "validation_failed", "error"
  "data": "<T>",       // The actual data payload for the request, or null if not applicable. The type 'T' varies per endpoint.
  "errors": [          // An array of error details, typically empty for successful responses.
    {
      "field": "fieldName", // Name of the field that caused the error. Can be "general" for non-field-specific errors.
      "message": "A descriptive error message."
    }
  ]
}
```

---

## Swagger UI (Interactive Documentation)

Once the application is running, the interactive Swagger UI documentation can be accessed at:
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

You can use the "Authorize" button on Swagger UI to authenticate using a JWT access token obtained from the `/auth/login` endpoint. The format is `Bearer <your_jwt_token>`.

---

## API Endpoints

This section provides an overview of the available API endpoints for the ForkMyFolio application.

### Public Endpoints

These endpoints are open and do not require authentication. They are used to populate the public-facing portfolio website.

#### Portfolio & Content
-   **Get Public Profile Info**: `GET /api/v1/portfolio-profile`
-   **Get All Projects**: `GET /api/v1/projects`
-   **Get All Skills**: `GET /api/v1/skills`
-   **Get All Experience**: `GET /api/v1/experience`
-   **Get All Testimonials**: `GET /api/v1/testimonials`
-   **Get All Qualifications**: `GET /api/v1/qualifications`

#### Submit Contact Message
-   **Endpoint**: `POST /api/v1/contact-messages`
-   **Purpose**: To submit a message from the public contact form.
-   **Request Body**:
    ```json
    {
      "name": "Jane Doe",
      "email": "jane.doe@example.com",
      "message": "This is a test message."
    }
    ```

#### Get Public Settings
-   **Endpoint**: `GET /api/v1/settings`
-   **Purpose**: To get a simple map of all public feature flags. This is ideal for quickly checking which UI sections to display on the public site.
-   **Response Body**:
    ```json
    {
      "SHOW_PROJECTS": true,
      "SHOW_SKILLS": true,
      "SHOW_EXPERIENCE": false,
      "SHOW_TESTIMONIALS": true,
      "SHOW_QUALIFICATIONS": true,
      "SHOW_CONTACT_FORM": true
    }
    ```

### Authentication Endpoints

-   **Login, Refresh, etc.**: `POST /api/v1/auth/**`
    -   Handles user authentication and token management.

### Admin Endpoints

All admin endpoints require a valid JWT `Bearer` token in the `Authorization` header and are prefixed with `/api/v1/admin`.

#### Content Management (CRUD)
-   **Projects**: `GET, POST, PUT, DELETE /api/v1/admin/projects/{uuid}`
-   **Skills**: `GET, POST, PUT, DELETE /api/v1/admin/skills/{uuid}`
-   **Experience**: `GET, POST, PUT, DELETE /api/v1/admin/experience/{uuid}`
-   **Testimonials**: `GET, POST, PUT, DELETE /api/v1/admin/testimonials/{uuid}`
-   **Qualifications**: `GET, POST, PUT, DELETE /api/v1/admin/qualifications/{uuid}`

#### Contact Message Management
-   **Get All Messages**: `GET /api/v1/admin/contact-messages`
-   **Delete a Message**: `DELETE /api/v1/admin/contact-messages/{uuid}`

#### Application Settings Management
-   **Get All Settings Details**: `GET /api/v1/admin/settings`
-   **Update Settings (Bulk)**: `PUT /api/v1/admin/settings`

---

## Authentication Flow

The application uses JWTs for authentication, with a system of short-lived access tokens and long-lived refresh tokens. This provides a balance of performance and security.

1.  **Login/Registration (`/auth/login`, `/auth/register`)**:
    *   Upon successful authentication or registration, the server returns two tokens in two different places:
        *   **Access Token**: A short-lived JWT is sent in the JSON response body (`data.accessToken`). This should be stored by the client (e.g., in memory) and sent in the `Authorization: Bearer <token>` header for all future requests to protected endpoints.
        *   **Refresh Token**: A long-lived token is sent in an `HttpOnly`, `Secure`, `SameSite=Lax` cookie. The browser stores this automatically and sends it on requests to the refresh endpoint. It is inaccessible to client-side JavaScript, which is a key security feature.

2.  **Accessing Protected Resources**:
    *   The client includes the JWT access token in the `Authorization: Bearer <token>` header.
    *   The server validates this token on every request.

3.  **Token Refresh (`/auth/refresh-token`)**:
    *   If an access token expires, the client will receive a 401 Unauthorized status.
    *   The client should then make a `POST` request to `/auth/refresh-token`. No request body is needed; the browser will automatically send the refresh token cookie.
    *   If the refresh token is valid:
        *   The server issues a **new access token** in the JSON response body.
        *   It also issues a **new refresh token** in a new `HttpOnly` cookie to replace the old one (this is called "rolling refresh tokens").
    *   The client can now use the new access token to retry the original failed request.

4.  **Logout (`/auth/logout`)**:
    *   The client makes a `POST` request to `/auth/logout`.
    *   The server invalidates the refresh token (by deleting it from the database) and sends back a response header to clear the refresh token cookie.
    *   The client should also clear its stored access token on its end.