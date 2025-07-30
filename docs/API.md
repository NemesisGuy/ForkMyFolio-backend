# ForkMyFolio API Documentation

This document provides a comprehensive overview of the ForkMyFolio REST API, including the standardized response structure, authentication mechanisms, and all available endpoints.

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

## Interactive Documentation (Swagger UI)

Once the application is running, the interactive Swagger UI documentation can be accessed at:
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

You can use the "Authorize" button on Swagger UI to authenticate using a JWT access token obtained from the `/auth/login` endpoint. The format is `Bearer <your_jwt_token>`.

## Authentication

The API uses JSON Web Tokens (JWTs) for authentication. The flow is as follows:

1.  **Login/Register:** The user authenticates via `POST /api/v1/auth/login` or `POST /api/v1/auth/register`. The server responds with a short-lived `accessToken` in the response body and a secure, `HttpOnly` `refreshToken` cookie.
2.  **Authenticated Requests:** The `accessToken` must be included in the `Authorization` header for all protected endpoints (e.g., `/api/v1/me/**`).
3.  **Token Refresh:** If the `accessToken` expires (resulting in a `401 Unauthorized` response), the client should use the `refreshToken` cookie to request a new `accessToken` from `POST /api/v1/auth/refresh-token`.
4.  **Logout:** `POST /api/v1/auth/logout` clears the `refreshToken` cookie.

---

## API Endpoints

The API is versioned under `/api/v1/` and is divided into three main sections: Public, Authenticated User, and Admin.

### 1. Public Endpoints (No Authentication Required)

#### Authentication

| Endpoint                          | Method | Description                                                                 |
|-----------------------------------|--------|-----------------------------------------------------------------------------|
| `/api/v1/auth/register`           | POST   | Creates a new user account with `ROLE_USER`.                                |
| `/api/v1/auth/login`              | POST   | Authenticates a user, returning a JWT access token and an HttpOnly refresh token cookie. |
| `/api/v1/auth/refresh-token`      | POST   | Issues a new access token using the HttpOnly refresh token cookie.          |
| `/api/v1/auth/logout`             | POST   | Clears the refresh token cookie, logging the user out.                      |

#### Public Portfolios

| Endpoint                                       | Method | Description                                                                 |
|------------------------------------------------|--------|-----------------------------------------------------------------------------|
| `/api/v1/portfolios/{slug}`                    | GET    | Retrieves the public portfolio for a user by their `slug`.                  |
| `/api/v1/portfolios/{slug}/pdf?template={templateName}` | GET    | Downloads a PDF of the user’s portfolio (optional `templateName`, e.g., `modern`, `classic`). |
| `/api/v1/portfolios/{slug}/markdown`           | GET    | Downloads a Markdown version of the user's portfolio.                      |
| `/api/v1/portfolios/{slug}/vcard`              | GET    | Downloads a vCard (.vcf) with the user’s contact information.               |
| `/api/v1/portfolios/{slug}/contact-messages`   | POST   | Submits a contact message to the user.                                      |


#### Public Settings

| Endpoint         | Method | Description                                    |
|------------------|--------|------------------------------------------------|
| `/api/v1/settings` | GET    | Retrieves public application settings.         |
| `/api/v1/settings/pdf-templates`  | GET    | Get list of available PDF template names |

---

### 2. Authenticated User Endpoints (`/api/v1/me/**`)

These endpoints require a valid JWT with `ROLE_USER` or `ROLE_ADMIN`.

#### User Account & Profile

| Endpoint                     | Method | Description                                        |
|------------------------------|--------|----------------------------------------------------|
| `/api/v1/me`                 | GET    | Gets the user’s basic account details.             |
| `/api/v1/me`                 | PUT    | Updates the user’s basic account details.          |
| `/api/v1/me/profile`         | GET    | Gets the user’s detailed portfolio profile.        |
| `/api/v1/me/profile`         | PUT    | Updates the user’s detailed portfolio profile.     |
| `/api/v1/me/profile/visibility` | PUT    | Toggles the visibility of the user’s portfolio profile. |

#### Portfolio Content Management (CRUD)

Full CRUD operations for portfolio entities, identified by `{uuid}`.

