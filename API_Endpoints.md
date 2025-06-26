# ForkMyFolio API Endpoints

Version: 0.0.1-SNAPSHOT

## Base URL

All API endpoints are prefixed with `/api/v1`.

## General Response Structure

All responses from the API (both success and error) are wrapped in a standard structure:

```json
{
  "status": "success", // Can be "success", "unauthorized", "forbidden", "error", "validation_failed", etc.
  "data": "<T>",         // The actual data payload (type varies by endpoint) or a message object. Can be null for errors or some success cases.
  "errors": [          // Array of error details, typically empty for successful responses.
    {
      "field": "fieldName", // Specific field causing the error (for validation errors) or "general", "authentication", etc.
      "message": "Descriptive error message"
    }
  ]
}
```
The type `T` for the `data` field will be specified for each endpoint below.

---

## Authentication Endpoints

Base Path: `/api/v1/auth`

These endpoints are generally public, except where noted.

### 1. Register User
*   **Method:** `POST`
*   **Path:** `/api/v1/auth/register`
*   **Description:** Registers a new user in the system.
*   **Authentication Required:** No
*   **Success Status Code:** `201 CREATED`
*   **`ApiResponseWrapper.data` Type:** `AuthResponse` (contains `accessToken` (String) and `user` (UserDto object))
*   **Cookies:** Sets an HttpOnly refresh token cookie (`refreshToken`) scoped to `/api/v1/auth`.

### 2. Login User
*   **Method:** `POST`
*   **Path:** `/api/v1/auth/login`
*   **Description:** Authenticates an existing user.
*   **Authentication Required:** No
*   **Success Status Code:** `200 OK`
*   **`ApiResponseWrapper.data` Type:** `AuthResponse` (contains `accessToken` (String) and `user` (UserDto object))
*   **Cookies:** Sets an HttpOnly refresh token cookie (`refreshToken`) scoped to `/api/v1/auth`.

### 3. Refresh Access Token
*   **Method:** `POST`
*   **Path:** `/api/v1/auth/refresh-token`
*   **Description:** Issues a new JWT access token using the refresh token (which must be sent as an HttpOnly cookie). Implements a rolling refresh token strategy.
*   **Authentication Required:** Relies on the `refreshToken` HttpOnly cookie.
*   **Success Status Code:** `200 OK`
*   **`ApiResponseWrapper.data` Type:** `AuthResponse` (contains new `accessToken` (String) and `user` (UserDto object))
*   **Cookies:** Sets a new HttpOnly refresh token cookie (`refreshToken`).

### 4. Logout User
*   **Method:** `POST`
*   **Path:** `/api/v1/auth/logout`
*   **Description:** Logs out the current user by invalidating the server-side refresh token and clearing the client-side refresh token cookie.
*   **Authentication Required:** Yes (JWT Bearer token in Authorization header)
*   **Success Status Code:** `200 OK`
*   **`ApiResponseWrapper.data` Type:** `Map<String, String>` (e.g., `{"message": "User logged out successfully."}`)
*   **Cookies:** Clears the `refreshToken` cookie.

---

## User Endpoints

Base Path: `/api/v1/users`

### 1. Get Current User Profile
*   **Method:** `GET`
*   **Path:** `/api/v1/users/me/profile`
*   **Description:** Fetches the profile information for the currently authenticated user (excludes password).
*   **Authentication Required:** Yes (JWT Bearer token)
*   **Success Status Code:** `200 OK`
*   **`ApiResponseWrapper.data` Type:** `UserDto`

---

## Project Endpoints

Base Path: `/api/v1/projects`

### 1. Get All Projects
*   **Method:** `GET`
*   **Path:** `/api/v1/projects`
*   **Description:** Retrieves a list of all projects.
*   **Authentication Required:** No
*   **Success Status Code:** `200 OK`
*   **`ApiResponseWrapper.data` Type:** `List<ProjectDto>`

### 2. Get Project by ID
*   **Method:** `GET`
*   **Path:** `/api/v1/projects/{id}`
*   **Description:** Retrieves a specific project by its ID.
*   **Authentication Required:** No
*   **Success Status Code:** `200 OK`
*   **`ApiResponseWrapper.data` Type:** `ProjectDto`

