package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.CreateSkillRequest;
import com.forkmyfolio.dto.SkillDto;
import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.model.Skill;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.SkillRepository;
import com.forkmyfolio.service.SkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link SkillService} interface.
 * Handles business logic related to user skills.
 */
@Service
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;

    /**
     * Constructs a {@code SkillServiceImpl} with the necessary {@link SkillRepository}.
     *
     * @param skillRepository The repository for accessing skill data.
     */
    @Autowired
    public SkillServiceImpl(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<SkillDto> getAllSkills() {
        // This currently returns all skills in the system.
        // It could be adapted to return skills for a specific user if /api/v1/skills implies current user's skills
        // or be an admin-only endpoint if it's meant to list all skills across all users.
        // For now, keeping it simple as "all skills available".
        return skillRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<SkillDto> getAllSkillsByUserId(Long userId) {
        return skillRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public SkillDto getSkillById(Long id) {
        Skill skill = findSkillEntityById(id);
        return convertToDto(skill);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Skill findSkillEntityById(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public SkillDto createSkill(CreateSkillRequest createSkillRequest, User currentUser) {
        // The skill is associated with the admin user who created it.
        // This implies an admin might be adding skills to their own profile or a system-wide list.
        // If skills are user-specific beyond just admin-managed, this logic might need adjustment.
        Skill skill = convertCreateRequestToEntity(createSkillRequest, currentUser);
        Skill savedSkill = skillRepository.save(skill);
        return convertToDto(savedSkill);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteSkill(Long id, User currentUser) {
        Skill skill = findSkillEntityById(id);

        // Authorization: Check if the current user is an ADMIN
        // OR if the skill belongs to the current user (if non-admins could manage their own skills).
        // For this iteration, only ADMINs can delete, and they can delete any skill.
        // The @PreAuthorize("hasRole('ADMIN')") on the controller handles the role check.
        // A more granular check could be:
        // if (!currentUser.getRoles().contains(Role.ADMIN) && !skill.getUser().getId().equals(currentUser.getId())) {
        //    throw new AccessDeniedException("User does not have permission to delete this skill.");
        // }
        // However, the current requirement is admin-only deletion for skills.

        skillRepository.delete(skill);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SkillDto convertToDto(Skill skill) {
        if (skill == null) {
            return null;
        }
        return new SkillDto(
                skill.getId(),
                skill.getName(),
                skill.getLevel(),
                skill.getUser() != null ? skill.getUser().getId() : null,
                skill.getCreatedAt(),
                skill.getUpdatedAt()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Skill convertCreateRequestToEntity(CreateSkillRequest request, User owner) {
        Skill skill = new Skill();
        skill.setName(request.getName());
        skill.setLevel(request.getLevel());
        skill.setUser(owner); // Associate the skill with the owner
        // createdAt and updatedAt will be handled by Hibernate
        return skill;
    }
}
