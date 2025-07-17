# ForkMyFolio API Endpoint Documentation

This document provides an overview of the available API endpoints for the ForkMyFolio application.

---

## 1. Public Endpoints

These endpoints are open and do not require authentication. They are used to populate the public-facing portfolio website.

### Portfolio & Content

-   **Get Public Profile Info**: `GET /api/v1/portfolio-profile`
-   **Get All Projects**: `GET /api/v1/projects`
-   **Get All Skills**: `GET /api/v1/skills`
-   **Get All Experience**: `GET /api/v1/experience`
-   **Get All Testimonials**: `GET /api/v1/testimonials`
-   **Get All Qualifications**: `GET /api/v1/qualifications`

### Submit Contact Message

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

### Get Public Settings

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

---

## 2. Authentication Endpoints

-   **Login, Refresh, etc.**: `POST /api/v1/auth/**`
    -   Handles user authentication and token management.

---

## 3. Admin Endpoints

All admin endpoints require a valid JWT `Bearer` token in the `Authorization` header and are prefixed with `/api/v1/admin`.

### Content Management (CRUD)

-   **Projects**: `GET, POST, PUT, DELETE /api/v1/admin/projects/{uuid}`
-   **Skills**: `GET, POST, PUT, DELETE /api/v1/admin/skills/{uuid}`
-   **Experience**: `GET, POST, PUT, DELETE /api/v1/admin/experience/{uuid}`
-   **Testimonials**: `GET, POST, PUT, DELETE /api/v1/admin/testimonials/{uuid}`
-   **Qualifications**: `GET, POST, PUT, DELETE /api/v1/admin/qualifications/{uuid}`

### Contact Message Management

-   **Get All Messages**: `GET /api/v1/admin/contact-messages`
-   **Delete a Message**: `DELETE /api/v1/admin/contact-messages/{uuid}`

### Application Settings Management

#### Get All Settings Details

-   **Endpoint**: `GET /api/v1/admin/settings`
-   **Purpose**: To get the full list of settings with all details for the admin UI.
-   **Response Body**: An array of setting objects.
    ```json
    [
      {
        "uuid": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
        "name": "SHOW_PROJECTS",
        "enabled": true,
        "description": "Display the \"Projects\" section on the public site."
      },
      {
        "uuid": "b2c3d4e5-f6a7-8901-2345-67890abcdef1",
        "name": "SHOW_SKILLS",
        "enabled": true,
        "description": "Display the \"Skills\" section on the public site."
      }
    ]
    ```

#### Update Settings (Bulk)

-   **Endpoint**: `PUT /api/v1/admin/settings`
-   **Purpose**: To update one or more settings in a single, efficient transaction.
-   **Request Body**: An array of objects, where each object contains the `uuid` of the setting to change and its new `enabled` status.
    ```json
    [
      {
        "uuid": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
        "enabled": false
      },
      {
        "uuid": "b2c3d4e5-f6a7-8901-2345-67890abcdef1",
        "enabled": true
      }
    ]
    ```
-   **Success Response**: The full, updated list of all settings (in the same format as the `GET` endpoint above).