# ForkMyFolio Backend: Designing a Modern, Multi-User Portfolio Platform in Spring Boot

If you’ve ever tried to build your own developer portfolio, you know how quickly things get messy. You start with a static page, then you want a blog, then a projects gallery, then a PDF resume, then analytics, then multi-user support, then… suddenly you’re maintaining a small SaaS in your spare time.

ForkMyFolio is what happens when you stop fighting that reality and embrace it.

This post is a deep dive into the **ForkMyFolio backend**: a Java 21 / Spring Boot 3 REST API that powers a multi-user, configurable, analytics-aware portfolio platform. We’ll walk through the **architecture**, **security model**, **API design**, **data model**, and the kinds of engineering trade‑offs you only see once you’ve iterated a few versions.

The goal is not just to show “here’s some Spring controllers,” but to explain *why* the system looks the way it does, and how the pieces fit together to support:

- Multiple users, each with their own portfolio and slug
- A clear separation between **public**, **authenticated**, and **admin** APIs
- JWT auth with refresh tokens stored in `HttpOnly` cookies
- Dynamic portfolio configuration (PDF templates, section toggles, etc.)
- Non‑intrusive analytics and backups for both users and admins

---

## 1. The Problem ForkMyFolio Is Solving

Developer portfolios are deceptively simple: “just a page with your projects,” right?

In practice, people want much more:

- A **single-page public portfolio** that feels dynamic and modern
- An **admin panel** to edit content without touching code
- **PDF export** of the portfolio / resume in multiple templates
- **Analytics** on visitors, project views, and engagement
- **Multi-user support**, so the platform can host many portfolios
- And all of this **secure**, **Dockerized**, and **production‑ready**

Rather than baking this logic into a monolithic full-stack app, ForkMyFolio cleanly separates concerns:

- A **frontend SPA** consumes a **RESTful backend API**.
- The backend exposes clearly partitioned **public**, **authenticated user**, and **admin** surfaces.
- The data model is flexible enough to represent many different portfolio structures, while still being opinionated about quality (skills, experiences, testimonials, etc.).

The backend is where these responsibilities are enforced, validated, and made safe.

---

## 2. High-Level Architecture

At a high-level, ForkMyFolio’s backend is a **layered Spring Boot application** that closely follows Domain‑Driven Design (DDD) inspired patterns.

### 2.1 Technology Stack

- **Language:** Java 21
- **Framework:** Spring Boot 3 (web, security, validation, OAuth2 client)
- **Persistence:** Spring Data JPA, MySQL (with Flyway), H2 for dev
- **Security:** Spring Security, JWT via `jjwt`
- **API Docs:** Springdoc OpenAPI (Swagger UI)
- **PDF Generation:** iText 7
- **Deployment:** Docker; environment-driven configuration

The `pom.xml` reflects this stack directly, with clear boundaries for core concerns: JPA, security, validation, JWT, OAuth2, PDF generation, and testing.

### 2.2 Project Structure

The backend follows the familiar Maven layout, with a clear package structure under `com.forkmyfolio`:

- `config` – Spring configuration (e.g., `SecurityConfig`)
- `controller` – REST controllers (`@RestController`), grouped by feature
- `dto` – Request/response models (API-facing)
- `exception` – Custom exceptions + global exception handler
- `model` – JPA entities (`@Entity`), the core domain
- `repository` – Spring Data JPA repositories
- `security` – JWT filters, authentication entrypoints, OAuth2 integration
- `service` – Business logic interfaces and implementations
- `mapper` – Manual DTO ↔ Entity mappers
- `util`, `validator`, `advice`, `aop` – Supporting infrastructure

The **golden rule** that drives this organization comes from `ARCHITECTURE_RULES.md`:

> Controllers handle HTTP and DTOs. Services handle business logic and Entities. Repositories handle database access. Never mix these responsibilities.

That discipline shows up everywhere:

- Controllers are intentionally thin and stateless.
- Services are where business logic and cross-entity orchestration lives.
- Repositories remain small, declarative interfaces.

