package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.response.UserFullBackupDto;
import com.forkmyfolio.model.*;
import com.forkmyfolio.model.enums.AuthProvider;
import com.forkmyfolio.model.enums.Role;
import com.forkmyfolio.repository.UserRepository;
import com.forkmyfolio.service.BackupService;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:mysql://localhost:3308/forkmyfolio_test_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
        "spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver",
        "spring.datasource.username=root",
        "spring.datasource.password=RootPassword",
        "spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect",
        "spring.jpa.hibernate.ddl-auto=create",
        "spring.flyway.enabled=false",
        "spring.datasource.hikari.is-read-only=false",
        "spring.jpa.properties.hibernate.connection.readOnly=false",
        "logging.level.org.hibernate.SQL=DEBUG"
})
@ActiveProfiles("test")
public class StagedLoadingIntegrationTest {

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private com.forkmyfolio.config.startup.DataInitializer dataInitializer;

    @Autowired
    private BackupService backupService;

    @Autowired
    private UserRepository userRepository;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    @BeforeEach
    @org.springframework.transaction.annotation.Transactional(readOnly = false)
    void setUp() {
        // Clear existing test data to avoid unique constraint violations
        userRepository.deleteAll();

        // Create a user with some related data
        User user = new User();
        user.setEmail("test-integration@example.com");
        user.setFirstName("Test");
        user.setLastName("Integration");
        user.setSlug("test-integration");
        user.setPassword("password");
        user.setProvider(AuthProvider.LOCAL);
        user.setRoles(Set.of(Role.USER));

        PortfolioProfile profile = new PortfolioProfile();
        profile.setSummary("Test Bio");
        profile.setUser(user);
        user.setPortfolioProfile(profile);

        Project project = new Project();
        project.setTitle("Test Project");
        project.setDescription("This is a test project description with enough length.");
        project.setUser(user);
        user.setProjects(Set.of(project));
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @Transactional(readOnly = true)
    void stagedLoading_shouldLoadCollectionsOnDemand() {
        // Fetch user using the optimized "ForBackup" query
        List<User> users = userRepository.findAllForBackup();
        User user = users.stream()
                .filter(u -> u.getEmail().equals("test-integration@example.com"))
                .findFirst()
                .orElseThrow();

        // Verify that profile is loaded (it's EAGER or part of the specific query)
        assertNotNull(user.getPortfolioProfile());

        // Verify that projects collection is NOT loaded yet (Lazy)
        assertFalse(Hibernate.isInitialized(user.getProjects()), "Projects should be lazy-loaded");

        // Now touch the projects collection
        int projectCount = user.getProjects().size();
        assertEquals(1, projectCount);

        // Now it should be initialized
        assertTrue(Hibernate.isInitialized(user.getProjects()), "Projects should be initialized after access");
    }

    @Test
    void fullSystemBackup_shouldSucceedWithLazyCollections() {
        // This test runs OUTSIDE of a manual @Transactional at the test level
        // to verify that the @Transactional inside BackupServiceImpl works correctly.

        List<UserFullBackupDto> backup = backupService.createFullSystemBackup();

        UserFullBackupDto testUserBackup = backup.stream()
                .filter(b -> b.getUser().getEmail().equals("test-integration@example.com"))
                .findFirst()
                .orElseThrow();

        assertNotNull(testUserBackup.getPortfolio());
        assertEquals(1, testUserBackup.getPortfolio().getProjects().size());
        assertEquals("Test Project", testUserBackup.getPortfolio().getProjects().get(0).getTitle());
    }
}
