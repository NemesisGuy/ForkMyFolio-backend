# Authentication Flow

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