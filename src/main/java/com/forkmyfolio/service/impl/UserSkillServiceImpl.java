package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.request.CreateUserSkillDto;
import com.forkmyfolio.dto.response.UserSkillResponseDto;
import com.forkmyfolio.dto.update.UpdateUserSkillRequest;
import com.forkmyfolio.exception.ResourceAlreadyExistsException;
import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.mapper.UserSkillMapper;
import com.forkmyfolio.model.Skill;
import com.forkmyfolio.model.User;
import com.forkmyfolio.model.UserSkill;
import com.forkmyfolio.repository.SkillRepository;
import com.forkmyfolio.repository.UserSkillRepository;
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
 */
@Service
@RequiredArgsConstructor
public class UserSkillServiceImpl implements UserSkillService {

    private final UserSkillRepository userSkillRepository;
    private final SkillRepository skillRepository;
    private final UserSkillMapper userSkillMapper;

    @Override
    @Transactional
    public UserSkillResponseDto createUserSkill(CreateUserSkillDto createDto, User user) {
        // 1. Find the global Skill entity by name, or create a new one if it doesn't exist.
        Skill skill = skillRepository.findByName(createDto.getName())
                .orElseGet(() -> {
                    Skill newSkill = new Skill();
                    newSkill.setName(createDto.getName());
                    newSkill.setCategory(StringUtils.hasText(createDto.getCategory()) ? createDto.getCategory() : "Uncategorized");
                    newSkill.setIcon(createDto.getIcon());
                    // The global description for a newly created skill is taken from the DTO.
                    newSkill.setDescription(createDto.getDescription());
                    newSkill.setVisible(true);
                    return skillRepository.save(newSkill);
                });

        // Prevent adding a duplicate skill relationship for the same user.
        if (userSkillRepository.existsByUserAndSkill(user, skill)) {
            throw new ResourceAlreadyExistsException("You have already added the skill: " + skill.getName());
        }

        // 2. Create the new UserSkill relationship entity.
        UserSkill newUserSkill = new UserSkill();
        newUserSkill.setUser(user);
        newUserSkill.setSkill(skill);
        newUserSkill.setLevel(createDto.getLevel());
        newUserSkill.setVisible(createDto.isVisible());

        // 3. Apply the description logic.
        // If the user provided a specific description in the form, use it.
        // Otherwise, fall back to the global skill's default description.
        if (StringUtils.hasText(createDto.getDescription())) {
            newUserSkill.setDescription(createDto.getDescription());
        } else {
            newUserSkill.setDescription(skill.getDescription());
        }

        // 4. Save the new relationship to the database.
        UserSkill savedUserSkill = userSkillRepository.save(newUserSkill);

        // 5. Use the injected mapper to convert the entity to a response DTO.
        return userSkillMapper.toResponseDto(savedUserSkill);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSkillResponseDto> getAllUserSkills(User user) {
        List<UserSkill> userSkills = userSkillRepository.findByUser(user);
        return userSkillMapper.toResponseDtoList(userSkills);
    }

    @Override
    @Transactional
    public UserSkillResponseDto updateUserSkill(UUID userSkillId, UpdateUserSkillRequest updateDto, User user) {
        // FIX: Use findByUuid to look up by the public-facing identifier.
        UserSkill userSkill = userSkillRepository.findByUuid(userSkillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill with ID " + userSkillId + " not found."));

        // Security check: ensure the user owns this skill relationship.
        if (!userSkill.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to update this skill.");
        }

        // Apply updates from the DTO.
        if (updateDto.getLevel() != null) {
            userSkill.setLevel(updateDto.getLevel());
        }
        if (updateDto.getVisible() != null) {
            userSkill.setVisible(updateDto.getVisible());
        }
        if (updateDto.getDescription() != null) {
            userSkill.setDescription(updateDto.getDescription());
        }

        UserSkill updatedUserSkill = userSkillRepository.save(userSkill);
        return userSkillMapper.toResponseDto(updatedUserSkill);
    }

    @Override
    @Transactional
    public void deleteUserSkill(UUID userSkillId, User user) {
        // FIX: Use findByUuid to look up by the public-facing identifier.
        UserSkill userSkill = userSkillRepository.findByUuid(userSkillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill with ID " + userSkillId + " not found."));

        // Security check: ensure the user owns this skill relationship.
        if (!userSkill.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this skill.");
        }

        userSkillRepository.delete(userSkill);
    }
}