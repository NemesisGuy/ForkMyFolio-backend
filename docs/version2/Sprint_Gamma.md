# 🚀 Sprint Gamma: Profile Visibility & Sharing

> **Sprint Goal:** Empower users with master control over their portfolio's visibility and make public profiles easily shareable. This sprint focuses on foundational features required before larger integrations like OAuth2 or advanced exports.

---

## ⭐ Primary Feature: Global Public/Private Profile Toggle

### 🎯 User Story

> "As a user, I want a single switch in my dashboard to make my entire portfolio either public (visible to anyone with the link) or private (visible only to me), so I can control who sees my information at any given time."

### 🛠 Backend Implementation Plan

#### 1. Entity Modification

Add a visibility toggle to the user's profile:

```java
// In PortfolioProfile.java
@Column(nullable = false)
private boolean isPublic = false; // Default to private for security
```

#### 2. DTO Update

Create a DTO to handle visibility toggle requests:

```java
// UpdateProfileVisibilityRequest.java
public class UpdateProfileVisibilityRequest {
  @NotNull
  private Boolean isPublic;
  // Getters/setters
}
```

#### 3. Service & Controller

- Add a method in `PortfolioProfileService` to update `isPublic`
- Create endpoint: `PUT /api/v1/me/profile/visibility`
- Modify `PublicPortfolioController` (`GET /api/v1/public/portfolio/{slug}`)
    - Check `isPublic` flag
    - If `false`, return `403 Forbidden` or custom "Profile is Private" response

---

### 💻 Frontend Implementation Plan

#### 1. UI Component

Add a prominent **toggle switch** in the user dashboard or profile settings.

#### 2. API Integration

Wire the switch to call `PUT /api/v1/me/profile/visibility` on change.

#### 3. Public View Handling

If profile is `!isPublic`, display:

> "🔒 This portfolio is private."

---

## 🎨 Secondary Polish Features

### 🔗 1. Social Sharing Buttons

> Once public, make profiles easy to share.

**Implementation:**

- Add sharing buttons to `/portfolio/:slug` (only if `isPublic === true`):
    - LinkedIn
    - X (Twitter)
    - Email
    - Copy Link (clipboard)

No backend work needed — just `<a>` tags with crafted URLs.

---

### 🔍 2. Public Resume SEO Enhancements

> Rich previews when links are shared = more professional.

**Implementation:**

- Use Vue's meta tools or static `<head>` block to add:
  ```html
  <meta property="og:title" content="John Doe – Software Engineer" />
  <meta property="og:description" content="10+ years experience in full-stack dev, AI, and cloud." />
  <meta property="og:image" content="/public/avatar_og.png" />
  ```
- Content is dynamic, sourced from user profile

---

## ✅ Outcomes of Sprint Gamma

| Feature                           | Status                   |
| --------------------------------- | ------------------------ |
| Public/Private toggle             | 🟢 Core security feature |
| Toggle UI + API sync              | 🟢 Dashboard UX          |
| Share buttons (X, LinkedIn, etc.) | 🟡 Viral growth support  |
| SEO Meta tags                     | 🟡 Enhanced visibility   |

---

### 🧠 Why This Sprint Matters

Completing Sprint Gamma makes ForkMyFolio:

- 🔐 Respectful of user privacy
- 📢 Shareable in a job-seeking context
- 🌐 Ready for future features like LinkedIn OAuth, QR export, and public resume routing

> *"A private portfolio is useless. A public one without control is dangerous. Sprint Gamma brings balance."*

