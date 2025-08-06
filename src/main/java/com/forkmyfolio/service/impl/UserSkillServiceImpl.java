package com.forkmyfolio.service.impl;

import com.forkmyfolio.exception.ResourceAlreadyExistsException;
import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.model.Skill;
import com.forkmyfolio.model.User;
import com.forkmyfolio.model.UserSkill;
import com.forkmyfolio.repository.UserSkillRepository;
import com.forkmyfolio.service.SkillService;
import com.forkmyfolio.service.UserSkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

/**
 * Service implementation for managing user-specific skills.
 * This service operates exclusively on domain entities.
 */
@Service
@RequiredArgsConstructor
public class UserSkillServiceImpl implements UserSkillService {

    private final UserSkillRepository userSkillRepository;
    private final SkillService skillService;

    @Override
    @Transactional
    public UserSkill addSkillToUser(User user, Skill skillDetails, UserSkill userSkillDetails) {
        // 1. Delegate finding or creating the global Skill to the SkillService.
        Skill skill = skillService.findOrCreateSkill(skillDetails);

        // 2. Prevent adding a duplicate skill relationship for the same user.
        if (userSkillRepository.existsByUserAndSkill(user, skill)) {
            throw new ResourceAlreadyExistsException("You have already added the skill: " + skill.getName());
        }

        // 3. Create the new UserSkill relationship entity.
        UserSkill newUserSkill = new UserSkill();
        newUserSkill.setUser(user);
        newUserSkill.setSkill(skill);
        newUserSkill.setLevel(userSkillDetails.getLevel());
        newUserSkill.setVisible(userSkillDetails.isVisible());

        // 4. Apply the description logic.
        // If the user provided a specific description, use it.
        // Otherwise, fall back to the global skill's default description.
        if (StringUtils.hasText(userSkillDetails.getDescription())) {
            newUserSkill.setDescription(userSkillDetails.getDescription());
        } else {
            newUserSkill.setDescription(skill.getDescription());
        }

        // 5. Save the new relationship to the database.
        return userSkillRepository.save(newUserSkill);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSkill> getAllSkillsForUser(User user) {
        // FIX: Call the new repository method that eagerly fetches the associated Skill entities.
        // This resolves the LazyInitializationException that occurs in the controller/mapper layer.
        return userSkillRepository.findByUserWithSkillEagerly(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserSkill getSkillForUser(User user, UUID userSkillUuid) {
        UserSkill userSkill = userSkillRepository.findByUuid(userSkillUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Skill with ID " + userSkillUuid + " not found."));

        // Security check: ensure the user owns this skill relationship.
        if (!userSkill.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to access this skill.");
        }
        return userSkill;
    }

    @Override
    @Transactional
    public UserSkill updateSkillForUser(UUID userSkillUuid, UserSkill userSkillUpdates, User user) {
        UserSkill existingUserSkill = getSkillForUser(user, userSkillUuid); // Reuse lookup and security check

        // Apply updates from the transient entity.
        existingUserSkill.setLevel(userSkillUpdates.getLevel());
        existingUserSkill.setVisible(userSkillUpdates.isVisible());
        existingUserSkill.setDescription(userSkillUpdates.getDescription());

        return userSkillRepository.save(existingUserSkill);
    }

    @Override
    @Transactional
    public void removeSkillFromUser(UUID userSkillUuid, User user) {
        UserSkill userSkill = getSkillForUser(user, userSkillUuid); // Reuse lookup and security check
        userSkillRepository.delete(userSkill);
    }
}