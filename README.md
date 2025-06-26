# ForkMyFolio Backend API

ForkMyFolio is a digital portfolio platform. This repository contains the source code for its modern Spring Boot REST API backend.

## Table of Contents

- [About The Project](#about-the-project)
- [Features](#features)
- [Built With](#built-with)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation & Setup](#installation--setup)
- [Running the Application](#running-the-application)
  - [Development Profile (H2 Database)](#development-profile-h2-database)
  - [Production Profile (PostgreSQL)](#production-profile-postgresql)
- [API Documentation (Swagger UI)](#api-documentation-swagger-ui)
- [Docker](#docker)
  - [Building the Docker Image](#building-the-docker-image)
  - [Running with Docker](#running-with-docker)
- [Environment Variables](#environment-variables)
- [Code Structure](#code-structure)
- [Testing](#testing)
- [Contributing](#contributing)
- [License](#license)

## About The Project

This project provides the backend services for ForkMyFolio. It handles user authentication, portfolio project management, skills tracking, and contact messaging. The API is designed to be RESTful, secure, and easily consumable by a frontend application.

## Features

- User registration and JWT-based authentication (Login/Logout).
- Role-based access control (USER, ADMIN).
- CRUD operations for Projects (Admin only for CUD).
- CRUD operations for Skills (Admin only for CUD).
- Submission of Contact Messages.
- Auto-generated API documentation via Swagger/OpenAPI.
- Docker support for containerization.
- Separate configurations for Development (H2) and Production (PostgreSQL) environments.

## Built With

*   [Spring Boot](https://spring.io/projects/spring-boot) (v3.2.5)
*   [Spring Security](https://spring.io/projects/spring-security) (JWT)
*   [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
*   [Java 21](https://www.oracle.com/java/technologies/javase/21-relnote-issues.html)
*   [Maven](https://maven.apache.org/)
*   [H2 Database](https://www.h2database.com) (for Development)
*   [PostgreSQL](https://www.postgresql.org/) (for Production)
*   [Lombok](https://projectlombok.org/)
*   [Springdoc OpenAPI](https://springdoc.org/) (for Swagger UI)
*   [JJWT](https://github.com/jwtk/jjwt) (for JWT handling)
*   [Docker](https://www.docker.com/)

## Getting Started

This section will guide you through setting up the project locally.

### Prerequisites

*   Java JDK 21 or later (e.g., OpenJDK, Oracle JDK)
*   Apache Maven 3.6.x or later
*   Git
*   (Optional for Production profile) PostgreSQL server running
*   (Optional) Docker Desktop

### Installation & Setup

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/your-username/forkmyfolio-backend.git
    cd forkmyfolio-backend
    ```
    *(Note: Replace `your-username` with the actual path if applicable)*

2.  **Build the project with Maven:**
    This will download dependencies and compile the source code.
    ```bash
    mvn clean install
    ```

## Running the Application

The application can be run using different Spring profiles.

### Development Profile (H2 Database)

This is the default profile. It uses an in-memory H2 database.
```bash
mvn spring-boot:run
```
Or, after building the JAR:
```bash
java -jar target/forkmyfolio-backend-0.0.1-SNAPSHOT.jar
```
The application will be available at `http://localhost:8080`.
H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:forkmyfolio_dev`, User: `sa`, Password: (empty))

### Production Profile (PostgreSQL)

This profile requires a running PostgreSQL instance and proper configuration (see [Environment Variables](#environment-variables)).
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```
Or, after building the JAR:
```bash
java -jar -Dspring.profiles.active=prod target/forkmyfolio-backend-0.0.1-SNAPSHOT.jar
```

## API Documentation (Swagger UI)

Once the application is running, the Swagger UI documentation can be accessed at:
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

The OpenAPI specification (JSON) is available at:
[http://localhost:8080/api-docs](http://localhost:8080/api-docs)

You can use the "Authorize" button on Swagger UI (top right) to authenticate using a JWT token obtained from the `/auth/login` or `/auth/register` endpoints. The format is `Bearer <your_jwt_token>`.

## Docker

This section will be updated with Docker build and run instructions once the `Dockerfile` is finalized.

### Building the Docker Image
```bash
# Placeholder: Instructions to come
```

### Running with Docker
```bash
# Placeholder: Instructions to come
```

## Environment Variables

For the **production profile**, the following environment variables should be configured:

*   `SPRING_DATASOURCE_URL`: The JDBC URL for your PostgreSQL database.
    *   Example: `jdbc:postgresql://your-db-host:5432/your-db-name`
*   `SPRING_DATASOURCE_USERNAME`: The username for your PostgreSQL database.
*   `SPRING_DATASOURCE_PASSWORD`: The password for your PostgreSQL database.
*   `JWT_SECRET`: A strong, base64-encoded secret key for signing JWTs. This **MUST** be overridden from the default.
    *   You can generate a suitable secret using various tools. Ensure it's sufficiently long and random.
*   `JWT_EXPIRATION_MS`: (Optional) JWT expiration time in milliseconds. Defaults to `86400000` (24 hours).

These variables can be set directly in your deployment environment or using a `.env` file if your deployment method supports it (Docker Compose, etc.).

For the **development profile**, these are not strictly necessary as it defaults to H2 and a default JWT secret (which is insecure and for development only).

## Code Structure

*(A brief overview of the main packages will be added here)*
- `com.forkmyfolio.config`: Spring configuration classes (Security, OpenAPI).
- `com.forkmyfolio.controller`: REST API controllers.
- `com.forkmyfolio.dto`: Data Transfer Objects for API requests/responses.
- `com.forkmyfolio.exception`: Custom exceptions and global exception handler.
- `com.forkmyfolio.model`: JPA entities representing the domain model.
- `com.forkmyfolio.repository`: Spring Data JPA repositories.
- `com.forkmyfolio.security`: JWT utilities, UserDetailsService, security filters.
- `com.forkmyfolio.service`: Service layer interfaces and implementations.

## Testing

*(Information on how to run unit and integration tests will be added here)*
```bash
mvn test
```

## Contributing

Contributions are welcome! Please adhere to the project's coding standards and practices.
*(More details can be added here if this were a public project: fork, branch, PR process)*

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details (if one were present).
*(Assuming Apache 2.0 based on typical Spring Boot project licenses and OpenAPI config)*
