# API Structure Changes for Frontend

This document outlines the recent changes to the API data structures that affect the frontend. All database schema issues have been resolved, and the API is now stable and fully functional.

## Summary of Changes

The primary changes involve adding several new fields to the `User` and `Qualification` resources to support more detailed portfolio information.

---

### 1. User Resource (`/api/v1/me`)

The main `User` object, returned from endpoints like `/api/v1/me` or as part of the `/api/v1/auth/login` response, now includes a new boolean field.

-   **`emailVerified` (boolean)**: Indicates if the user's email has been confirmed. This is automatically `true` for users who sign up via an OAuth2 provider (like Google).

#### Updated `UserDto` Example:

```json
{
  "uuid": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane.doe@example.com",
  "slug": "jane-doe",
  "profileImageUrl": "https://example.com/path/to/image.jpg",
  "emailVerified": true,
  "active": true
}
```

---

### 2. Qualification Resource

The `Qualification` object, which is part of the main portfolio response (`/api/v1/portfolios/{slug}`), has been significantly enhanced with several new fields.

-   **`institutionLogoUrl` (string)**: A URL to the logo of the school or institution.
-   **`institutionWebsite` (string)**: A URL to the institution's main website.
-   **`fieldOfStudy` (string)**: The specific field of study (e.g., "Computer Science", "Graphic Design").
-   **`level` (string)**: The academic level of the qualification (e.g., "BACHELOR_DEGREE", "CERTIFICATE"). This is an enum.
-   **`startYear` (integer)**: The year the qualification was started.
-   **`stillStudying` (boolean)**: A flag to indicate if the user is currently still pursuing this qualification.
-   **`credentialUrl` (string)**: A URL to a digital certificate, badge, or verification page.

#### Updated `QualificationDto` Example:

```json
{
  "uuid": "f1e2d3c4-b5a6-7890-4321-fedcba098765",
  "qualificationName": "Bachelor of Science",
  "institutionName": "Metropolis University",
  "grade": "First Class Honours",
  "visible": true,
  "completionYear": 2024,
  "institutionLogoUrl": "https://example.com/logos/mu.png",
  "institutionWebsite": "https://metropolis-university.edu",
  "fieldOfStudy": "Computer Science",
  "level": "BACHELOR_DEGREE",
  "startYear": 2020,
  "stillStudying": false,
  "credentialUrl": "https://credential.net/verify/12345"
}
```

---

### 3. Authentication Flow

The authentication flow documentation (`docs/AUTHENTICATION.md`) is now fully implemented and accurate. The system uses a JWT access token in the response body and a refresh token in a secure, `HttpOnly` cookie. Please refer to that document for details on handling token expiration and refresh.

No other breaking changes have been made to the API structure. The endpoints and overall response wrapper remain the same.