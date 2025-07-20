package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.response.PortfolioBackupDto;
import com.forkmyfolio.mapper.*;
import com.forkmyfolio.model.*;
import com.forkmyfolio.repository.*;
import com.forkmyfolio.service.RestoreService;
import com.forkmyfolio.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestoreServiceImpl implements RestoreService {

    private final UserService userService;

    // Repositories for clearing old data
    private final ProjectRepository projectRepository;
    private final SkillRepository skillRepository;
    private final ExperienceRepository experienceRepository;
    private final TestimonialRepository testimonialRepository;
    private final QualificationRepository qualificationRepository;
    private final PortfolioProfileRepository portfolioProfileRepository;

    // Mappers for creating new entities from DTOs
    private final ProjectMapper projectMapper;
    private final SkillMapper skillMapper;
    private final ExperienceMapper experienceMapper;
    private final TestimonialMapper testimonialMapper;
    private final QualificationMapper qualificationMapper;
    private final PortfolioProfileMapper portfolioProfileMapper;

    @Override
    @Transactional
    public void restoreFromBackup(PortfolioBackupDto backupDto) {
        User currentUser = userService.getCurrentAuthenticatedUser();

        // --- Step 1: Clear all existing portfolio data for the user ---
        // This is a destructive action, ensuring a clean slate for the restore.
        projectRepository.deleteByUser(currentUser);
        skillRepository.deleteByUser(currentUser);
        experienceRepository.deleteByUser(currentUser);
        testimonialRepository.deleteByUser(currentUser);
        qualificationRepository.deleteByUser(currentUser);

        // --- Step 2: Restore data from the backup DTO ---

        // Restore Profile (this will update the existing profile)
        PortfolioProfile profile = portfolioProfileRepository.findByUser(currentUser)
                .orElseGet(() -> {
                    PortfolioProfile newProfile = new PortfolioProfile();
                    newProfile.setUser(currentUser);
                    return newProfile;
                });
        portfolioProfileMapper.applyUpdateFromDto(backupDto.getProfile(), profile);
        portfolioProfileRepository.save(profile);

        // Restore Projects
        if (backupDto.getProjects() != null) {
            List<Project> projects = backupDto.getProjects().stream()
                    .map(dto -> projectMapper.toEntityFromDto(dto, currentUser))
                    .collect(Collectors.toList());
            projectRepository.saveAll(projects);
        }

        // Restore Skills
        if (backupDto.getSkills() != null) {
            List<Skill> skills = backupDto.getSkills().stream()
                    .map(dto -> skillMapper.toEntityFromDto(dto, currentUser))
                    .collect(Collectors.toList());
            skillRepository.saveAll(skills);
        }

        // Restore Experiences
        if (backupDto.getExperiences() != null) {
            List<Experience> experiences = backupDto.getExperiences().stream()
                    .map(dto -> experienceMapper.toEntityFromDto(dto, currentUser))
                    .collect(Collectors.toList());
            experienceRepository.saveAll(experiences);
        }

        // Restore Testimonials
        if (backupDto.getTestimonials() != null) {
            List<Testimonial> testimonials = backupDto.getTestimonials().stream()
                    .map(dto -> testimonialMapper.toEntityFromDto(dto, currentUser))
                    .collect(Collectors.toList());
            testimonialRepository.saveAll(testimonials);
        }

        // Restore Qualifications
        if (backupDto.getQualifications() != null) {
            List<Qualification> qualifications = backupDto.getQualifications().stream()
                    .map(dto -> qualificationMapper.toEntityFromDto(dto, currentUser))
                    .collect(Collectors.toList());
            qualificationRepository.saveAll(qualifications);
        }
    }
}