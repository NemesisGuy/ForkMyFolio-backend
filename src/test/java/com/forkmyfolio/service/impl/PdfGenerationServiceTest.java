package com.forkmyfolio.service.impl;

import com.forkmyfolio.model.*;
import com.forkmyfolio.repository.*;
import com.forkmyfolio.service.PortfolioProfileService;
import com.forkmyfolio.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfGenerationServiceTest {

        @Mock
        private ExperienceRepository experienceRepository;
        @Mock
        private ProjectRepository projectRepository;
        @Mock
        private QualificationRepository qualificationRepository;
        @Mock
        private PortfolioProfileService portfolioProfileService;
        @Mock
        private SettingRepository settingRepository;
        @Mock
        private UserService userService;

        @InjectMocks
        private PdfGenerationService pdfGenerationService;

        private User testUser;

        @BeforeEach
        void setUp() {
                testUser = new User();
                testUser.setId(1L);
                testUser.setSlug("test-user");
                testUser.setFirstName("Test");
                testUser.setLastName("User");
                testUser.setEmail("test@example.com");
                testUser.setUserSkills(new HashSet<>());
                testUser.setExperiences(new HashSet<>());
                testUser.setProjects(new HashSet<>());
                testUser.setQualifications(new HashSet<>());
                testUser.setUserSettings(new HashSet<>());
        }

        @Test
        void generatePortfolioPdf_shouldGeneratePdfWithValidData() {
                // Arrange
                PortfolioProfile profile = new PortfolioProfile();
                profile.setUser(testUser);
                profile.setHeadline("Software Engineer");
                profile.setSummary("Experienced developer.");

                when(userService.findBySlugWithAllPortfolioData(anyString())).thenReturn(Optional.of(testUser));
                when(portfolioProfileService.getProfileByUser(any(User.class))).thenReturn(profile);
                when(experienceRepository.findByUserOrderByDisplayOrderAsc(any(User.class)))
                                .thenReturn(Collections.emptyList());
                when(projectRepository.findByUserOrderByDisplayOrderAsc(any(User.class)))
                                .thenReturn(Collections.emptyList());
                when(qualificationRepository.findByUserOrderByCompletionYearDescStartYearDesc(any(User.class)))
                                .thenReturn(Collections.emptyList());

                // Act
                PdfGenerationService.PdfFile pdfFile = pdfGenerationService.generatePortfolioPdf(testUser, "modern");

                // Assert
                assertNotNull(pdfFile);
                assertNotNull(pdfFile.content());
                assertTrue(pdfFile.content().length > 0);
                assertTrue(pdfFile.suggestedFilename().endsWith(".pdf"));
        }

        @Test
        void generatePortfolioPdf_shouldFallbackToDefaultTemplateIfNotFound() {
                // Arrange
                PortfolioProfile profile = new PortfolioProfile();
                profile.setUser(testUser);
                profile.setHeadline("Software Engineer");
                profile.setSummary("Experienced developer.");
                when(userService.findBySlugWithAllPortfolioData(anyString())).thenReturn(Optional.of(testUser));
                when(portfolioProfileService.getProfileByUser(any(User.class))).thenReturn(profile);
                when(experienceRepository.findByUserOrderByDisplayOrderAsc(any(User.class)))
                                .thenReturn(Collections.emptyList());
                when(projectRepository.findByUserOrderByDisplayOrderAsc(any(User.class)))
                                .thenReturn(Collections.emptyList());
                when(qualificationRepository.findByUserOrderByCompletionYearDescStartYearDesc(any(User.class)))
                                .thenReturn(Collections.emptyList());

                // Act
                PdfGenerationService.PdfFile pdfFile = pdfGenerationService.generatePortfolioPdf(testUser,
                                "non-existent-template");

                // Assert
                assertNotNull(pdfFile);
                assertTrue(pdfFile.content().length > 0);
        }
}
