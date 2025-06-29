# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-jammy as builder

# Set the working directory
WORKDIR /app

# Copy the Maven wrapper and pom.xml to leverage Docker cache
# COPY mvnw .
# COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
# RUN ./mvnw dependency:go-offline
RUN mvn dependency:go-offline -B

# Copy the rest of the source code
COPY src ./src

# Package the application
# RUN ./mvnw package -DskipTests
RUN mvn package -DskipTests -B

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre-jammy

# Set the working directory
WORKDIR /app

# Copy the JAR file from the builder stage
COPY --from=builder /app/target/forkmyfolio-backend-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# Set the entrypoint for the application
# Default to 'dev' portfolioProfile if SPRING_PROFILES_ACTIVE is not set.
# For production, an environment variable SPRING_PROFILES_ACTIVE=prod should be passed.
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}", "-jar", "app.jar"]

# Optional: Add a non-root user for security best practices
# USER appuser:appgroup
# (This would require creating the user and group first, e.g., in the builder stage or here)
# RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
# USER appuser

# Healthcheck (Optional but recommended)
# HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
#   CMD curl -f http://localhost:8080/actuator/health || exit 1
# (Requires actuator/health to be enabled and accessible)