---

## 3. Layered Design and Data Flow

The core of the architecture is a **strict separation of concerns**:

1. **Controller layer (`@RestController`)**
	 - Speaks HTTP/JSON.
	 - Accepts and returns **DTOs**, never entities.
	 - Uses `@Valid` for validation.
	 - Delegates to services and wraps results in a consistent `ApiResponseWrapper<T>` shape.

2. **Service layer (`@Service`)**
	 - Speaks domain language (entities, value objects, business rules).
	 - Knows nothing about HTTP or DTOs.
	 - Orchestrates repositories, performs validation and enrichment, applies permissions.

3. **Repository layer (`@Repository`)**
	 - Speaks database.
	 - Provides type-safe query methods via Spring Data JPA.
	 - Contains no business logic.

The **data flow** is deliberately one-directional:

`HTTP Request (JSON DTO)` → Controller → Service → Repository → Database → Service → Controller → `HTTP Response (DTO wrapped in ApiResponseWrapper)`

Even features that might be tempting to “just implement in a controller” (like PDF / vCard downloads) follow this separation: the controller is only responsible for **protocol-level concerns** (headers, content type), while the service handles file generation.

---

## 4. Designing for a Multi-User, Slug-Based World

One of the core V2 evolutions of ForkMyFolio is **multi-user support with slugs**. Instead of a single “site owner,” the platform can host many users, each with:

- A `slug` (e.g. `"nemesis"`, `"jane-doe"`) that becomes the public URL key
- A full set of portfolio content: profile, projects, skills, experiences, qualifications, testimonials
- Personal settings and visibility rules

### 4.1 The `User` Entity

The `User` model is the anchor of the backend’s data model:

- Internal primary key: `id` (auto-increment, DB-level)
- External identifier: `uuid` (public-facing)
- Public routing identifier: `slug` (used in URLs)
- Authentication details: `email`, `password`, `authProvider`, `providerId`
- Lifecycle flags: `active`, `emailVerified`, `termsAcceptedAt`, `termsVersion`
- Relationships to portfolio content:
	- `PortfolioProfile` (1:1)
	- `Project`, `Experience`, `Qualification`, `Testimonial`, `UserSkill`, `ContactMessage`, `UserSetting`, `RefreshToken` (1:N)
- Roles via `@ElementCollection<Role>` → Spring Security `GrantedAuthority` mapping

By implementing `UserDetails`, `User` integrates directly with Spring Security. The `getAuthorities()` method maps the internal `Role` enum to `ROLE_USER`, `ROLE_ADMIN`, etc., which is later used in access control.

### 4.2 UUIDs and Security

The architecture rules stress a security best practice: **never expose raw DB primary keys**. Instead:

- Every entity that appears in the public API exposes a **UUID** field.
- URLs and payloads reference these UUIDs, not numeric IDs.

This prevents simple “ID enumeration” attacks and makes it harder for a malicious user to guess resources that they shouldn’t see.

### 4.3 Slug as the Public Handle

For the public portfolio surface, the **slug** becomes the stable, human-readable key:

- Public read endpoints live under `/api/v1/portfolios/{slug}/…`.
- The `PortfolioService` exposes `getPublicPortfolioUserBySlug(String slug)` as the canonical way to look up a user for public consumption.

The contract for this method is intentionally strict:

- If the user is not found → `ResourceNotFoundException`.
- If the portfolio is not public → `PermissionDeniedException`.

This ensures that any controller building public views must go through a gate that enforces **both existence and visibility**.

---

## 5. API Surface: Public, User, and Admin Zones

ForkMyFolio V2’s API is explicitly partitioned into three zones:

1. **Public API (no auth)** – portfolio viewing and public settings
2. **Authenticated User API (JWT required)** – managing one’s own portfolio
3. **Admin API (JWT + ADMIN role)** – platform-wide management

This structure is described in both `TECHNICAL_DOCUMENTATION.md` and `ForkMyFolio V2 API Architecture.md`, and it mirrors what’s enforced in `SecurityConfig`.

