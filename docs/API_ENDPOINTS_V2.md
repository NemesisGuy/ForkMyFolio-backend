# ForkMyFolio API Endpoints (Simplified)

This document provides a high-level overview of the API endpoints available in the ForkMyFolio application.

---

## 1. Public Endpoints

Accessible to anyone without authentication.

### Authentication

| Method | Endpoint                     | Description                                |
| :----- | :--------------------------- | :----------------------------------------- |
| `POST` | `/api/v1/auth/register`      | Creates a new user account.                |
| `POST` | `/api/v1/auth/login`         | Authenticates a user and returns a token.  |
| `POST` | `/api/v1/auth/refresh-token` | Issues a new access token.                 |
| `POST` | `/api/v1/auth/logout`        | Logs the current user out.                 |

### Public Portfolios & Settings

| Method | Endpoint                                   | Description                                        |
| :----- | :----------------------------------------- | :------------------------------------------------- |
| `GET`    | `/api/v1/portfolios/{slug}`                | Retrieves a user's full public portfolio by slug.  |
| `GET`    | `/api/v1/portfolios/{slug}/settings`       | Retrieves the public display settings for a portfolio. |
| `POST`   | `/api/v1/portfolios/{slug}/contact-messages` | Submits a contact message to a specific user.      |
| `GET`    | `/api/v1/portfolios/{slug}/pdf`            | Downloads a PDF version of a user's portfolio.     |
| `GET`    | `/api/v1/portfolios/{slug}/vcard`          | Downloads a vCard of a user's contact info.        |
| `GET`    | `/api/v1/settings`                         | Retrieves public, system-wide application settings. |

---

## 2. Authenticated User Endpoints

Requires a valid user token. All endpoints are prefixed with `/api/v1/me`.

### Account & Profile

| Method | Endpoint      | Description                               |
| :----- | :------------ | :---------------------------------------- |
| `GET`    | `/`           | Gets the authenticated user's account details. |
| `PUT`    | `/`           | Updates the authenticated user's account details. |
| `GET`    | `/profile`    | Gets the user's detailed portfolio profile. |
| `PUT`    | `/profile`    | Updates the user's detailed portfolio profile. |

### Portfolio Content Management (CRUD)

| Method           | Endpoint            | Description                               |
| :--------------- | :------------------ | :---------------------------------------- |
| `GET` / `POST`   | `/projects`         | List all or create a new project.         |
| `GET`/`PUT`/`DELETE` | `/projects/{uuid}`  | Manage a specific project.                |
| `GET` / `POST`   | `/skills`           | List all or create a new skill.           |
| `GET`/`PUT`/`DELETE` | `/skills/{uuid}`    | Manage a specific skill.                  |
| `GET` / `POST`   | `/experiences`      | List all or create a new experience.      |
| `GET`/`PUT`/`DELETE` | `/experiences/{uuid}` | Manage a specific experience.             |
| `GET` / `POST`   | `/qualifications`   | List all or create a new qualification.   |
| `GET`/`PUT`/`DELETE` | `/qualifications/{uuid}` | Manage a specific qualification.          |
| `GET` / `POST`   | `/testimonials`     | List all or create a new testimonial.     |
| `GET`/`PUT`/`DELETE` | `/testimonials/{uuid}` | Manage a specific testimonial.            |

### User-Specific Management

| Method   | Endpoint                 | Description                                        |
| :------- | :----------------------- | :------------------------------------------------- |
| `GET` / `PUT`  | `/settings`              | Retrieve or update personal portfolio display settings. |
| `GET`      | `/contact-messages`      | Lists all contact messages received by the user.   |
| `DELETE`   | `/contact-messages/{uuid}` | Deletes a specific contact message.                |
| `GET`      | `/backup`                | Downloads a JSON backup of the user's portfolio.   |
| `POST`     | `/backup/restore`        | Restores portfolio data from a JSON backup.        |

---

## 3. Admin Endpoints

Requires a valid admin token. All endpoints are prefixed with `/api/v1/admin`.

### User Management

| Method   | Endpoint           | Description                                          |
| :------- | :----------------- | :--------------------------------------------------- |
| `GET`      | `/users`           | Lists all users in the system (paginated).           |
| `GET`      | `/users/{userId}`  | Gets details for a single user by their ID.          |
| `PUT`      | `/users/{userId}`  | Updates a user's details, roles, and active status.  |
| `DELETE`   | `/users/{userId}`  | Deactivates (soft deletes) a user account.           |

### Site-wide Management

| Method   | Endpoint                 | Description                                        |
| :------- | :----------------------- | :------------------------------------------------- |
| `GET` / `PUT`  | `/settings`              | Retrieve or bulk-update global application settings. |
| `GET`      | `/contact-messages`      | Lists all contact messages from all users.         |
| `DELETE`   | `/contact-messages/{uuid}` | Deletes any contact message from the system.       |
| `GET`      | `/stats`                 | Retrieves site-wide visitor statistics.            |
