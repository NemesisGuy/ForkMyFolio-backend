package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.response.PortfolioBackupDto;
import com.forkmyfolio.mapper.*;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.*;
import com.forkmyfolio.service.RestoreService;
import com.forkmyfolio.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RestoreServiceImpl implements RestoreService {

    private final UserService userService;
    private final PortfolioProfileRepository portfolioProfileRepository;
    private final ProjectRepository projectRepository;
    private final SkillRepository skillRepository;
    private final ExperienceRepository experienceRepository;
    private final TestimonialRepository testimonialRepository;
    private final QualificationRepository qualificationRepository;

    // Mappers
    private final PortfolioProfileMapper portfolioProfileMapper;
    private final ProjectMapper projectMapper;
    private final SkillMapper skillMapper;
    private final ExperienceMapper experienceMapper;
    private final TestimonialMapper testimonialMapper;
    private final QualificationMapper qualificationMapper;

    @Override
    @Transactional
    public void restoreFromBackup(PortfolioBackupDto backupDto) {
        User currentUser = userService.getCurrentAuthenticatedUser();

        // --- Step 1: Clear existing portfolio data for the user ---
        projectRepository.deleteByUser(currentUser);
        skillRepository.deleteByUser(currentUser);
        experienceRepository.deleteByUser(currentUser);
        testimonialRepository.deleteByUser(currentUser);
        qualificationRepository.deleteByUser(currentUser);

        // --- Step 2: Restore data from the DTO ---

        // Restore PortfolioProfile
        if (backupDto.getProfile() != null) {
            portfolioProfileRepository.findByUser(currentUser).ifPresent(existingProfile -> {
                portfolioProfileMapper.applyUpdateFromDto(backupDto.getProfile(), existingProfile);
                portfolioProfileRepository.save(existingProfile);
            });
        }

        // Restore Projects
        if (backupDto.getProjects() != null) {
            backupDto.getProjects().forEach(projectDto ->
                    projectRepository.save(projectMapper.toEntityFromDto(projectDto, currentUser))
            );
        }

        // Restore Skills
        if (backupDto.getSkills() != null) {
            backupDto.getSkills().forEach(skillDto ->
                    skillRepository.save(skillMapper.toEntityFromDto(skillDto, currentUser))
            );
        }

        // Restore Experiences
        if (backupDto.getExperiences() != null) {
            backupDto.getExperiences().forEach(expDto ->
                    experienceRepository.save(experienceMapper.toEntityFromDto(expDto, currentUser))
            );
        }

        // Restore Testimonials
        if (backupDto.getTestimonials() != null) {
            backupDto.getTestimonials().forEach(testDto ->
                    testimonialRepository.save(testimonialMapper.toEntityFromDto(testDto, currentUser))
            );
        }

        // Restore Qualifications
        if (backupDto.getQualifications() != null) {
            backupDto.getQualifications().forEach(qualDto ->
                    qualificationRepository.save(qualificationMapper.toEntityFromDto(qualDto, currentUser))
            );
        }
    }
}