### 5.1 Public API

Base paths include:

- `/api/v1/portfolios/{slug}` – Portfolio shell and sections
- `/api/v1/portfolios/{slug}/projects` – Public projects
- `/api/v1/portfolios/{slug}/experience` – Experience
- `/api/v1/portfolios/{slug}/qualifications` – Qualifications
- `/api/v1/portfolios/{slug}/skills` – Skills
- `/api/v1/portfolios/{slug}/testimonials` – Testimonials
- `/api/v1/portfolios/{slug}/contact-messages` – Contact form submission
- `/api/v1/portfolios/{slug}/pdf` – PDF download
- `/api/v1/portfolios/{slug}/markdown` – Markdown export
- `/api/v1/portfolios/{slug}/vcard` – vCard export

Additional public endpoints:

- `/api/v1/settings` – Public system settings (e.g., app-wide flags, theming)
- `/api/v1/settings/pdf-templates` – List of PDF templates
- `/api/v1/policies/terms-of-service` / `/privacy-policy` – Public policies

In `SecurityConfig`, these are configured as:

- `permitAll()` for:
	- `/api/v1/policies/**`
	- `GET /api/v1/settings`, `GET /api/v1/settings/pdf-templates`
	- `GET /api/v1/portfolios/**`
	- `POST /api/v1/portfolios/*/contact-messages`

This gives anonymous visitors full read access to public data, plus the ability to send contact messages.

### 5.2 Authenticated User API

Everything under `/api/v1/me/**` and parts of `/api/v1/auth/**` belong to the **user zone**.

Key areas include:

- `/api/v1/auth/register`, `/login`, `/refresh-token`, `/logout`
- `/api/v1/me` – Account details, password changes, terms acceptance
- `/api/v1/me/profile` – Detailed portfolio profile
- `/api/v1/me/projects` – CRUD on own projects
- `/api/v1/me/skills`, `/experiences`, `/qualifications`, `/testimonials`
- `/api/v1/me/settings` – Personal display settings
- `/api/v1/me/contact-messages` – Inbox for messages received through the public form
- `/api/v1/me/backup` – Self-service backup/restore

These endpoints enforce **ownership**: a user can only see and mutate their own data, never another user’s.

### 5.3 Admin API

The **admin zone** is strictly namespaced under `/api/v1/admin/**` and guarded by `hasRole("ADMIN")` in `SecurityConfig`.

Capabilities include:

- User management (`/users`)
- Global settings (`/settings`)
- System-wide contact messages
- Platform visitor statistics (`/stats`)
- Backup and restore:
	- Full system backups
	- System restores
	- Per-user restore operations

Admin operations are designed around **operational needs**: migrating servers, recovering from disasters, onboarding users, and diagnosing production issues.

---

## 6. Security: JWT, OAuth2, and CORS

Security is a first-class concern in ForkMyFolio, and the implementation leans heavily on Spring Security with a few important patterns.

### 6.1 JWT-Based Authentication

The authentication model uses two tokens:

- **Access token (JWT)** – Short-lived, sent in the `Authorization: Bearer <token>` header.
- **Refresh token** – Long-lived, stored in a secure **HttpOnly cookie**.

The flow looks like this:

1. User logs in via `/auth/login` or `/auth/register`.
2. Server returns:
	 - A JSON body containing the access token.
	 - A `Set-Cookie` header with the refresh token (HttpOnly, `SameSite=Lax` or configured, `Secure` in prod).
3. Subsequent calls to protected endpoints include the access token in the `Authorization` header.
4. When the access token expires, client calls `/auth/refresh-token`; the refresh cookie is sent automatically.
5. Server issues a new access token and rotates the refresh token (rolling tokens).
6. `/auth/logout` invalidates the refresh token and clears the cookie.

This design balances **security** (short-lived access, HttpOnly refresh) with **developer ergonomics** (simple, stateless requests, no session server state beyond refresh token records).

### 6.2 `SecurityConfig`: Central Policy Definition

`SecurityConfig` is the heart of the security setup:

