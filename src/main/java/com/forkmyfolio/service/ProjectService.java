package com.forkmyfolio.service;

import com.forkmyfolio.dto.CreateProjectRequest;
import com.forkmyfolio.dto.ProjectDto;
import com.forkmyfolio.dto.UpdateProjectRequest;
import com.forkmyfolio.model.Project;
import com.forkmyfolio.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service interface for managing projects.
 * Defines operations such as creating, retrieving, updating, and deleting projects.
 */
public interface ProjectService {

    /**
     * Retrieves all projects.
     *
     * @return A list of {@link ProjectDto} objects.
     */
    List<ProjectDto> getAllProjects();

    /**
     * Retrieves a project by its ID.
     *
     * @param id The ID of the project to retrieve.
     * @return The {@link ProjectDto} if found.
     * @throws com.forkmyfolio.exception.ResourceNotFoundException if the project with the given ID is not found.
     */
    ProjectDto getProjectById(Long id);

    /**
     * Retrieves a project entity by its ID.
     *
     * @param id The ID of the project to retrieve.
     * @return The {@link Project} entity if found.
     * @throws com.forkmyfolio.exception.ResourceNotFoundException if the project with the given ID is not found.
     */
    Project findProjectEntityById(Long id);


    /**
     * Creates a new project.
     *
     * @param createProjectRequest DTO containing the details for the new project.
     * @param currentUser          The user creating the project.
     * @return The created {@link ProjectDto}.
     */
    ProjectDto createProject(CreateProjectRequest createProjectRequest, User currentUser);

    /**
     * Updates an existing project.
     *
     * @param id                   The ID of the project to update.
     * @param updateProjectRequest DTO containing the updated details for the project.
     * @param currentUser          The user attempting to update the project.
     * @return The updated {@link ProjectDto}.
     * @throws com.forkmyfolio.exception.ResourceNotFoundException       if the project with the given ID is not found.
     * @throws org.springframework.security.access.AccessDeniedException if the user is not authorized to update the project.
     */
    ProjectDto updateProject(Long id, UpdateProjectRequest updateProjectRequest, User currentUser);

    /**
     * Deletes a project by its ID.
     *
     * @param id          The ID of the project to delete.
     * @param currentUser The user attempting to delete the project.
     * @throws com.forkmyfolio.exception.ResourceNotFoundException       if the project with the given ID is not found.
     * @throws org.springframework.security.access.AccessDeniedException if the user is not authorized to delete the project.
     */
    void deleteProject(Long id, User currentUser);

    /**
     * Converts a {@link Project} entity to a {@link ProjectDto}.
     *
     * @param project The project entity.
     * @return The corresponding DTO.
     */
    ProjectDto convertToDto(Project project);

    /**
     * Converts a {@link CreateProjectRequest} DTO to a {@link Project} entity.
     *
     * @param request The DTO.
     * @param owner   The user who will own the project.
     * @return The project entity.
     */
    Project convertCreateRequestToEntity(CreateProjectRequest request, User owner);

    @Transactional(readOnly = true)
    List<ProjectDto> getPublicProjects();
}
