# Job Application Tracker — Specification (ForkMyFolio v2)

> **Purpose:** Empower users to track their job hunt efforts directly within ForkMyFolio. No more spreadsheets — fully integrated with their dev profile.

---

## 🌐 Overview

The Job Application Tracker is a **user-authenticated feature** that allows logged-in users to:

- Add job applications manually (or later via API/browser extension)
- Track statuses like: `Applied`, `Interviewing`, `Offer`, `Rejected`, `Withdrawn`
- Log notes, resume versions used, links, contact persons, and timelines
- Analyze job hunt metrics over time (conversion rates, offer ratios, app volume)

## 👤 User Stories

- "As a user, I want to log job applications so I can track my job hunt."
- "As a user, I want to update the status of my applications."
- "As a user, I want to write private notes per application."
- "As a user, I want to view insights about my applications over time."
- "As a user, I want to export all my job applications to JSON or CSV."

## 🔄 Lifecycle Statuses

```
[DRAFT] → [APPLIED] → [INTERVIEWING] → [OFFER] → [ACCEPTED] / [REJECTED] / [WITHDRAWN]
```

Each status should have a timestamp of when it was set.

## 📋 Entity Design

### JobApplication

```java
UUID id;
User owner;
String jobTitle;
String companyName;
String jobLocation;
String jobType; // e.g., Full-Time, Contract, Internship
String applicationUrl;
String resumeVersion;
ApplicationStatus status; // Enum
LocalDateTime statusUpdatedAt;
LocalDateTime appliedAt;
List<JobApplicationNote> notes;
boolean visible; // For UI toggling
```

### JobApplicationNote

```java
UUID id;
JobApplication jobApplication;
String content;
LocalDateTime createdAt;
```

### ApplicationStatus (Enum)

```java
DRAFT, APPLIED, INTERVIEWING, OFFER, ACCEPTED, REJECTED, WITHDRAWN
```

## 🚀 Features

- ✅ Add/edit/delete job applications
- ✅ Add notes to application timeline
- ✅ Filter/sort/search (by company, title, status, date)
- ✅ Export to JSON (later CSV)
- ✅ Visibility toggle (private/public if ever needed)
- ✅ Graphs & stats dashboard (later, using Grafana or custom Vue charts)

## 🛠️ Planned UX Components

- Job Tracker section in user dashboard
- Form to add/update an application
- Timeline view for application events
- Tag chips for status
- Badge counters for number in each phase
- Optional: Markdown support in notes

## ⚖️ Permissions

- Only the logged-in user can view/edit their job applications
- Admins may view for moderation/debug but not edit

## 🔮 Future Ideas

- 👀 GuestPass support (let recruiters see status page)
- 📅 Calendar integration (interview dates, follow-ups)
- 📎 Upload job description PDF or screenshot
- 📈 Application outcome analytics (time to response, etc.)
- ✅ Browser extension to clip job postings into app tracker

---

> **Conclusion:** The Job Application Tracker is a focused productivity tool to help users manage their job hunt inside their dev ecosystem.

This gives ForkMyFolio an edge beyond portfolios — it becomes a self-hosted **career operations tool.**

