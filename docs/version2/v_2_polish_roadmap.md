# ForkMyFolio – V2 Polish Roadmap (Beta Tier Polish Goals)

This roadmap captures the polish-level features that will take ForkMyFolio from an advanced full-stack resume builder to a top-tier open-source portfolio platform.

---

## 🧪 OAuth2 Authentication Integration

**Goal:** Enable seamless sign-in experience with trusted providers.

### Providers to Support:
- [ ] Google (primary target)
- [ ] GitHub (developer-focused)
- [ ] LinkedIn (job-seeker focused)
- [ ] Facebook (general audience, optional)

**Benefits:**
- Reduces user friction
- Increases trust & onboarding speed
- Enables easier integration with LinkedIn & GitHub APIs

---

## 🔄 Import Profile from LinkedIn

**Goal:** Fast-track user setup by importing CV data from LinkedIn.

### Steps:
- [ ] Allow upload of LinkedIn PDF resume export
- [ ] Extract relevant sections using parsing (PDFBox, etc.)
- [ ] Map to ForkMyFolio entities (Skills, Experience, Education)
- [ ] Provide UI to review/confirm before saving

**Optional:** Attempt LinkedIn API if token allows pulling structured data.

---

## 📤 Export to GitHub Pages

**Goal:** Provide users with a way to deploy a static resume site.

### Steps:
- [ ] Generate static bundle of the resume
- [ ] Preview static version in-app
- [ ] Use OAuth token to push to `gh-pages` branch of user's repo

**Tech options:** VuePress, Astro, Vite SSG, or custom static HTML/CSS generator.

---

## 📢 Share Resume on Socials

**Goal:** Make users’ resumes easy to share for networking/job hunting.

### Features:
- [ ] Read-only public resume mode
- [ ] Share buttons for:
  - [ ] LinkedIn
  - [ ] X / Twitter
  - [ ] WhatsApp
  - [ ] Email
- [ ] Copy link button
- [ ] Optional: QR Code generator

---

## 💾 Export Resume in Various Formats

**Goal:** Allow full control over resume portability.

### Formats:
- [ ] PDF (already supported?)
- [ ] JSON (backup/restore)
- [ ] Markdown (for developers)
- [ ] HTML export (for GitHub Pages or offline use)

---

## 💥 Bonus Polish Tier Ideas

### Public Resume SEO & Analytics:
- [ ] Add OpenGraph + meta tags to public resume
- [ ] Count views/downloads of resume (basic analytics)

### Testimonial Enhancements:
- [ ] Allow users to collect public feedback/testimonials via guestbook-like UI (read-only)

---

## Summary:
ForkMyFolio V2 is about taking what already feels like a finished product and making it:
- Polished for onboarding
- Flexible for importing/exporting
- Shareable for real-world use
- Ready for contributors or hosted deployment

This is the final 10% that will make it feel like a full commercial-grade product.

---

**Next Steps:**
- Start with Google OAuth2
- Work on LinkedIn import parser
- Design static resume generator
- Wire up GitHub Pages deployment
- Style public resume links and sharing options

