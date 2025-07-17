# ForkMyFolio - Docker Compose Deployment Guide

This guide provides step-by-step instructions for deploying the entire ForkMyFolio application stack (Frontend, Backend, and Database) using Docker Compose.

This setup uses the official, pre-built Docker images from Docker Hub and the deployment configuration from the official GitHub repository.

- **Backend Repo**: [NemesisGuy/ForkMyFolio-backend on GitHub](https://github.com/NemesisGuy/ForkMyFolio-backend)
- **Frontend Repo**: [NemesisGuy/ForkMyFolio-frontend on GitHub](https://github.com/NemesisGuy/ForkMyFolio-frontend)

This setup is ideal for local development, testing, or a simple server deployment.

## Table of Contents
- [Prerequisites](#prerequisites)
- Step 1: Get the Deployment Files
- Step 2: Create the Environment File (`.env`)
- Step 3: Understanding Environment Variables
- Step 4: Running the Application
- Step 5: Accessing the Application
- Step 6: Managing the Stack

---

## Prerequisites

Before you begin, ensure you have the following installed on your system:
- **Docker**: Get Docker
- **Docker Compose**: Included with Docker Desktop. For Linux servers, you may need to install it separately.

---

## Step 1: Get the Deployment Files

First, clone the backend repository which contains the `docker-compose.yaml` file needed to orchestrate the services.

```bash
git clone https://github.com/NemesisGuy/ForkMyFolio-backend.git
cd ForkMyFolio-backend
```

---

## Step 2: Create the Environment File (`.env`)

Docker Compose uses an `.env` file in the same directory as the `docker-compose.yaml` file to manage secrets and configuration.

Create a file named `.env` and paste the following content into it. You **must** fill in the placeholder values.

```bash
# .env - Configuration for ForkMyFolio Docker Stack

########### DATABASE CREDENTIALS ###########
# For this Docker Compose setup, the application connects to the database as the 'root' user.
DB_USERNAME=root
# This password is for the 'root' user of the MySQL container.
# It is used by both the backend service to connect and the database service to initialize.
DB_PASSWORD=YourStrong_Db_Password123

########### BACKEND SECURITY ###########
# A long, random, secret string for signing JWTs.
# You can generate one here: https://www.uuidgenerator.net/version4
JWT_SECRET_KEY=YourSuperSecret_Long_And_Random_JwtKey_Here

# The initial password for the default admin user ('admin@forkmyfolio.com').
DEFAULT_ADMIN_PASSWORD=YourSecure_Admin_Password123

########### FRONTEND <-> BACKEND COMMUNICATION ###########
# The public URL where the frontend will be accessible.
# This is CRITICAL for CORS security on the backend.
# For this local deployment guide, this matches the port exposed by the frontend service.
APP_CORS_ALLOWED_ORIGINS=http://localhost:8089

# The public URL where the backend API can be reached.
# This is CRITICAL for the frontend to make API calls.
# For this local deployment guide, this matches the port exposed by the backend service.
API_BASE_URL=http://localhost:8080/api/v1
```

---

## Step 2: Understanding Environment Variables

It is crucial to understand what each variable does.

| Variable                 | Service(s) Used By | Description                                                                                                                                                            |
| :----------------------- | :----------------- | :--------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `DB_PASSWORD`            | Backend, Database  | The **root password** for the MySQL container. The backend uses this to connect. **This must be a strong, secret value.**                                                |
| `JWT_SECRET_KEY`         | Backend            | The secret key used to sign and verify JSON Web Tokens. **This must be a long, random, and securely stored secret.**                                                   |
| `DEFAULT_ADMIN_PASSWORD` | Backend            | On the very first startup, the backend will create a default user `admin@forkmyfolio.com` with this password.                                                            |
| `APP_CORS_ALLOWED_ORIGINS`| Backend            | A security feature (CORS) that tells the backend to only accept API requests from this URL. This should be the full URL of your frontend.                               |
| `API_BASE_URL`           | Frontend           | The full, public-facing URL of the backend API. The frontend's JavaScript (running in the user's browser) will send all requests to this address.                         |

---

## Step 3: Running the Application

With your `docker-compose.yaml` and your completed `.env` file in the same directory, open a terminal in that directory and run:

```bash
# This command will pull the images, create the network and volumes,
# and start all three services in the background.
docker-compose up -d
```

The first time you run this, it will take a few minutes to download the Docker images from Docker Hub.

---

## Step 4: Accessing the Application

- **Frontend Application**:
  Open your web browser and navigate to the URL you set in `APP_CORS_ALLOWED_ORIGINS`.
  > **http://localhost:8089**

- **Backend API Documentation (Swagger UI)**:
  You can view all the available API endpoints by navigating to:
  > **http://localhost:8080/swagger-ui.html**

---

## Step 5: Managing the Stack

- **To see the live logs for all services:**
  ```bash
  docker-compose logs -f
  ```

- **To stop the services without removing them:**
  ```bash
  docker-compose stop
  ```

- **To stop and remove the containers, network, and volumes:**
  ```bash
  docker-compose down -v
  ```