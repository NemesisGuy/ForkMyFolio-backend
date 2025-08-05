
# ForkMyFolio – Data Flow Explanation (No DTOs in Service Layer)

## Overview

This document explains the preferred data flow in the `ForkMyFolio` architecture, specifically adhering to the design principle of **not using DTOs inside the Service Layer**.

## Why Avoid DTOs in the Service Layer?

- **Separation of Concerns**: Service layer should operate on domain logic and core entities only.
- **Maintainability**: DTOs are presentation or transport-layer constructs; mixing them in service logic tightly couples your domain to specific APIs or views.
- **Reusability**: Services become easier to reuse across different interfaces (e.g., REST API, CLI, GraphQL) when they work purely with entities.

---

## Preferred Data Flow

```text
[Outside Caller (DTO)]
        ↓
  [Controller (DTO)]
        ↓
   Mapper (DTO → Entity)
        ↓
  [Controller (Entity)]
        ↓
   [Service Layer (Entity)]
        ↓
  [Controller (Entity)]
        ↓
   Mapper (Entity → DTO)
        ↓
  [Controller (DTO)]
        ↓
[Outside Caller (DTO)]
```

---

## Example Flow

1. **Client sends a DTO** to create or update a resource.
2. **Controller receives the DTO**, validates it.
3. **Mapper converts DTO to Entity**.
4. **Entity is passed into the Service layer**.
5. **Service performs business logic** with pure domain entities.
6. **Entity result is returned to Controller**.
7. **Controller converts the Entity back to DTO** using a Mapper.
8. **DTO is returned to the client**.

---

## Benefits Recap

- Keeps services focused on business logic.
- Encourages testable, clean code.
- Allows DTO evolution without touching the service layer.
- Easy to add new API versions, CLI interfaces, or alternate views.

---

**Author:** Nemesis  
**System:** ForkMyFolio Clean Architecture  
**Date:** August 2025
