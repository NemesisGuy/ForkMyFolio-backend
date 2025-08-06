package com.forkmyfolio.mapper;

import com.forkmyfolio.dto.create.CreateProjectRequest;
import com.forkmyfolio.dto.response.ProjectDto;
import com.forkmyfolio.dto.update.UpdateProjectRequest;
import com.forkmyfolio.model.Project;
import com.forkmyfolio.model.User;
import com.forkmyfolio.model.UserSkill;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper class responsible for converting between Project domain models and Project-related DTOs.
 * This keeps the conversion logic separate from the service and controller layers.
 */
@Component
@RequiredArgsConstructor
public class ProjectMapper {

    private final SkillMapper skillMapper;

    /**
     * Converts a Project entity to a ProjectDto for API responses.
     * This version provides basic skill information without user-specific details.
     *
     * @param project The Project entity to convert.
     * @return The corresponding ProjectDto.
     */
    public ProjectDto toDto(Project project) {
        if (project == null) {
            return null;
        }
        ProjectDto dto = new ProjectDto();
        dto.setUuid(project.getUuid());
        dto.setTitle(project.getTitle());
        dto.setDescription(project.getDescription());
        dto.setRepoUrl(project.getRepoUrl());
        dto.setLiveUrl(project.getLiveUrl());
        dto.setImageUrl(project.getImageUrl());
        dto.setVisible(project.isVisible());
        dto.setDisplayOrder(project.getDisplayOrder());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());

        if (project.getSkills() != null) {
            // This now correctly calls the basic toDto(Skill) method in SkillMapper
            dto.setSkills(project.getSkills().stream()
                    .map(skillMapper::toDto)
                    .collect(Collectors.toSet()));
        } else {
            dto.setSkills(Collections.emptySet());
        }

        return dto;
    }

    /**
     * Converts a Project entity to a ProjectDto, enriching it with user-specific skill data.
     * This is the preferred method for public portfolio views where detailed skill context is needed.
     *
     * @param project     The Project entity to convert.
     * @param skillLookup A map where the key is the global Skill UUID and the value is the user-specific UserSkill entity.
     * @return A detailed ProjectDto with user-specific skill information.
     */
    public ProjectDto toDto(Project project, Map<UUID, UserSkill> skillLookup) {
        if (project == null) {
            return null;
        }
        ProjectDto dto = new ProjectDto();
        dto.setUuid(project.getUuid());
        dto.setTitle(project.getTitle());
        dto.setDescription(project.getDescription());
        dto.setRepoUrl(project.getRepoUrl());
        dto.setLiveUrl(project.getLiveUrl());
        dto.setImageUrl(project.getImageUrl());
        dto.setVisible(project.isVisible());
        dto.setDisplayOrder(project.getDisplayOrder());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());

        if (project.getSkills() != null) {
            dto.setSkills(project.getSkills().stream()
                    .map(skill -> {
                        // Look up the user-specific skill data from the context map
                        UserSkill userSkill = skillLookup.get(skill.getUuid());
                        if (userSkill != null) {
                            // If found, use the detailed mapper to include level, description, etc.
                            return skillMapper.toDetailDto(userSkill);
                        } else {
                            // Fallback to the basic mapper if no user-specific data is available
                            return skillMapper.toDto(skill);
                        }
                    })
                    .collect(Collectors.toSet()));
        } else {
            dto.setSkills(Collections.emptySet());
        }

        return dto;
    }

    /**
     * Converts a CreateProjectRequest DTO to a new, transient Project entity.
     * The User is not set here; the controller is responsible for associating the current user.
     *
     * @param request The DTO containing the creation data.
     * @return A new Project entity, ready to be configured and persisted.
     */
    public Project toEntity(CreateProjectRequest request) {
        if (request == null) {
            return null;
        }
        Project project = new Project();
        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setRepoUrl(request.getRepoUrl());
        project.setLiveUrl(request.getLiveUrl());
        project.setImageUrl(request.getImageUrl());
        project.setVisible(request.isVisible());
        project.setDisplayOrder(request.getDisplayOrder());
        return project;
    }

    /**
     * Converts a ProjectDto (typically from a backup) to a new Project entity.
     *
     * @param dto  The DTO containing the project data.
     * @param user The user who will own this project.
     * @return A new Project entity, ready to be persisted.
     */
    public Project toEntityFromDto(ProjectDto dto, User user) {
        if (dto == null) {
            return null;
        }
        Project project = new Project();
        // FIX: Preserve the original UUID from the backup DTO. This is crucial for restores.
        project.setUuid(dto.getUuid());
        project.setUser(user);
        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDescription());
        project.setRepoUrl(dto.getRepoUrl());
        project.setLiveUrl(dto.getLiveUrl());
        project.setImageUrl(dto.getImageUrl());
        project.setVisible(dto.isVisible());
        project.setDisplayOrder(dto.getDisplayOrder());
        return project;
    }

    /**
     * Creates a transient Project entity from an UpdateProjectRequest DTO.
     *
     * @param request The DTO containing the update data.
     * @return A transient Project entity populated with data from the request.
     */
    public Project toEntity(UpdateProjectRequest request) {
        if (request == null) {
            return null;
        }
        Project project = new Project();
        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setRepoUrl(request.getRepoUrl());
        project.setLiveUrl(request.getLiveUrl());
        project.setImageUrl(request.getImageUrl());
        project.setVisible(request.getVisible());
        project.setDisplayOrder(request.getDisplayOrder());
        return project;
    }
}