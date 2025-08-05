package com.forkmyfolio.service.impl;

import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.model.Experience;
import com.forkmyfolio.model.Skill;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.ExperienceRepository;
import com.forkmyfolio.service.ExperienceService;
import com.forkmyfolio.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of the {@link ExperienceService} interface.
 * Handles business logic related to work experiences.
 */
@Service
@RequiredArgsConstructor
public class ExperienceServiceImpl implements ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final SkillService skillService;

    @Override
    @Transactional(readOnly = true)
    public List<Experience> getExperiencesForUser(User user) {
        return experienceRepository.findByUserOrderByDisplayOrderAsc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Experience findExperienceByUuidAndUser(UUID uuid, User user) {
        Experience experience = experienceRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Experience with UUID: " + uuid));

        if (!experience.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("User does not have permission to access this experience.");
        }
        return experience;
    }

    @Override
    @Transactional
    public Experience createExperience(Experience experience, Set<String> skillNames) {
        // Delegate to SkillService to find or create the associated global skills.
        Set<Skill> skills = skillService.findOrCreateSkills(skillNames);
        experience.setSkills(skills);
        return experienceRepository.save(experience);
    }

    @Override
    @Transactional
    public Experience updateExperience(UUID uuid, Experience updatedExperienceData, Set<String> skillNames, User currentUser) {
        Experience existingExperience = findExperienceByUuidAndUser(uuid, currentUser);

        // Update all fields from the provided data object
        existingExperience.setJobTitle(updatedExperienceData.getJobTitle());
        existingExperience.setCompanyName(updatedExperienceData.getCompanyName());
        existingExperience.setCompanyUrl(updatedExperienceData.getCompanyUrl());
        existingExperience.setCompanyLogoUrl(updatedExperienceData.getCompanyLogoUrl());
        existingExperience.setLocation(updatedExperienceData.getLocation());
        existingExperience.setLocationType(updatedExperienceData.getLocationType());
        existingExperience.setEmploymentType(updatedExperienceData.getEmploymentType());
        existingExperience.setStartDate(updatedExperienceData.getStartDate());
        existingExperience.setEndDate(updatedExperienceData.getEndDate());
        existingExperience.setDescription(updatedExperienceData.getDescription());
        existingExperience.setAchievements(updatedExperienceData.getAchievements());
        existingExperience.setVisible(updatedExperienceData.isVisible());
        existingExperience.setDisplayOrder(updatedExperienceData.getDisplayOrder());

        // Update associated skills by finding or creating them in the global pool.
        Set<Skill> skillsToAssociate = skillService.findOrCreateSkills(skillNames);
        existingExperience.setSkills(skillsToAssociate);

        return experienceRepository.save(existingExperience);
    }

    @Override
    @Transactional
    public void deleteExperience(UUID uuid, User currentUser) {
        Experience experienceToDelete = findExperienceByUuidAndUser(uuid, currentUser);
        experienceRepository.delete(experienceToDelete);
    }
}