package com.forkmyfolio.service.impl;

import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.model.Skill;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.SkillRepository;
import com.forkmyfolio.repository.UserRepository;
import com.forkmyfolio.service.SkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of the {@link SkillService} interface.
 * Handles business logic related to user skills.
 * This service operates solely on domain models and is DTO-agnostic.
 */
@Service
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final UserRepository userRepository;

    @Autowired
    public SkillServiceImpl(SkillRepository skillRepository, UserRepository userRepository) {
        this.skillRepository = skillRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves the list of public skills for the portfolio owner.
     *
     * @return A list of {@link Skill} entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Skill> getPublicSkills() {
        // Find the portfolio owner using our established "first user" strategy.
        User owner = userRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("Portfolio owner user not found in the database."));

        // Fetch their skills. Assumes a method `findByUser` exists in SkillRepository.
        return skillRepository.findByUser(owner);
    }

    /**
     * Retrieves a single skill by its public UUID.
     *
     * @param uuid The UUID of the skill to find.
     * @return The {@link Skill} entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Skill getSkillByUuid(UUID uuid) {
        return skillRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with UUID: " + uuid));
    }

    /**
     * Creates and persists a new skill.
     * The incoming Skill object should be pre-constructed by a mapper.
     *
     * @param skill The new skill entity to save.
     * @return The persisted {@link Skill} entity with its generated ID and UUID.
     */
    @Override
    @Transactional
    public Skill createSkill(Skill skill) {
        // The service's job is simply to persist the fully-formed entity.
        return skillRepository.save(skill);
    }

    /**
     * Deletes a skill by its public UUID, ensuring the user has permission.
     *
     * @param uuid The UUID of the skill to delete.
     * @param currentUser The user performing the action.
     */
    @Override
    @Transactional
    public void deleteSkill(UUID uuid, User currentUser) {
        Skill skillToDelete = getSkillByUuid(uuid);

        // Authorization check: Ensure the skill belongs to the user trying to delete it.
        // This is a crucial check even if the controller endpoint is secured by role.
        if (!skillToDelete.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("User does not have permission to delete this skill.");
        }

        skillRepository.delete(skillToDelete);
    }
}