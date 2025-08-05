# ForkMyFolio v2 Beta Features

> **Codename:** Nemesis: Forge Edition 
> _"Your identity, your data, your infra — self-hosted, secured, and battle-tested."_

## ✨ CORE SYSTEM
- ✅ Spring Boot backend (secure, scalable)
- ✅ Vue 3 + Vite frontend (glassmorphic UI, light/dark mode)
- ✅ Dockerized architecture (frontend, backend, DB, with Nginx/Grafana planned)
- ✅ Runtime-configurable backend URL (Vite + Docker Compose)
- ✅ Clean architecture: DTOs, Mappers, Services, Entities
- ✅ Role-based auth (`ADMIN`, `USER`, etc.)
- ✅ JWT short-lived access + HttpOnly refresh cookies
- ✅ Slug-based routing for multi-user support

## 🛠️ DATA & BACKUPS
- ✅ Versioned, meta-tagged JSON export system
- ✅ Backups are backward **and forward** compatible
- ✅ Restore system intelligently parses and merges
- ✅ Export formats: JSON, PDF, Markdown, vCard (multiple templates)
- ✅ Full data coverage: Skills, Projects, Experience, Testimonials, etc.
- ✅ Admin-managed backup/restore lifecycle

## 🧩 SKILLS & PROJECT SYSTEM
- ✅ 500+ seeded skills on DB init
- ✅ Skill Pills: icon-tagged, category color, hover tooltips
- ✅ Skill levels (Beginner, Intermediate, Expert)
- ✅ Pills appear on Projects and Experience entries
- ✅ Auto-suggest missing skills from global pool (fuzzy match)
- ✅ Auto-add new skills to global and user pool if unmatched

## 📄 PAGE VISIBILITY & CUSTOMIZATION
- ✅ Per-section (Skills, Projects, Testimonials, etc.) visibility toggles
- ✅ Per-instance entity visibility control
- ✅ User decides what’s public vs private
- ✅ Light/Dark mode toggle
- ✅ Smart Nav: auth-aware, role-aware, slug-aware

## 🔐 SECURITY
- ✅ Login history: successful and failed attempts per user
- ✅ Built-in default Admin account
- 🔜 InitWizard to force admin credential change on first use
- 🔜 GuestPass system:
  - Temp, password-protected scoped access to specific routes
  - Full resume/contact visibility on private endpoint
  - Manage/revoke GuestPass entries via dashboard

## 📊 ANALYTICS & TELEMETRY
- ✅ Page view counters tracked per user
- ✅ Login attempt tracking
- ✅ Admin-accessible activity log
- 🔜 Grafana + Loki integration for:
  - Structured logging
  - Dashboard visualizations
  - Alerts and event tracking

## 💬 MESSAGING & UX
- ✅ Contact Us form: messages saved in DB
- ✅ Admin dashboard for message management
- ✅ Notification bell in navbar for new messages
- ✅ Polished SaaS-style UX

## 👩‍💼 JOB APPLICATION TRACKER (Planned)
- 🔜 Add job applications manually or via API
- 🔜 Track status: Applied, Interviewing, Offer, Rejected, etc.
- 🔜 Store job descriptions, notes, company info, resume version used
- 🔜 Analytics on application outcomes and timelines

## 🚀 FUTURE & IDEAS
- 🔜 GuestPass: tokenized access to private endpoints (temp credentials)
- 🔜 Startup InitWizard: force admin to secure credentials on first boot
- 🔜 GitHub/LinkedIn OAuth for skill sync
- 🔜 Calendar integration for freelance availability
- 🔜 Auto-update README.md/PDF when data changes (CI/CD style)
- 🔜 Plugin system for widgets (e.g. skill charts, GitHub pins)
- 🔜 Hosted version with Stripe integration (SaaS tier)
- 🔜 Public/private profile toggle
- 🔜 Webhook triggers (e.g., send resume via email on new GuestPass access)

---

> ForkMyFolio isn't a portfolio site. It's a full-stack developer identity platform.

**NemesisNet is almost live. Prepare accordingly.**

