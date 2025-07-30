# Architecture & Code Style Guide

This document explains the core architectural principles, design decisions, and coding standards for the ForkMyFolio project. Adhering to these rules ensures the codebase remains clean, maintainable, and scalable.

---

## 1. Core Architectural Principles: A Clean, Layered Architecture

Our backend follows a philosophy inspired by Domain-Driven Design (DDD) to ensure a strict separation of concerns. Each layer has a distinct responsibility, and data flows in a predictable way.

### The Layers

1.  **`@RestController` (Controller Layer)**
    *   **Responsibility**: The only layer that interacts with the outside world via HTTP. It handles incoming requests and outgoing responses.
    *   **What it does**: It receives Data Transfer Objects (DTOs) from requests, validates them (`@Valid`), and passes the data to the Service layer. It then receives data back from the Service layer and maps it to DTOs before sending the final `ApiResponseWrapper`.
    *   **Key Rule**: Controllers **never** contain business logic. They are thin layers for translation and delegation.

2.  **`@Service` (Service Layer)**
    *   **Responsibility**: This is where all business logic lives. It coordinates operations, performs calculations, and enforces business rules.
    *   **What it does**: It operates **exclusively** on domain models (`@Entity` objects). It fetches entities from repositories, manipulates them according to the business requirements, and saves them back.
    *   **Key Rule**: Services **never** know about DTOs or the HTTP layer. They are pure, reusable business logic components.

3.  **`@Repository` (Repository/Persistence Layer)**
    *   **Responsibility**: To abstract all database interactions.
    *   **What it does**: It provides methods to find, save, update, and delete entities from the database. It uses Spring Data JPA to handle the boilerplate.
    *   **Key Rule**: Repositories are simple interfaces for data access. They do not contain business logic.

### Data Flow: DTOs vs. Entities

-   **DTOs (Data Transfer Objects)**: These are plain Java objects used **only** to transfer data to and from the API (Controller layer). They define the public "shape" of our API.
-   **Entities (`@Entity`)**: These are the rich domain models that represent our core business concepts (e.g., `User`, `Project`). They contain the actual state and are used within the Service and Repository layers.

**The flow is strict:**
`Request (DTO)` → `Controller` → `Service (operates on Entity)` → `Repository` → `Database`

---

## 2. API Design: Consistency and Clarity

A predictable API is a usable API. We follow strict RESTful principles.

-   **Resource-Oriented URLs**: Use plural nouns for resources (e.g., `/api/v1/projects`, `/api/v1/skills`).
-   **Standard HTTP Verbs**: `GET` (retrieve), `POST` (create), `PUT` (update), `DELETE` (remove).
-   **Standardized Response Wrapper**: Every single API response is wrapped in `ApiResponseWrapper<T>`. This provides a consistent structure for the frontend to handle both successful data payloads and detailed error messages.

```json
// Success
{
  "status": "success",
  "data": { "...": "..." },
  "errors": []
}

// Failure
{
  "status": "fail",
  "data": null,
  "errors": [ { "field": "email", "message": "Email is already in use" } ]
}
```

---

## 3. Security First

-   **External vs. Internal IDs**: We never expose our database's auto-incrementing primary keys (`id`) in the API. Instead, every public-facing entity uses a `UUID` for external identification. This prevents attackers from guessing resource IDs.
-   **Robust Authentication**: We use a standard JWT (JSON Web Token) flow with a short-lived access token and a long-lived refresh token stored in a secure, `HttpOnly` cookie. This provides a strong defense against common web vulnerabilities like XSS.
-   **Admin-Only Routes**: All administrative functions are protected under the `/api/v1/admin/` path and require `ADMIN` role privileges.

---

## 4. Backend Standards (Spring Boot + MySQL)
- `@Service`, `@Repository`, `@RestController` separation.
- `@Valid` on DTOs at Controller layer only.
- **MySQL** as default DB.
- Liquibase for migrations.
- Logging via `Slf4j`.
- Profiles: `dev`, `prod`.
- **Javadoc comments** required on backend code.

---

## 5. Frontend Standards (Vue + Bootstrap + Vite)
- Vue 3 + Composition API.
- **Bootstrap** for styling.
- Axios with interceptor to inject backend URL dynamically.
- URL configured via **Vite environment variable** (`VITE_API_URL`).
- Supports Docker Compose without rebuilds.
- **JSDoc comments** required on frontend code.

---

## 6. Infrastructure and Deployment

-   **Docker is King**: The entire application (backend and frontend) is containerized with Docker. This ensures a consistent environment from development to production.
-   **Configuration via Environment**: We use Spring Profiles (`dev`, `prod`) to manage different configurations. All sensitive information (database credentials, JWT secrets) is injected via environment variables, never hardcoded.
-   **Docker Compose** defines frontend/backend services and injects `VITE_API_URL`.
-   **CI/CD**: build > test > deploy.
-   **Observability**:
    -   **Swagger / OpenAPI** docs at `/api/v1/docs`.
    -   **Prometheus** metrics at `/actuator/prometheus`.
    -   **Loki** for centralized JSON structured logs.
    -   **Grafana** for logs/metrics dashboards.

---

## 7. Testing
- Unit tests **where they add value**, not for every trivial method/class.
- Focus on core business logic, services, mappers, and critical paths.
- Integration tests for controllers.
- API contract tests for `/api/v1`.
- Frontend E2E encouraged via Cypress or Playwright.

---

## Golden Rules: The TL;DR

If you remember nothing else, remember these:

1.  **Controllers handle HTTP and DTOs.**
2.  **Services handle business logic and Entities.**
3.  **Repositories handle database access.**
4.  **Never mix these responsibilities.**
5.  **Use UUIDs for all external APIs.**
6.  **Wrap all API responses.**
7.  **Always Dockerized.**
8.  **Mandatory testing (where it matters).**
