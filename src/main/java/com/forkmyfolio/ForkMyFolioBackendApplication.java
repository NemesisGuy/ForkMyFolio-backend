package com.forkmyfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the ForkMyFolio Backend.
 * This class initializes and runs the Spring Boot application.
 * OpenAPI documentation configuration is centralized in {@link com.forkmyfolio.config.OpenApiConfig}.
 */
@SpringBootApplication
public class ForkMyFolioBackendApplication {

    /**
     * Main method to start the Spring Boot application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ForkMyFolioBackendApplication.class, args);
    }

}
