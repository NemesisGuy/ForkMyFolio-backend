# ForkMyFolio Backend API

ForkMyFolio is a digital portfolio platform designed to help developers and creative professionals showcase their work. This repository contains the source code for its modern, secure, and feature-rich Spring Boot REST API backend.

The platform provides a dynamic, single-page application experience for public visitors, and a comprehensive admin panel for content management, live site configuration, and visitor analytics.

For detailed technical information, setup instructions, and API documentation, please see our [**Technical Documentation**](docs/old-docs/TECHNICAL_DOCUMENTATION.md) and our [**Testing Guide**](docs/TESTING.md).

---

## ✨ Key Features

### 🏛️ Platform & Architecture
- **Modern Tech Stack**: Built with Java 21 and Spring Boot 3 for a robust and high-performance application.
- **RESTful API**: A clean, well-structured API serves all data to the frontend.
- **Dockerized for Deployment**: The entire stack is containerized for simple, one-command deployment.

### 🔐 Security
- **JWT-Based Authentication**: Secures the application using stateless JSON Web Tokens with a secure refresh token strategy.
- **Role-Based Access Control (RBAC)**: Clear distinction between public and protected admin routes.
- **Password Encryption**: All user passwords are securely hashed.

### 🖥️ Content Management System (Admin Panel)
- **Full CRUD Operations**: Provides a complete admin interface for managing all portfolio content (Profile, Projects, Skills, Experience, etc.).
- **Contact Message Inbox**: A simple interface to view and manage messages submitted through the public contact form.
- **Live Site Configuration**: Admins can instantly change the public site's behavior, such as toggling section visibility or changing the default PDF template.

### ⚙️ Dynamic Application Settings
- **Flexible Configuration**: The system supports live configuration of the application without redeployment.
- **Dynamic PDF Templates**: Allows the admin to choose which PDF design is used for public downloads.

### 📊 Visitor Analytics & Tracking
- **Non-Intrusive Tracking**: Captures visitor metrics without impacting user experience.
- **Comprehensive Event Tracking**: Tracks page views, engagement events, and individual project views.

### ✨ User Experience
- **Dynamic Content**: All content displayed on the public portfolio is fetched dynamically from the backend API.
- **Dynamic PDF Resume Generation**: Allows visitors to download a PDF version of the portfolio.

---

## 🛠️ Built With

*   **Backend**: Java 21, Spring Boot 3, Spring Security, Spring Data JPA
*   **Database**: MySQL (Production), H2 (Development)
*   **API Documentation**: Springdoc OpenAPI (Swagger UI)
*   **Containerization**: Docker
*   **Authentication**: JWT (JSON Web Tokens)