- Disables CSRF for the JSON API context (`csrf(AbstractHttpConfigurer::disable)`).
- Enables CORS via a dedicated `CorsConfigurationSource` bean.
- Sets session management to **stateless**.
- Injects a custom `JwtAuthenticationFilter` *before* `UsernamePasswordAuthenticationFilter`.
- Plugs in `JwtAuthenticationEntryPoint` to standardize 401 responses.
- Configures OAuth2 login for external providers.

The configuration of CORS is non-trivial and carefully tuned:

- Allowed origins are injected via `@Value("${app.cors.allowed-origins}")`.
- Credentials are allowed (`config.setAllowCredentials(true)`), which is required for the refresh cookie.
- Allowed headers and exposed headers explicitly list what the frontend needs, including `Content-Disposition` for file downloads.

This combination allows a separate SPA frontend (e.g., on `localhost:3000` or a production domain) to interact comfortably with the backend while preserving cookie security.

### 6.3 Role-Based Access Control

Roles are assigned to `User` entities via a `Set<Role>` and are mapped to Spring Security authorities (`ROLE_USER`, `ROLE_ADMIN`).

`SecurityConfig` then uses these roles in the routing config:

- `/api/v1/admin/**` → `hasRole("ADMIN")`
- `/api/v1/me/**` → `hasAnyRole("USER", "ADMIN")`

Method-level security (`@EnableMethodSecurity`) is also enabled, allowing services and controllers to specify fine-grained constraints with `@RolesAllowed`, `@PreAuthorize`, etc., where needed.

---

## 7. DTOs, Mappers, and Response Wrappers

Exposing entities directly in a public API is convenient at first and painful later. ForkMyFolio avoids this entirely by using **DTOs + manual mappers** + a **standard response wrapper**.

### 7.1 Why Manual Mappers?

Rather than using MapStruct or reflection-heavy mappers, the backend uses **manual mappers** housed in `com.forkmyfolio.mapper`.

The reasons are pragmatic:

- **Explicitness** – You can see exactly what fields are exposed.
- **Contextual enrichment** – For example, experiences and projects can be enriched with user skill proficiency or derived properties.
- **Fine-grained control over evolution** – As V2 and later features add fields, mappers make it obvious what’s being added to the API vs. kept internal.

Typical mappers expose methods like:

- `toDto(Entity entity)`
- `toEntity(Dto dto)`
- `updateEntityFromDto(Dto dto, Entity entity)` (for patching)

### 7.2 Standardized API Responses

Every successful or failed API call returns a consistent JSON envelope, e.g.:

- On success:

	```json
	{
		"status": "success",
		"data": { "...": "..." },
		"errors": []
	}
	```

- On failure:

	```json
	{
		"status": "fail",
		"data": null,
		"errors": [
			{ "field": "email", "message": "Email is already in use" }
		]
	}
	```

This wrapper simplifies frontend logic: the UI can handle all responses through a single lens, regardless of which controller produced them.

---

## 8. Portfolio Features: Content, Settings, and Downloads

Beyond the architecture diagrams, what makes ForkMyFolio interesting is how it models and delivers portfolio content.

### 8.1 Core Portfolio Content

Each user’s portfolio is composed of several entity types:

- `PortfolioProfile` – High-level information: name, title, summary, links.
- `Project` – Projects, each with descriptions, links, and tags.
- `Experience` – Work/volunteer experiences.
- `Qualification` – Education and certifications.
- `UserSkill` – User-specific skill relationships, often referencing global platform `Skill` entries.
- `Testimonial` – Social proof from colleagues or clients.

The public `/portfolios/{slug}/…` endpoints aggregate this content into **read-optimized DTOs**. The service layer is responsible for:

- Filtering out private or hidden items.
- Ordering data (e.g., newest experience first).
- Enriching items with computed values (e.g., proficiency levels, tag categorization).

### 8.2 Dynamic Settings & Feature Flags

Settings are modeled at multiple levels:

