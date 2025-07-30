package com.forkmyfolio.service.impl;

import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.model.Skill;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.SkillRepository;
import com.forkmyfolio.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of the {@link SkillService} interface.
 * Handles business logic related to skills.
 */
@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Skill> getSkillsForUser(User user) {
        return skillRepository.findByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Skill findSkillByUuid(UUID uuid) {
        return skillRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Skill with UUID: " + uuid));
    }

    @Override
    @Transactional(readOnly = true)
    public Skill findSkillByUuidAndUser(UUID uuid, User user) {
        Skill skill = findSkillByUuid(uuid);
        if (!skill.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("User does not have permission to access this skill.");
        }
        return skill;
    }

    @Override
    @Transactional
    public Skill createSkill(Skill skill) {
        // The user must be set on the skill object before calling this method.
        return skillRepository.save(skill);
    }

    @Override
    @Transactional
    public void deleteSkill(UUID uuid, User currentUser) {
        Skill skillToDelete = findSkillByUuidAndUser(uuid, currentUser);
        skillRepository.delete(skillToDelete);
    }

    @Override
    @Transactional
    public Skill updateSkill(UUID uuid, String name, Skill.SkillLevel level, boolean visible, String category, String icon, String description, User currentUser) {
        Skill skillToUpdate = findSkillByUuidAndUser(uuid, currentUser);

        skillToUpdate.setName(name);
        skillToUpdate.setLevel(level);
        skillToUpdate.setVisible(visible);
        skillToUpdate.setCategory(category);
        skillToUpdate.setIcon(icon);
        skillToUpdate.setDescription(description);

        return skillRepository.save(skillToUpdate);
    }
}