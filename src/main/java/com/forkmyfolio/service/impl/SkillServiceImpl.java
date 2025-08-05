package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.create.CreateSkillRequest;
import com.forkmyfolio.dto.update.UpdateSkillRequest;
import com.forkmyfolio.exception.ResourceAlreadyExistsException;
import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.mapper.SkillMapper;
import com.forkmyfolio.model.Skill;
import com.forkmyfolio.model.User;
import com.forkmyfolio.model.UserSkill;
import com.forkmyfolio.repository.SkillRepository;
import com.forkmyfolio.repository.UserSkillRepository;
import com.forkmyfolio.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final SkillMapper skillMapper;

    /**
     * Retrieves all skills available on the platform.
     */
    @Override
    public List<Skill> getAllPlatformSkills() {
        return skillRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSkill> getAllSkillsForUser(User user) {
        return userSkillRepository.findByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserSkill getSkillForUser(User user, UUID skillUuid) {
        return userSkillRepository.findByUserAndSkillUuid(user, skillUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Skill with UUID " + skillUuid + " not found for this user."));
    }

    @Override
    @Transactional
    public UserSkill addSkillToUser(CreateSkillRequest request, User user) {
        // 1. Find or create the global skill. This is case-insensitive.
        Skill globalSkill = skillRepository.findByNameIgnoreCase(request.getName())
                .orElseGet(() -> {
                    Skill newGlobalSkill = new Skill();
                    newGlobalSkill.setName(request.getName());
                    // Admin can add icon/category/default level later
                    return skillRepository.save(newGlobalSkill);
                });

        // 2. Check if the user already has this skill to prevent duplicates.
        userSkillRepository.findByUserAndSkill(user, globalSkill).ifPresent(us -> {
            // FIX: Use the standardized ResourceAlreadyExistsException for consistency.
            throw new ResourceAlreadyExistsException("You have already added the skill: " + globalSkill.getName());
        });

        // 3. Create the new user-specific relationship.
        UserSkill newUserSkill = new UserSkill();
        newUserSkill.setUser(user);
        newUserSkill.setSkill(globalSkill);
        newUserSkill.setLevel(request.getLevel());
        newUserSkill.setVisible(request.isVisible());
        newUserSkill.setDescription(request.getDescription());

        return userSkillRepository.save(newUserSkill);
    }

    @Override
    @Transactional
    public UserSkill updateSkillForUser(UUID skillUuid, UpdateSkillRequest request, User user) {
        UserSkill existingUserSkill = getSkillForUser(user, skillUuid);
        // This line now compiles correctly.
        skillMapper.applyUpdateFromRequest(request, existingUserSkill);
        return userSkillRepository.save(existingUserSkill);
    }

    @Override
    @Transactional
    public void removeSkillFromUser(UUID skillUuid, User user) {
        UserSkill userSkillToDelete = getSkillForUser(user, skillUuid);
        userSkillRepository.delete(userSkillToDelete);
    }

    @Override
    @Transactional
    public Set<Skill> findOrCreateSkills(Set<String> skillNames) {
        if (skillNames == null || skillNames.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> trimmedNames = skillNames.stream().map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
        if (trimmedNames.isEmpty()) {
            return Collections.emptySet();
        }

        // 1. Find all skills that already exist globally from the given names.
        Set<Skill> existingSkills = skillRepository.findByNameIn(trimmedNames);
        Set<String> existingSkillNames = existingSkills.stream()
                .map(Skill::getName)
                .collect(Collectors.toSet());

        // 2. Determine which skill names are new.
        Set<Skill> newSkillsToCreate = trimmedNames.stream()
                .filter(name -> !existingSkillNames.contains(name))
                .map(name -> {
                    // 3. For each new name, create a new global Skill entity.
                    Skill newSkill = new Skill();
                    newSkill.setName(name);
                    // Set sensible defaults. Admins can curate these later.
                    newSkill.setVisible(true);
                    return newSkill;
                })
                .collect(Collectors.toSet());

        // 4. Save all the newly created skills to the database in one go.
        if (!newSkillsToCreate.isEmpty()) {
            skillRepository.saveAll(newSkillsToCreate);
        }

        // 5. Combine the existing skills and the newly created ones for the final result.
        Set<Skill> allSkills = new HashSet<>(existingSkills);
        allSkills.addAll(newSkillsToCreate);

        return allSkills;
    }
}