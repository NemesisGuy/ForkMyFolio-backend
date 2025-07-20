package com.forkmyfolio;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Basic integration tests for the ForkMyFolioBackendApplication.
 * Verifies that the application context loads and basic endpoints are accessible.
 */
@SpringBootTest
@AutoConfigureMockMvc // To enable MockMvc for testing web layers
@ActiveProfiles("dev") // Use the 'dev' profile for tests (H2 database, etc.)
public class ForkMyFolioBackendApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Tests if the Spring application context loads successfully.
     * This is a fundamental check to ensure all beans are initialized correctly.
     */
    @Test
    void contextLoads() {
        // If the test runner reaches here and autowiring works, the context has loaded.
        // No explicit assertions needed for this specific test method name if it completes.
    }

    /**
     * Tests if the Swagger UI HTML page is accessible.
     * This verifies that Spring MVC is configured and Springdoc OpenAPI is serving content.
     *
     * @throws Exception if MockMvc performance fails.
     */
    @Test
    void swaggerUiIsAccessible() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }

    /**
     * Tests if the Swagger UI index page is accessible after redirection.
     *
     * @throws Exception if MockMvc performance fails.
     */
    @Test
    void swaggerUiIndexIsAccessible() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    /**
     * Tests if the OpenAPI documentation endpoint (api-docs) is accessible and returns JSON.
     * This verifies the core OpenAPI documentation generation.
     *
     * @throws Exception if MockMvc performance fails.
     */
    @Test
    void apiDocsIsAccessibleAndReturnsJson() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string(containsString("\"openapi\":\"3.0.1\""))) // Basic check for OpenAPI spec
                .andExpect(content().string(containsString("\"title\":\"ForkMyFolio API\""))); // Check for custom title
    }

    /**
     * Tests that an unauthenticated request to a secured endpoint (e.g., /api/v1/users/me/profile)
     * results in an HTTP 401 Unauthorized status.
     * This verifies that Spring Security is protecting endpoints as expected.
     *
     * @throws Exception if MockMvc performance fails.
     */
    @Test
    void accessSecuredEndpointWithoutAuthShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/me/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("unauthorized")))
                .andExpect(jsonPath("$.errors[0].field", is("authentication")))
                .andExpect(jsonPath("$.errors[0].message", containsString("Full authentication is required")));
    }
}
