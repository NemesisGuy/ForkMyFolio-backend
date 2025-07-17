# Standalone Docker Guide

This document provides instructions for building and running the backend as a standalone Docker container, separate from the full `docker-compose` stack.

## Building the Docker Image

The project includes a multi-stage `Dockerfile` for optimized, small production images.

To build the Docker image, navigate to the project root directory and run:
```bash
docker build -t forkmyfolio-backend .
```
Or, to specify a version tag matching your Docker Hub repository:
```bash
docker build -t nemesisguy/forkmyfolio-backend:latest .
```

## Running the Docker Container

When running the container, you must provide the necessary environment variables, especially for a production profile.

### Production Profile Example
This example assumes you have an external PostgreSQL database.
```bash
docker run -d -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/your-db-name \
  -e DB_USERNAME=your-db-user \
  -e DB_PASSWORD=your-db-password \
  -e JWT_SECRET_KEY=your-super-strong-and-secret-jwt-key \
  -e APP_CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com \
  -e DEFAULT_ADMIN_PASSWORD=a-secure-admin-password \
  --name forkmyfolio-backend-prod \
  nemesisguy/forkmyfolio-backend:latest
```
Replace the placeholder values with your actual production configuration.

### Development Profile Example
This will run the container using the default `dev` profile, which uses an in-memory H2 database.
```bash
docker run -d -p 8080:8080 --name forkmyfolio-backend-dev nemesisguy/forkmyfolio-backend:latest
```