- **Global settings** – Admin-configurable, apply to everyone.
- **User-specific settings** – Per-user overrides; for example, whether to display certain sections, which PDF template to default to, or which social links to emphasize.

This allows powerful combinations:

- Admin can enable or disable platform-wide features.
- Users can still tailor how their own portfolio surface behaves within those constraints.

The `/api/v1/me/settings` endpoint returns **effective settings**, combining global defaults and user overrides.

### 8.3 PDF, Markdown, and vCard Downloads

One of the signature user experiences is being able to download a portfolio in different formats:

- `/portfolios/{slug}/pdf` – PDF resume/portfolio powered by iText 7.
- `/portfolios/{slug}/markdown` – Easily portable Markdown version.
- `/portfolios/{slug}/vcard` – Contact card for quick import.

The implementation pattern for these follows the same principles:

1. Controller validates the request and calls a service method.
2. Service loads all necessary entities, composes a **view model** for export.
3. Service calls a specialized generator (PDF / Markdown / vCard).
4. Controller sets headers (`Content-Type`, `Content-Disposition`) and streams the result.

This keeps the export logic modular and testable, and makes it easy to add new formats later.

---

## 9. Visitor Analytics and Contact Messages

The backend includes support for **non-intrusive visitor analytics** and **contact message handling**.

### 9.1 Visitor Analytics

The analytics design follows a few core principles:

- **Non-intrusive** – No heavy tracking scripts or invasive fingerprinting.
- **Actionable, not exhaustive** – Focused on page views, project views, and key engagement signals.
- **Role-aware access** – Users can view their own stats; admins can view aggregated platform stats.

Endpoints under `/api/v1/stats` and `/api/v1/admin/stats` expose aggregated views that the frontend can chart or summarize.

### 9.2 Contact Messages: Public Entry, Private Inbox

The contact flow looks like this:

1. A visitor fills out a contact form on a user’s public portfolio.
2. The frontend sends a `POST /api/v1/portfolios/{slug}/contact-messages` request.
3. The backend:
	 - Resolves the slug to a user (`PortfolioService.getPublicPortfolioUserBySlug`).
	 - Stores the message as a `ContactMessage` entity.
4. The owning user later calls `/api/v1/me/contact-messages` to view or manage their inbox.

This design gives visitors a straightforward way to reach out, without exposing user emails directly on the page.

---

## 10. Backup & Restore: Treating Portfolios as First-Class Data

One of the standout features of ForkMyFolio is **backup and restore**, reflecting the idea that a portfolio can be a critical data asset for its owner.

### 10.1 User-Level Backup

Authenticated users can:

- `GET /api/v1/me/backup` – Download a JSON representation of their portfolio data.
- `POST /api/v1/me/backup/restore` – Restore from a previously downloaded backup.

This operation is carefully scoped:

- It only touches the calling user’s data.
- It respects referential integrity (e.g., recreating entities with proper relationships).

### 10.2 Admin-Level Backup and Disaster Recovery

Admins can:

- Generate full **system backups**.
- Restore the entire platform or restore a single user from a system backup.
- Perform destructive wipes when required (e.g., for GDPR/POPIA compliance in test environments).

These operations live under `/api/v1/admin/backup/...` and are heavily restricted by role.

---

## 11. Configuration, Profiles, and Dockerization

From the beginning, ForkMyFolio is designed to be **easy to run locally** and **safe to run in production**.

### 11.1 Spring Profiles

There are two primary profiles:

- `dev` (default) – Uses H2 in-memory database, developer-friendly JWT secrets, and local CORS origins.
- `prod` – Uses MySQL/PostgreSQL (depending on environment), secure cookie flags, strict CORS, and production-grade JWT secrets.

Running locally is as simple as:

```bash
mvn spring-boot:run
```

In production, you’d typically run:

```bash
java -jar -Dspring.profiles.active=prod target/forkmyfolio-backend-0.0.1-SNAPSHOT.jar
```

### 11.2 Environment Variables

Key environment variables include:

