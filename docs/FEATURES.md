﻿# Project Features & Capabilities

This document outlines the key features and technical capabilities of the ForkMyFolio application, encompassing both the backend API and the frontend client.

## 🏛️ Platform & Architecture

- **Modern Tech Stack**: Built with Java 21, Spring Boot 3, and a Vue 3 frontend for a robust and high-performance application.
- **RESTful API**: A clean, well-structured API serves all data to the frontend, with clear endpoints under the `/api/v1/` path.
- **JPA/Hibernate ORM**: Manages all database interactions, mapping Java objects to MySQL database tables.
- **Centralized Exception Handling**: A global exception handler provides consistent and well-formatted error responses to the client.
- **Dockerized for Deployment**: The entire stack (backend, frontend, database) is containerized and orchestrated with Docker Compose for simple, one-command deployment.

## 🔐 Security

- **JWT-Based Authentication**: Secures the application using stateless JSON Web Tokens.
- **Secure Refresh Token Strategy**: Implements `HttpOnly` cookies for refresh tokens, protecting them from client-side script access and mitigating XSS risks.
- **Role-Based Access Control (RBAC)**: Clear distinction between public `permitAll()` endpoints and protected `/admin/**` routes, which require admin privileges.
- **Password Encryption**: All user passwords are securely hashed using Spring Security's standard `BCryptPasswordEncoder`.

## 📊 Visitor Analytics & Tracking

- **Non-Intrusive Tracking**: Visitor metrics are captured using a system that keeps tracking logic completely separate from core business logic.
- **Admin Activity Isolation**: The system accurately distinguishes between public visitors and the logged-in admin, ensuring administrative actions do not inflate public engagement metrics.
- **Comprehensive Event Tracking**:
  - **Page/Section Views**: Tracks views for the main profile, projects list, skills, experience, qualifications, and testimonials sections.
  - **Engagement Events**: Monitors contact form submissions and PDF resume downloads.
  - **Individual Project Views**: Tracks views for each specific project by its UUID.
- **Authentication Event Monitoring**:
  - Automatically logs successful and failed login attempts for security auditing.

## 🖥️ Content Management System (Admin Panel)

- **Full CRUD Operations**: Provides a complete admin interface for managing all portfolio content:
  - **Profile**: Update personal details, summary, and social links.
  - **Projects**: Create, read, update, and delete projects, including image uploads.
  - **Skills**: Manage the list of skills and their categorizations.
  - **Experience**: Add and edit work history.
  - **Qualifications**: Maintain a list of degrees and certifications.
  - **Testimonials**: Curate quotes and recommendations.
- **Contact Message Inbox**: A simple interface to view and manage messages submitted through the public contact form.
- **Dynamic Application Settings**: A centralized location to control application-wide settings, such as toggling the visibility of entire portfolio sections on the public site.
- **Account Management**: Admins can update their private user details (name, email, etc.).

## ✨ User Experience (Vue.js Frontend)

- **Single-Page Application (SPA)**: A modern, reactive, and fast user interface built with Vue 3 and Vite.
- **Dynamic Content**: All content displayed on the public portfolio is fetched dynamically from the backend API, allowing for instant updates without redeploying the frontend.
- **Seamless Navigation**: Uses Vue Router for a smooth user experience without full page reloads.
- **PDF Resume Generation**: Allows visitors to download a neatly formatted PDF version of the portfolio on-the-fly.
- **Secure Admin Dashboard**: A protected section of the application with a clean, intuitive interface for all content management and analytics viewing.