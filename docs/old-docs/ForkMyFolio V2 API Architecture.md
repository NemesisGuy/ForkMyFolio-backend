# 🔧 ForkMyFolio V2 API Architecture Breakdown

ForkMyFolio V2 REST API is split into three clear sections based on authentication and usage context:
**Public**, **Authenticated Users**, and **Admin**.

---

## 🧱 1. Public API (No Auth Required)

Accessible by anyone. Used to view public portfolios and public settings.

### Base path: `/api/v1/portfolios`

| Method | Path                               | Description                              |
|--------|------------------------------------|------------------------------------------|
| GET    | `/{slug}`                          | Get public portfolio by slug             |
| GET    | `/{slug}/settings`                 | Get public settings for portfolio        |
| GET    | `/{slug}/pdf`                      | Download portfolio PDF                   |
| GET    | `/{slug}/vcard`                    | Download portfolio vCard                 |
| GET    | `/{slug}/markdown`                 | Download portfolio Markdown              |
| POST   | `/{slug}/contact-messages`         | Submit a contact message to the user     |

---

### Base path: `/api/v1/settings`

| Method | Path              | Description                              |
|--------|-------------------|------------------------------------------|
| GET    | `/`               | Get all public settings as list of objects |
| GET    | `/pdf-templates`  | Get list of available PDF template names |

---

## 🧑‍💻 2. Authenticated User API (JWT Auth Required)

Users manage their own data here. All routes are prefixed with `/api/v1`.

### Base path: `/auth`

| Method | Path            | Description                          |
|--------|-----------------|------------------------------------|
| POST   | `/register`     | Register new user                  |
| POST   | `/login`        | Login user                         |
| POST   | `/refresh-token`| Refresh JWT access token           |
| POST   | `/logout`       | Logout user                        |

---

### Base path: `/me`

| Method | Path            | Description                              |
|--------|-----------------|------------------------------------------|
| GET    | `/`             | Get current user's account details       |
| PUT    | `/`             | Update current user's account details    |

---

### Base path: `/me/settings`

| Method | Path            | Description                              |
|--------|-----------------|------------------------------------------|
| GET    | `/`             | Get effective user settings (global + overrides) |
| PUT    | `/`             | Bulk update user's personal settings     |

---

### Base path: `/me/profile`

| Method | Path            | Description                              |
|--------|-----------------|------------------------------------------|
| GET    | `/`             | Get detailed portfolio profile           |
| PUT    | `/`             | Update detailed portfolio profile        |

---

### Base path: `/me/projects`

| Method | Path            | Description                              |
|--------|-----------------|------------------------------------------|
| GET    | `/`             | Get all of my projects                   |
| GET    | `/{uuid}`       | Get one project by UUID                  |
| POST   | `/`             | Create new project                       |
| PUT    | `/{uuid}`       | Update project by UUID                   |
| DELETE | `/{uuid}`       | Delete project by UUID                   |

---

### Base path: `/me/experiences`

| Method | Path            | Description                              |
|--------|-----------------|------------------------------------------|
| GET    | `/`             | Get all of my experiences                |
| GET    | `/{uuid}`       | Get one experience by UUID               |
| POST   | `/`             | Create new experience                    |
| PUT    | `/{uuid}`       | Update experience by UUID                |
| DELETE | `/{uuid}`       | Delete experience by UUID                |

---

### Base path: `/me/skills`

| Method | Path            | Description                              |
|--------|-----------------|------------------------------------------|
| GET    | `/`             | Get all of my skills                     |
| GET    | `/{uuid}`       | Get one skill by UUID                    |
| POST   | `/`             | Create new skill                         |
| PUT    | `/{uuid}`       | Update skill by UUID                     |
| DELETE | `/{uuid}`       | Delete skill by UUID                     |

---

### Base path: `/me/qualifications`

| Method | Path            | Description                              |
|--------|-----------------|------------------------------------------|
| GET    | `/`             | Get all of my qualifications             |
| GET    | `/{uuid}`       | Get one qualification by UUID            |
| POST   | `/`             | Create new qualification                 |
| PUT    | `/{uuid}`       | Update qualification by UUID             |
| DELETE | `/{uuid}`       | Delete qualification by UUID             |

---

### Base path: `/me/testimonials`

| Method | Path            | Description                              |
|--------|-----------------|------------------------------------------|
| GET    | `/`             | Get all of my testimonials               |
| GET    | `/{uuid}`       | Get one testimonial by UUID              |
| POST   | `/`             | Create new testimonial                   |
| PUT    | `/{uuid}`       | Update testimonial by UUID               |
| DELETE | `/{uuid}`       | Delete testimonial by UUID               |

---

### Base path: `/me/contact-messages`

| Method | Path            | Description                              |
|--------|-----------------|------------------------------------------|
| GET    | `/`             | Get all of my received messages          |
| DELETE | `/{uuid}`       | Delete one of my messages by UUID        |

---

### Base path: `/me/backup`

| Method | Path            | Description                              |
|--------|-----------------|------------------------------------------|
| GET    | `/`             | Download a backup of my portfolio data   |
| POST   | `/restore`      | Restore my portfolio from a backup file  |

---

## 🛡️ 3. Admin API (Admin Role Required)

Site-wide admin tasks. All routes are prefixed with `/api/v1/admin`.

### User Management

| Method | Path                      | Description                               |
|--------|---------------------------|-------------------------------------------|
| GET    | `/users`                  | List all users (paginated)                |
| POST   | `/users`                  | Create new user (Admin)                   |
| GET    | `/users/{userId}`         | Get user by UUID                          |
| PUT    | `/users/{userId}`         | Update user details                       |
| DELETE | `/users/{userId}`         | Deactivate user (soft delete)             |

---

### Platform Management

| Method | Path                         | Description                               |
|--------|------------------------------|-------------------------------------------|
| GET    | `/stats`                     | Get visitor statistics                    |
| GET    | `/settings`                  | Get all application settings              |
| PUT    | `/settings`                  | Update multiple application settings      |
| GET    | `/contact-messages`          | Get all contact messages from all users   |
| DELETE | `/contact-messages/{uuid}`   | Delete any contact message by UUID        |
| GET    | `/backup/system`             | Generate and download full system backup  |
| POST   | `/restore/system`            | Restore system from backup (destructive)  |

---

## 🔐 Auth and Roles

- Public routes require no auth.
- User routes require JWT auth (short-lived + refresh).
- Admin routes require role: `ADMIN`.

---

## 📦 Response Format

All JSON responses follow this structure:

## 📦 Response Format

All responses follow this structure:

```json
{
  "status": "success",
  "data": {...},
  "errors": []
}
```

Summary
The entire API is logically partitioned by access level:

🌍 Public Zone (/api/v1/portfolios & /api/v1/settings)
For anonymous visitors to view public portfolios and settings.

👤 Authenticated User Zone (/api/v1/auth & /api/v1/me)
For logged-in users to manage their portfolio, projects, backups, and settings.

🛠️ Admin Zone (/api/v1/admin)
For administrators to manage users, platform stats, settings, and contact messages.

This modular structure makes the API predictable, secure, and maintainable.