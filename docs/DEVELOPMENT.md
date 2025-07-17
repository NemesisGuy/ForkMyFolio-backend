# Local Development Guide

This section will guide you through setting up and running the project locally without Docker.

## Prerequisites

*   Java JDK 21 or later (e.g., OpenJDK, Oracle JDK)
*   Apache Maven 3.6.x or later
*   Git
*   (Optional for `prod` profile) A local PostgreSQL server running.

## Installation & Setup

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/NemesisGuy/ForkMyFolio-backend.git
    cd ForkMyFolio-backend
    ```

2.  **Build the project with Maven:**
    This will download dependencies and compile the source code.
    ```bash
    mvn clean install
    ```

## Running the Application

The application can be run using different Spring profiles.

### Development Profile (H2 Database)

This is the default profile. It uses a temporary, in-memory H2 database.
```bash
mvn spring-boot:run
```
The application will be available at `http://localhost:8080`.
*   **H2 Console**: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:forkmyfolio_dev`, User: `sa`, Password: (empty))

### Production Profile (PostgreSQL)

This profile requires a running PostgreSQL instance and proper configuration via a `secrets.properties` file or environment variables.
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```
Or, after building the JAR:
```bash
java -jar -Dspring.profiles.active=prod target/forkmyfolio-backend-0.0.1-SNAPSHOT.jar
```