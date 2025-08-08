# AGENTS.md

# ForkMyFolio Backend Architecture & Coding Standards

---

## Table of Contents

- [Overview](#overview)
- [User Roles and Permissions](#user-roles-and-permissions)
- [Project Architecture](#project-architecture)
- [Manual DTO Mappers](#manual-dto-mappers)
- [Controller Layer](#controller-layer)
- [Service Layer](#service-layer)
- [Data Model and Entity Design](#data-model-and-entity-design)
- [Backup and Restore Strategy](#backup-and-restore-strategy)
- [Swagger (OpenAPI) Documentation](#swagger-openapi-documentation)
- [JavaDoc and Code Comments](#javadoc-and-code-comments)
- [Error Handling](#error-handling)
- [Testing](#testing)
- [Coding Conventions](#coding-conventions)

---

## Overview

This document describes the backend architecture, design decisions, and coding standards for the ForkMyFolio project. The aim is to maintain consistency, ensure code quality, and facilitate collaboration among developers.

---

## User Roles and Permissions

- **Visitor:** Unauthenticated users. Can only view public portfolio data.
- **User:** Authenticated users who can manage and backup their own profile data.
- **Admin:** Authenticated with elevated permissions. Can manage their own profile plus backup and restore other users’ data, including bulk operations for migrations and server moves.

---

## Project Architecture

- The backend uses **Spring Boot** with a layered architecture:
   - **Controller Layer:** REST API endpoints exposing business functionality.
   - **Service Layer:** Business logic and data enrichment.
   - **Repository Layer:** Direct data access via Spring Data JPA.
- All API responses are wrapped in a generic response format to standardize success/error handling.
- **Entities** map to database tables using JPA annotations.
- DTOs (Data Transfer Objects) are used to expose only necessary data to clients.
- Mapping between Entities and DTOs is done via **manual mappers** to keep explicit control.

---

## Manual DTO Mappers

- All entity-to-DTO and DTO-to-entity conversions are done manually (no MapStruct or other tools).
- This allows precise control over fields exposed, filtering logic, and context-based enrichment.
- Mappers reside in `com.forkmyfolio.mapper` package.
- Each mapper has methods like `toDto(Entity entity)` and `toEntity(Dto dto)`.
- When an entity has nested relations (e.g., Experience → Skill + UserSkill enrichment), the mapper receives contextual data (like a skill proficiency map) for richer DTOs.

---

## Controller Layer

- Controllers are split into multiple focused classes, grouped by resource (e.g., `PortfolioExperienceController`, `PortfolioQualificationsController`).
- Each controller uses path variables (e.g., `{slug}`) to identify user portfolios.
- Controllers only orchestrate service calls and return DTOs wrapped in standard response wrappers.
- File downloads (PDF, Markdown, vCard) skip JSON response wrapping.
- API base path uses `/api/v1`.
- Controllers use Swagger annotations (`@Operation`, `@Tag`) for auto-generating API docs.
- Access control is enforced by Spring Security roles and JWT tokens.

---

## Service Layer

- Services contain all business logic and data enrichment.
- They interact with repositories and perform transformations like skill proficiency enrichment on Experience and Project entities.
- Services also handle backup and restore operations with fine-grained control depending on user role.
- Public portfolio retrieval aggregates data into composite DTOs.
- Backup services handle:
   - **User backup:** Single user backing up their profile data.
   - **Admin backup:** Backup of single or all user profiles.
   - **User restore:** Restoring a single user’s profile.
   - **Admin restore:** Bulk restore after migrations or server changes.

---

## Data Model and Entity Design

- Entities use UUIDs as external identifiers, with internal primary keys for efficiency.
- Key relationships:
   - `User` has many `Experiences`, `Projects`, `Qualifications`, `UserSkills`, `Testimonials`.
   - `Experience` links to many global `Skills`.
   - User skill proficiency is kept in `UserSkill`, separate from `Skill`.
   - `Experience` references `Skill` entities, not `UserSkill`, to maintain clean data boundaries.
- The service layer enriches `Experience` and `Project` DTOs with user skill proficiency info during mapping.
- Visibility flags control what is exposed publicly.

---

## Backup and Restore Strategy

| Backup Type      | Who Can Perform    | What is Backed Up                   | Use Case                               |
|------------------|--------------------|-----------------------------------|--------------------------------------|
| Visitor          | N/A                | N/A                               | Not applicable                       |
| User Backup      | Authenticated User | Own profile data only              | User backing up their portfolio data |
| Admin Backup     | Admin              | All users’ profiles or self only  | Full system backup or admin self-use |
| User Restore     | Authenticated User | Restore own profile                | Restore own data after loss          |
| Admin Restore    | Admin              | Restore any/all users’ profiles   | Bulk restore after migration/server |

- Backup and restore operations are exposed via secure endpoints, with strict role validation.
- Restore operations validate input data and maintain data integrity.
- Backup data includes all necessary related entities to fully reconstruct a user profile.

---

## Swagger (OpenAPI) Documentation

- Swagger docs are auto-generated from controllers using `springdoc-openapi`.
- API docs are exposed at `/api/v1/docs`.
- All endpoints have `@Operation` annotations with summaries and descriptions.
- Models have proper schema descriptions.
- File download endpoints use `@SkipApiResponseWrapper` to avoid wrapping binary data.
- Swagger UI is accessible for easy manual API exploration.

---

## JavaDoc and Code Comments

- All public classes and methods contain detailed JavaDoc comments.
- Comments describe method purpose, parameters, return values, exceptions thrown.
- Complex logic inside services or mappers is also documented inline.
- The goal is maintainability and easing onboarding of new developers.

---

## Error Handling

- Custom exceptions like `ResourceNotFoundException` are used for domain errors.
- Global exception handlers convert exceptions into consistent API error responses.
- Validation errors produce detailed messages via Spring Validation annotations.

---

## Testing

- Unit tests cover service logic and manual mappers.
- Integration tests cover REST endpoints with mock data.
- Tests verify role-based access, error cases, and response formats.

---

## Coding Conventions

- Follow standard Java conventions (camelCase, PascalCase for classes).
- Use Lombok to reduce boilerplate (`@Getter`, `@Setter`, `@NoArgsConstructor`).
- Use explicit imports, no wildcard imports.
- DTOs live separately from entities, usually in `dto.response` or `dto.request`.
- Service interfaces followed by implementations in `service.impl`.
- Controller classes kept thin and focused on request/response orchestration.
- Use consistent naming for REST endpoints (plural resource names, nested paths).

---

## Summary

This document captures the key architecture, coding standards, and backend design decisions for ForkMyFolio. It ensures a consistent, secure, and maintainable backend codebase that supports multiple user roles, clean DTO mapping, detailed Swagger documentation, and robust backup and restore functionality.

---

*Generated and maintained by Nemesis, 2025.*

