package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.response.PortfolioBackupDto;
import com.forkmyfolio.mapper.*;
import com.forkmyfolio.model.User;
import com.forkmyfolio.model.UserSkill;
import com.forkmyfolio.service.BackupService;
import com.forkmyfolio.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BackupServiceImpl implements BackupService {

    // Service to get user data
    private final UserService userService;

    // Mappers to convert domain entities to DTOs
    private final PortfolioProfileMapper portfolioProfileMapper;
    private final ProjectMapper projectMapper;
    private final SkillMapper skillMapper;
    private final ExperienceMapper experienceMapper;
    private final TestimonialMapper testimonialMapper;
    private final QualificationMapper qualificationMapper;

    @Override
    @Transactional(readOnly = true)
    public PortfolioBackupDto createBackupForCurrentUser() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        return createBackupDtoForUser(currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioBackupDto createBackupForUser(User user) {
        return createBackupDtoForUser(user);
    }

    /**
     * Private helper method to encapsulate the logic for creating a backup DTO from a User entity.
     * This avoids code duplication between creating a backup for the current user and a specific user.
     *
     * @param user The user for whom to create the backup.
     * @return A fully populated PortfolioBackupDto.
     */
    private PortfolioBackupDto createBackupDtoForUser(User user) {
        PortfolioBackupDto backupDto = new PortfolioBackupDto();

        // Create a lookup map from the global Skill's UUID to the specific UserSkill's UUID.
        // This provides the necessary context to correctly populate the userSkillId for nested skills.
        Map<UUID, UUID> skillUuidToUserSkillUuidMap = user.getUserSkills().stream()
                .collect(Collectors.toMap(
                        userSkill -> userSkill.getSkill().getUuid(), // Key: global Skill UUID
                        UserSkill::getUuid,                         // FIX: Use the public UUID, not the internal Long ID.
                        (uuid1, uuid2) -> uuid1                     // Merge function for safety
                ));

        // 1. Map Profile
        if (user.getPortfolioProfile() != null) {
            backupDto.setProfile(portfolioProfileMapper.toDto(user.getPortfolioProfile()));
        }

        // 2. Map Projects
        backupDto.setProjects(user.getProjects().stream()
                .map(project -> {
                    // First, perform the standard mapping. This will result in a null userSkillId if the user
                    // does not have a top-level UserSkill for a skill associated with the project.
                    var projectDto = projectMapper.toDto(project);

                    // Now, iterate through the mapped skills and use the lookup map to fix the userSkillId.
                    if (projectDto.getSkills() != null) {
                        projectDto.getSkills().forEach(skillDto ->
                                skillDto.setUserSkillId(skillUuidToUserSkillUuidMap.get(skillDto.getSkillId()))
                        );
                    }
                    return projectDto;
                })
                .collect(Collectors.toList()));

        // 3. Map top-level Skills
        backupDto.setSkills(skillMapper.toBackupDtoList(new ArrayList<>(user.getUserSkills())));

        // 4. Map Experiences
        backupDto.setExperiences(user.getExperiences().stream()
                .map(experience -> {
                    // Perform the standard mapping.
                    var experienceDto = experienceMapper.toDto(experience);

                    // Use the lookup map to fix the userSkillId for nested skills.
                    if (experienceDto.getSkills() != null) {
                        experienceDto.getSkills().forEach(skillDto ->
                                skillDto.setUserSkillId(skillUuidToUserSkillUuidMap.get(skillDto.getSkillId()))
                        );
                    }
                    return experienceDto;
                })
                .collect(Collectors.toList()));

        // 5. Map Testimonials
        backupDto.setTestimonials(user.getTestimonials().stream()
                .map(testimonialMapper::toDto)
                .collect(Collectors.toList()));

        // 6. Map Qualifications
        backupDto.setQualifications(user.getQualifications().stream()
                .map(qualificationMapper::toDto)
                .collect(Collectors.toList()));

        return backupDto;
    }
}