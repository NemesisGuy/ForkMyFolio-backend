# Deployment Guide

This guide provides instructions for deploying the ForkMyFolio application. You can either deploy the full stack using Docker Compose or run the backend as a standalone Docker container.

---

## Full Stack Deployment with Docker Compose

This is the recommended method for deploying the entire application stack (Frontend, Backend, and Database). This setup uses the official, pre-built Docker images from Docker Hub.

### Prerequisites

-   **Docker**: [Get Docker](https://docs.docker.com/get-docker/)
-   **Docker Compose**: Included with Docker Desktop. For Linux servers, you may need to install it separately.

### Step 1: Get the Deployment Files

Clone the backend repository which contains the `docker-compose.yaml` file:

```bash
git clone https://github.com/NemesisGuy/ForkMyFolio-backend.git
cd ForkMyFolio-backend
```

### Step 2: Create the Environment File (`.env`)

Create a file named `.env` in the same directory as the `docker-compose.yaml` file and paste the following content into it. You **must** fill in the placeholder values.

```bash
# .env - Configuration for ForkMyFolio Docker Stack

########### DATABASE CREDENTIALS ###########
DB_USERNAME=root
DB_PASSWORD=YourStrong_Db_Password123

########### BACKEND SECURITY ###########
JWT_SECRET_KEY=YourSuperSecret_Long_And_Random_JwtKey_Here
DEFAULT_ADMIN_PASSWORD=YourSecure_Admin_Password123

########### FRONTEND <-> BACKEND COMMUNICATION ###########
APP_CORS_ALLOWED_ORIGINS=http://localhost:8089
API_BASE_URL=http://localhost:8080/api/v1
```

### Step 3: Understanding Environment Variables

| Variable                 | Service(s) Used By | Description                                                                                             |
| :----------------------- | :----------------- | :------------------------------------------------------------------------------------------------------ |
| `DB_PASSWORD`            | Backend, Database  | The **root password** for the MySQL container. **This must be a strong, secret value.**                 |
| `JWT_SECRET_KEY`         | Backend            | The secret key used to sign and verify JSON Web Tokens. **This must be a long, random, and securely stored secret.** |
| `DEFAULT_ADMIN_PASSWORD` | Backend            | The initial password for the default admin user (`admin@forkmyfolio.com`).                              |
| `APP_CORS_ALLOWED_ORIGINS`| Backend            | The URL of your frontend, for CORS security.                                                          |
| `API_BASE_URL`           | Frontend           | The full, public-facing URL of the backend API.                                                         |

### Step 4: Running the Application

With your `docker-compose.yaml` and your completed `.env` file in the same directory, run:

```bash
docker-compose up -d
```

### Step 5: Accessing the Application

-   **Frontend Application**: [http://localhost:8089](http://localhost:8089)
-   **Backend API Documentation (Swagger UI)**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Step 6: Managing the Stack

-   **View logs:** `docker-compose logs -f`
-   **Stop services:** `docker-compose stop`
-   **Stop and remove all resources:** `docker-compose down -v`

---

## Standalone Backend Deployment with Docker

This method is for running the backend as a standalone Docker container, for example, to connect to an external database.

### Building the Docker Image

The project includes a multi-stage `Dockerfile` for optimized, small production images. To build the image, run:

```bash
docker build -t forkmyfolio-backend .
```

### Running the Docker Container

You must provide the necessary environment variables when running the container.

#### Production Profile Example (with external PostgreSQL)

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

#### Development Profile Example (with in-memory H2 database)

```bash
docker run -d -p 8080:8080 --name forkmyfolio-backend-dev nemesisguy/forkmyfolio-backend:latest
```
