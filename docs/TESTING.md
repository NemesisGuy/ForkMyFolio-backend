# Testing Guide

This document provides detailed information on the testing strategy and practices used in the ForkMyFolio-backend project.

## Overview

The project maintains a comprehensive test suite of **94 tests** (as of December 2025) covering core business logic, API endpoints, and data integration. The goal of the test suite is to ensure stability, prevent regressions, and provide documentation by example for the API's behavior.

## Running Tests

To execute the full test suite, run the following command from the project root:

```bash
mvn test
```

### Useful Maven Commands
*   **Run a specific test class**: `mvn test -Dtest=AdminControllerTest`
*   **Run a single test method**: `mvn test -Dtest=AdminControllerTest#getAllUsers_shouldReturnUserPage`
*   **Skip tests temporarily**: `mvn clean install -DskipTests` (Use with caution)

## Test Types

### 1. Unit Tests
Located in `src/test/java/com/forkmyfolio`, these tests use **JUnit 5** and **Mockito** to test individual classes in isolation.
*   **Mappers**: Verify correct entity-to-DTO and DTO-to-entity transformations.
*   **Services**: Verify business logic, handling of edge cases, and correct interaction with mocked repositories.

### 2. Integration Tests
These tests (mostly in the `controller` package) use **Spring MockMvc** to perform end-to-end testing of the REST API boundary without requiring a full running server or external database.
*   **Controllers**: Verify status codes, JSON response structures, and role-based access control.

## Key Configurations & Best Practices

### Handling Java 8 Date/Time
The project uses `LocalDate` for dates. To ensure correct serialization in tests, the `ObjectMapper` must be registered with the `JavaTimeModule`:

```java
@BeforeEach
void setUp() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    // ... setup MockMvc
}
```

### Pagination with MockMvc
When testing paginated endpoints, ensure that `PageableHandlerMethodArgumentResolver` is provided to `standaloneSetup`:

```java
mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
        .build();
```

Use `PageRequest.of(page, size)` in mock setups to avoid serialization issues with `Unpaged` objects.

### ApiResponseWrapper Structure
All controller responses are wrapped in `ApiResponseWrapper<T>`. When asserting JSON paths, remember the `.data` prefix:

```java
.andExpect(jsonPath("$.data.content[0].firstName").value("Admin"))
```

### Mocking Nested Collections
When mocking DTOs used in restore operations, always initialize nested collections to prevent `NullPointerException` during stream operations:

```java
projectDto.setSkills(Collections.emptySet());
```

## Coverage Goals
Maintain high coverage for:
*   Critical business logic in services.
*   Security constraints on admin and user endpoints.
*   Complex data mapping and enrichment.
