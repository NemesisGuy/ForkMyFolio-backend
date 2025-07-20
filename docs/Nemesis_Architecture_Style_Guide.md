
# 🕷️ Nemesis Architecture & Code Style Guide

_"Clarity, Stability, Control."_

---

## == 1: Architectural Core Principles

### ◽️ Domain-Driven Design (DDD) First
- **Entities** contain domain state and behavior.
- **Repositories** abstract persistence.
- **Services** operate purely on Entities — **never DTOs.**
- **DTOs** are strictly for Controller layer input/output.
- Controllers handle mapping. Services only see Entities.
- Manual mappers between Entity ⇄ DTO (NO MapStruct, NO magic).

---

## == 2: Entity Identification
- Every Entity has both:
  - **Primary Key (ID)** for internal use (auto-increment or DB-native).
  - **UUID** for external/public identification (used in APIs and URLs).
- UUIDs prevent ID guessing and support obfuscation.
- Never expose internal PKs externally.

---

## == 3: API Structure (RESTful Standards)
- RESTful, resource-oriented URLs:
  `/api/v1/users`, `/api/v1/orders/{id}`, `/api/v1/products/{productId}/reviews`
- Plural nouns for resources.
- HTTP verbs:
  - `GET` for retrieve
  - `POST` for create
  - `PUT` / `PATCH` for update
  - `DELETE` for removal
- Query parameters for filtering, sorting, pagination.
- Responses wrapped in `ApiResponse<T>`:
```json
{
  "status": "success",
  "data": [...],
  "errors": []
}
```

---

## == 4: Error Handling
- Global Exception Handler.
- HTTP codes respected.
- Error response structure:
```json
{
  "status": "error",
  "data": null,
  "errors": ["Reason goes here..."]
}
```

---

## == 5: Security
- Roles: `USER`, `ADMIN`, `SUPERADMIN`.
- Admin routes under `/admin/`.
- APIs secured by default.
- Auth flow:
  - **Short-lived JWTs** (5-15 min access tokens).
  - **HttpOnly, Secure cookies** for refresh tokens (`SameSite=Strict`).
  - Access via `Authorization: Bearer`.
  - `/auth/refresh` rotates tokens.
- Clear separation of access vs. refresh responsibilities.

---

## == 6: Backend Standards (Spring Boot + MySQL)
- `@Service`, `@Repository`, `@RestController` separation.
- `@Valid` on DTOs at Controller layer only.
- **MySQL** as default DB.
- Liquibase for migrations.
- Logging via `Slf4j`.
- Profiles: `dev`, `prod`.

### Folder Structure:
```
/domain
/domain/repository
/domain/service
/domain/entity
/api/controller
/api/dto
/api/mapper
/infrastructure
```
- **Javadoc comments** required on backend code.

---

## == 7: Frontend Standards (Vue + Bootstrap + Vite)
- Vue 3 + Composition API.
- **Bootstrap** for styling.
- Axios with interceptor to inject backend URL dynamically.
- URL configured via **Vite environment variable** (`VITE_API_URL`).
- Supports Docker Compose without rebuilds.
- **JSDoc comments** required on frontend code.

### Component Structure:
```
/views
/components
```
- DRY shared UI primitives.

---

## == 8: Infrastructure / DevOps
- Fully Dockerized.
- Backend API `:8080`, Frontend `:80`, Nginx reverse proxy optional.
- `.env` for dev, `.env.production` for deployments.
- **Docker Compose** defines frontend/backend services and injects `VITE_API_URL`.
- CI/CD: build > test > deploy.
- Nginx reverse proxy allowed in production.

### Observability:
- **Swagger / OpenAPI** docs at `/api/v1/docs`.
- **Prometheus** metrics at `/actuator/prometheus`.
- **Loki** for centralized JSON structured logs.
- **Grafana** for logs/metrics dashboards.

---

## == 9: Networking Tools Integration
- Vue frontend visually aggregates tools.
- Backend proxies:
    - Speedtest
    - Ping
    - Traceroute
    - LibreSpeed
    - Uptime Kuma
    - Netdata
    - Technitium DNS
    - Prometheus
    - Loki
    - Custom Ping/Trace modules
- All outputs standardized to JSON.

---

## == 10: Naming Conventions
| Type        | Convention     | Example             |
|-------------|----------------|---------------------|
| Classes     | PascalCase      | `UserService`        |
| Variables   | camelCase       | `userRepository`     |
| Constants   | UPPER_SNAKE_CASE | `DEFAULT_TIMEOUT_MS` |
| Packages    | lowercase.dots  | `com.nemesisnet.app` |

---

## == 11: Testing
- Unit tests **where they add value**, not for every trivial method/class.
- Focus on core business logic, services, mappers, and critical paths.
- Integration tests for controllers.
- API contract tests for `/api/v1`.
- Frontend E2E encouraged via Cypress or Playwright.

---

## == 12: Tone for AI Agent Output
**Formal, Direct, Structured.**
- No speculative features.
- No half-finished scaffolds.
- Tests required.
- Respect DDD boundaries.
- Return runnable, clean code only.

---

## == 13: Golden Rules for All Agents
✅ Controller handles DTO mapping.  
✅ Services only use Entities.  
✅ Repositories handle persistence isolation.  
✅ Layers remain clean.  
✅ Vue components atomic.  
✅ Always Dockerized.  
✅ Consistent logging.  
✅ Mandatory testing (where it matters).  
✅ All APIs wrapped responses.

---