| Entity         | Endpoints                                                               |
|----------------|-------------------------------------------------------------------------|
| **Projects**   | `/api/v1/me/projects`, `/api/v1/me/projects/{uuid}`, `/api/v1/me/projects/{uuid}/visibility` |
| **Skills**     | `/api/v1/me/skills`, `/api/v1/me/skills/{uuid}`, `/api/v1/me/skills/{uuid}/visibility` |
| **Experiences**| `/api/v1/me/experiences`, `/api/v1/me/experiences/{uuid}`, `/api/v1/me/experiences/{uuid}/visibility` |
| **Qualifications**| `/api/v1/me/qualifications`, `/api/v1/me/qualifications/{uuid}`, `/api/v1/me/qualifications/{uuid}/visibility` |
| **Testimonials**| `/api/v1/me/testimonials`, `/api/v1/me/testimonials/{uuid}`, `/api/v1/me/testimonials/{uuid}/visibility` |

#### Contact Messages

| Endpoint                           | Method | Description                                    |
|------------------------------------|--------|------------------------------------------------|
| `/api/v1/me/contact-messages`      | GET    | Lists all contact messages received by the user. |
| `/api/v1/me/contact-messages/{uuid}` | DELETE | Deletes a specific contact message.             |

#### Backup & Restore

| Endpoint                      | Method | Description                                      |
|-------------------------------|--------|--------------------------------------------------|
| `/api/v1/me/backup`           | GET    | Downloads a JSON backup of the user’s portfolio data. |
| `/api/v1/me/backup/restore`   | POST   | Restores portfolio data from a JSON backup.      |

---

### 3. Admin Endpoints (`/api/v1/admin/**`)

These endpoints require a valid JWT with `ROLE_ADMIN`.

#### User Management

| Endpoint                           | Method | Description                                    |
|------------------------------------|--------|------------------------------------------------|
| `/api/v1/admin/users`             | GET    | Lists all users (paginated).                   |
| `/api/v1/admin/users/{userId}`    | GET    | Gets details for a user by numeric ID.         |
| `/api/v1/admin/users/{userId}`    | PUT    | Updates a user’s details (e.g., role, active status). |
| `/api/v1/admin/users/{userId}`    | DELETE | Deactivates (soft deletes) a user account.     |
| `/api/v1/admin/users/{userId}/reset-password` | POST | Resets a user’s password.                     |

#### Site-wide Management

| Endpoint                              | Method | Description                                    |
|---------------------------------------|--------|------------------------------------------------|
| `/api/v1/admin/settings`             | GET    | Lists all application settings.                |
| `/api/v1/admin/settings/{uuid}`      | PUT    | Updates a specific application setting.        |
| `/api/v1/admin/contact-messages`      | GET    | Lists all contact messages (paginated).        |
| `/api/v1/admin/contact-messages/{uuid}` | DELETE | Deletes any contact message.                   |
| `/api/v1/admin/stats`                | GET    | Retrieves site-wide visitor statistics.        |

---

## API Changes and Frontend Migration (v2)

The v2 API introduced significant changes to support a multi-user architecture.

*   **Multi-User Support:** The API now supports multiple users, each with a unique portfolio accessible via a `slug` (e.g., `/api/v1/portfolios/jane-doe`).
*   **New API Structure:** Endpoints are now organized under `/api/v1/` and segmented into `public`, `me` (for authenticated users), and `admin`.
*   **UUIDs for Resources:** All public-facing resources now use UUIDs instead of numeric IDs.
*   **Visibility Controls:** Users can now toggle the visibility of individual portfolio sections.
*   **Enhanced Data Models:**
    *   **User:** Added `emailVerified` (boolean).
    *   **Qualification:** Added `institutionLogoUrl`, `institutionWebsite`, `fieldOfStudy`, `level`, `startYear`, `stillStudying`, and `credentialUrl`.
*   **Visitor Analytics:** A new AOP-based system tracks visitor interactions (e.g., PDF downloads) using the `@TrackVisitor` annotation.

Frontend developers should consult the detailed migration guide for instructions on updating their applications to work with the v2 API. Key actions include updating API base paths, handling JWT authentication, using UUIDs, and implementing visibility toggles.