### 3. Create Project
*   **Method:** `POST`
*   **Path:** `/api/v1/projects`
*   **Description:** Creates a new project.
*   **Authentication Required:** Yes (Admin role, JWT Bearer token)
*   **Success Status Code:** `201 CREATED`
*   **`ApiResponseWrapper.data` Type:** `ProjectDto`

### 4. Update Project
*   **Method:** `PUT`
*   **Path:** `/api/v1/projects/{id}`
*   **Description:** Updates an existing project by its ID.
*   **Authentication Required:** Yes (Admin role, JWT Bearer token)
*   **Success Status Code:** `200 OK`
*   **`ApiResponseWrapper.data` Type:** `ProjectDto`

### 5. Delete Project
*   **Method:** `DELETE`
*   **Path:** `/api/v1/projects/{id}`
*   **Description:** Deletes a project by its ID.
*   **Authentication Required:** Yes (Admin role, JWT Bearer token)
*   **Success Status Code:** `204 NO CONTENT`
*   **`ApiResponseWrapper.data` Type:** None (no response body)

---

## Skill Endpoints

Base Path: `/api/v1/skills`

### 1. Get All Skills
*   **Method:** `GET`
*   **Path:** `/api/v1/skills`
*   **Description:** Retrieves a list of all skills.
*   **Authentication Required:** No
*   **Success Status Code:** `200 OK`
*   **`ApiResponseWrapper.data` Type:** `List<SkillDto>`

### 2. Get Skill by ID
*   **Method:** `GET`
*   **Path:** `/api/v1/skills/{id}`
*   **Description:** Retrieves a specific skill by its ID.
*   **Authentication Required:** No
*   **Success Status Code:** `200 OK`
*   **`ApiResponseWrapper.data` Type:** `SkillDto`

### 3. Create Skill
*   **Method:** `POST`
*   **Path:** `/api/v1/skills`
*   **Description:** Creates a new skill.
*   **Authentication Required:** Yes (Admin role, JWT Bearer token)
*   **Success Status Code:** `201 CREATED`
*   **`ApiResponseWrapper.data` Type:** `SkillDto`

### 4. Delete Skill
*   **Method:** `DELETE`
*   **Path:** `/api/v1/skills/{id}`
*   **Description:** Deletes a skill by its ID.
*   **Authentication Required:** Yes (Admin role, JWT Bearer token)
*   **Success Status Code:** `204 NO CONTENT`
*   **`ApiResponseWrapper.data` Type:** None (no response body)

---

## Contact Message Endpoints

Base Path: `/api/v1/contact-messages`

### 1. Submit Contact Message
*   **Method:** `POST`
*   **Path:** `/api/v1/contact-messages`
*   **Description:** Submits a new contact message.
*   **Authentication Required:** No
*   **Success Status Code:** `201 CREATED`
*   **`ApiResponseWrapper.data` Type:** `Map<String, String>` (e.g., `{"message": "Contact message submitted successfully."}`)

---
**Note on DTOs:**
- `AuthResponse`: `{ "accessToken": "string", "user": UserDto }`
- `UserDto`: `{ "id": long, "email": "string", "firstName": "string", "lastName": "string", "profileImageUrl": "string", "roles": ["USER" | "ADMIN"], "createdAt": "datetime" }`
- `ProjectDto`: `{ "id": long, "title": "string", "description": "string", "techStack": ["string"], "repoUrl": "string", "liveUrl": "string", "imageUrl": "string", "userId": long, "createdAt": "datetime", "updatedAt": "datetime" }`
- `SkillDto`: `{ "id": long, "name": "string", "level": "BEGINNER" | "INTERMEDIATE" | "EXPERT", "userId": long, "createdAt": "datetime", "updatedAt": "datetime" }`
(Details of request DTOs like `LoginRequest`, `RegisterRequest`, `CreateProjectRequest`, `CreateSkillRequest`, `CreateContactMessageRequest` can be found in the Swagger UI or source code.)
