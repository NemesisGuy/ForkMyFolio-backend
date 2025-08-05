package com.forkmyfolio.service;

import com.forkmyfolio.model.Project;
import com.forkmyfolio.model.User;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service interface for managing portfolio projects.
 * Defines business logic for creating, retrieving, updating, and deleting projects.
 * This service operates solely on domain models (e.g., Project) and is DTO-agnostic.
 */
public interface ProjectService {

    /**
     * Retrieves all projects belonging to a specific user.
     *
     * @param user The user whose projects are to be retrieved.
     * @return A list of {@link Project} entities for the specified user.
     */
    List<Project> getProjectsForUser(User user);

    /**
     * Retrieves a single project by its public UUID, ensuring it belongs to the specified user.
     *
     * @param uuid The UUID of the project.
     * @param user The user who must own the project.
     * @return The {@link Project} entity if found and owned by the user.
     * @throws com.forkmyfolio.exception.ResourceNotFoundException if the project is not found.
     * @throws AccessDeniedException                               if the user does not own the project.
     */
    Project findProjectByUuidAndUser(UUID uuid, User user);

    /**
     * Creates and persists a new project.
     *
     * @param project    The project entity to save. The owner (User) must be set before calling this method.
     * @param skillNames A set of skill names to find and associate with this project.
     * @return The persisted {@link Project} entity, including its generated ID and UUID.
     */
    Project createProject(Project project, Set<String> skillNames);

    /**
     * Updates an existing project.
     *
     * @param uuid               The UUID of the project to update.
     * @param updatedProjectData A Project object containing the new data to be applied.
     * @param skillNames         A set of skill names to find and associate with this project.
     * @param currentUser        The user performing the action, for authorization.
     * @return The updated Project entity.
     */
    Project updateProject(UUID uuid, Project updatedProjectData, Set<String> skillNames, User currentUser);

    /**
     * Deletes a project by its public UUID.
     * Implementations of this method must perform an authorization check.
     *
     * @param uuid        The UUID of the project to delete.
     * @param currentUser The user performing the action, for authorization checks.
     * @throws com.forkmyfolio.exception.ResourceNotFoundException if the project is not found.
     * @throws AccessDeniedException                               if the user is not authorized to delete the project.
     */
    void deleteProject(UUID uuid, User currentUser);
}