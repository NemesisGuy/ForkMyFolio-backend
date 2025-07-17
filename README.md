# ForkMyFolio Backend API

ForkMyFolio is a digital portfolio platform. This repository contains the source code for its modern, secure, and performant Spring Boot REST API backend.

## About The Project

This project provides the backend services for ForkMyFolio. It handles user authentication, portfolio project management, skills tracking, and contact messaging. The API is designed to be RESTful, secure, and easily consumable by a frontend application.

## Key Features

-   **Secure Authentication**: Implements JWT-based authentication with short-lived access tokens and long-lived, HttpOnly refresh tokens for robust security.
-   **Role-Based Access Control**: Differentiates between `USER` and `ADMIN` roles, restricting access to sensitive operations.
-   **CRUD Operations**: Provides full CRUD (Create, Read, Update, Delete) functionality for core portfolio content like Projects, Skills, and more.
-   **Comprehensive API Documentation**: Includes interactive Swagger UI and detailed Markdown documents.
-   **Containerized**: Full Docker and Docker Compose support for easy deployment and scalability.
-   **Dual Database Support**: Configured for both H2 (development) and PostgreSQL (production) environments.

## Technology Stack

*   **Backend**: Java 21, Spring Boot 3, Spring Security, Spring Data JPA
*   **Database**: PostgreSQL, H2 (for development)
*   **Authentication**: JWT (JSON Web Tokens)
*   **Build Tool**: Maven
*   **Containerization**: Docker

## Documentation

-   **[API Documentation](docs/API_DOCUMENTATION.md)**: A comprehensive overview of the API, including its structure, endpoints, and authentication flow.
-   **[Codebase Overview](docs/CODEBASE_OVERVIEW.md)**: A detailed explanation of the project's structure.
-   **[Development Guide](docs/DEVELOPMENT.md)**: Instructions for setting up and running the project locally.
-   **[Docker Guide](docs/DOCKER.md)**: Instructions for building and running the project with Docker.
-   **[Swagger UI (Interactive)](http://localhost:8080/swagger-ui.html)**: Interactive API documentation.

## License

This project is licensed under the Apache License 2.0.
