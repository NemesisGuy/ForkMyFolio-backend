package com.forkmyfolio.config;

import com.forkmyfolio.model.Role; // Your enum: e.g., com.forkmyfolio.model.Role
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Initializes the database with essential data on application startup.
 * This runner ensures that a default admin user exists, allowing for
 * initial login and portfolio setup. It's designed to run once and
 * will not create duplicate data on subsequent startups.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment; // To get properties

    @Autowired
    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, Environment environment) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("DataInitializer is running to ensure essential data exists...");

        // Since your roles are enums, they don't need to be saved to a separate table.
        // We just need to create the admin user.

        createDefaultAdminUser();

        logger.info("DataInitializer has finished.");
    }

    private void createDefaultAdminUser() {
        // We check if any user has the ADMIN role.
        // This requires a new method in the UserRepository.
        if (userRepository.existsByRolesContains(Role.ADMIN)) {
            logger.info("An admin user already exists. Skipping default admin creation.");
            return;
        }

        logger.warn("!!! NO ADMIN USER FOUND !!!");
        logger.warn("Creating a default admin user with credentials from application.properties.");
        logger.warn("It is highly recommended to change this password after first login.");

        // Fetch credentials from application.properties to avoid hardcoding them.
        String adminEmail = environment.getProperty("app.default-admin.email", "admin@forkmyfolio.com");
        String adminPassword = environment.getProperty("app.default-admin.password", "password123");
        String adminFirstName = environment.getProperty("app.default-admin.first-name", "Admin");
        String adminLastName = environment.getProperty("app.default-admin.last-name", "User");

        if ("password123".equals(adminPassword)) {
            logger.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            logger.error("!!! CRITICAL SECURITY WARNING: Default admin password is not set in properties. !!!");
            logger.error("!!! Using a highly insecure default. Please set 'app.default-admin.password'.   !!!");
            logger.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }

        User adminUser = new User();
        adminUser.setEmail(adminEmail);
        adminUser.setPassword(passwordEncoder.encode(adminPassword));
        adminUser.setFirstName(adminFirstName);
        adminUser.setLastName(adminLastName);

        // The portfolio owner should have both ADMIN and USER roles.
        adminUser.setRoles(Set.of(Role.ADMIN, Role.USER));

        userRepository.save(adminUser);
        logger.info("Successfully created default admin user with email: {}", adminEmail);
    }
}