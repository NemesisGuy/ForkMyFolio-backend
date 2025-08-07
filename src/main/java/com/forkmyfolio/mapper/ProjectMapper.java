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
     * Converts a Project entity to a DTO, enriching it with user-specific skill data.
     * This is the correct way to build a complete Project DTO for the API.
     *
     * @param project         The Project entity to convert.
     * @param userSkillLookup A map of the user's skills to provide full context.
     * @return The corresponding ProjectDto.
     */
    public ProjectDto toDto(Project project, Map<UUID, UserSkill> userSkillLookup) {
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
            // FIX: Use the lookup map to call the correct skill mapper.
            // This ensures all user-specific data (ID, level, description) is included.
            dto.setSkills(project.getSkills().stream()
                    .map(skill -> {
                        UserSkill userSkill = userSkillLookup.get(skill.getUuid());
                        // If the user has this skill rated, use the detailed mapper. Otherwise, fall back to the basic one.
                        return (userSkill != null) ? skillMapper.toDetailDto(userSkill) : skillMapper.toBasicDto(skill);
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