- `SPRING_PROFILES_ACTIVE` – `dev` or `prod`
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET` (base64 encoded, strong secret)
- `JWT_ACCESS_TOKEN_EXPIRATION_MS`, `JWT_REFRESH_TOKEN_EXPIRATION_MS`
- `APP_JWT_REFRESH_COOKIE_NAME`, `APP_COOKIE_SECURE`, `APP_COOKIE_SAMESITE`
- `APP_CORS_ALLOWED_ORIGINS`

Nothing sensitive is hard-coded; everything important is sourced from the environment.

### 11.3 Docker & Deployment

The repository ships with a `Dockerfile` and `docker-compose.yaml`:

- Build an image:

	```bash
	docker build -t forkmyfolio-backend .
	```

- Run with in-memory DB (dev):

	```bash
	docker run -d -p 8080:8080 --name forkmyfolio-backend-dev forkmyfolio-backend
	```

- Run with `prod` profile using an external database:

	```bash
	docker run -d -p 8080:8080 \
		-e SPRING_PROFILES_ACTIVE=prod \
		-e SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/your-db-name \
		-e SPRING_DATASOURCE_USERNAME=your-db-user \
		-e SPRING_DATASOURCE_PASSWORD=your-db-password \
		-e JWT_SECRET=your-super-strong-base64-encoded-jwt-secret \
		-e APP_CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com \
		--name forkmyfolio-backend-prod \
		forkmyfolio-backend
	```

This makes it easy to move from **local dev → Docker → cloud** without rewriting configuration.

---

## 12. Coding Standards, Testing, and Evolution

ForkMyFolio is not just a collection of endpoints; it’s a codebase built to be maintained and evolved.

### 12.1 Coding Standards

The documented standards emphasize:

- Clear package structure and layer responsibilities.
- DTOs for all inbound/outbound data.
- Manual mappers for transparency and control.
- UUIDs on the API surface, numeric IDs hidden behind the scenes.
- Consistent response wrappers for all endpoints.

These standards make it much easier for new contributors to join the project and for future-you to understand what past-you was thinking.

### 12.2 Testing

Tests live under `src/test/java/com/forkmyfolio` and can be run with:

```bash
mvn test
```

The test suite covers:

- Unit tests for services and mappers.
- Integration tests for controllers and security (using `spring-security-test`).

As the platform grows (e.g., more PDF templates, additional export formats, richer analytics), tests help guard against regressions—and validate that the architectural rules remain intact.

### 12.3 Evolving Toward a Platform

The V2 documentation hints at an even broader roadmap:

- More advanced job application tracking on top of portfolio data.
- Multi-tenant hosting scenarios.
- Richer analytics and dashboards.
- Tighter integrations with external identity providers (Google, GitHub, etc.).

The current architecture—layered, DTO-driven, and Slug/UUID-based—sets the stage for those evolutions without forcing a rewrite.

---

## 13. Lessons Learned and Takeaways

Building ForkMyFolio’s backend is a story of **refining boundaries**:

- Separating **public vs. user vs. admin** concerns greatly simplifies reasoning about authorization.
- Committing to **DTOs + manual mappers** pays dividends as the API evolves.
- Using **UUIDs and slugs** for external identifiers avoids a whole class of security and migration headaches.
- Treating **backups** as first-class features respects the importance of user data.
- Investing in a solid **security configuration** early (CORS, JWT, OAuth2, cookies) makes it easier to integrate frontends and third-party providers safely.

If you’re designing your own SaaS-style backend—especially for a content-rich, user-centric product—ForkMyFolio offers a concrete, production-ready reference for:

- How to structure a Spring Boot project around clear layers.
- How to think about slugs, UUIDs, and roles.
- How to expose a clean, well-documented REST API while keeping the internals flexible.

And most importantly: how to build something that starts as “my portfolio API” but can grow into “a multi-user portfolio platform” without collapsing under its own weight.

---

*End of draft. This can be adapted into a series (architecture deep dive, security deep dive, backup/restore deep dive) or used as a single long-form article introducing ForkMyFolio to developers and potential contributors.*

