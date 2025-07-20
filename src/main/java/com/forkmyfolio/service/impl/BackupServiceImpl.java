package com.forkmyfolio.service.impl;

import com.forkmyfolio.model.User;
import com.forkmyfolio.service.*;
import com.forkmyfolio.service.model.PortfolioBackupData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BackupServiceImpl implements BackupService {

    // Services to fetch data
    private final UserService userService;
    private final PortfolioProfileService portfolioProfileService;
    private final ProjectService projectService;
    private final SkillService skillService;
    private final ExperienceService experienceService;
    private final TestimonialService testimonialService;
    private final QualificationService qualificationService;

    @Override
    @Transactional(readOnly = true)
    public PortfolioBackupData createBackupForCurrentUser() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        PortfolioBackupData backupData = new PortfolioBackupData();

        // 1. Get Profile
        backupData.setProfile(portfolioProfileService.getProfileByUser(currentUser));

        // 2. Get Projects
        backupData.setProjects(projectService.getPublicProjects());

        // 3. Get Skills
        backupData.setSkills(skillService.getPublicSkills());

        // 4. Get Experiences
        backupData.setExperiences(experienceService.getPublicExperience());

        // 5. Get Testimonials
        backupData.setTestimonials(testimonialService.getPublicTestimonials());

        // 6. Get Qualifications
        backupData.setQualifications(qualificationService.getPublicQualifications());

        return backupData;
    }
}