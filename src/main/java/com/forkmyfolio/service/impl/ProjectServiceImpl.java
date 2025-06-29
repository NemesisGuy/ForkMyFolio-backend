package com.forkmyfolio.service.impl;

import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.model.Project;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.ProjectRepository;
import com.forkmyfolio.repository.UserRepository;
import com.forkmyfolio.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of the {@link ProjectService} interface.
 * Handles business logic related to portfolio projects.
 * This service is completely DTO-agnostic.
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Project> getPublicProjects() {
        User owner = userRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("Portfolio owner user not found in the database."));
        return projectRepository.findByUser(owner);
    }

    @Override
    @Transactional(readOnly = true)
    public Project getProjectByUuid(UUID uuid) {
        return projectRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with UUID: " + uuid));
    }

    @Override
    @Transactional
    public Project createProject(Project project) {
        // The service's only job is to persist the entity passed to it.
        return projectRepository.save(project);
    }

    @Override
    @Transactional
    public Project save(Project project) {
        // This method is used for updates, where the entity has been modified in the controller.
        return projectRepository.save(project);
    }

    @Override
    @Transactional
    public void deleteProject(UUID uuid, User currentUser) {
        Project projectToDelete = getProjectByUuid(uuid);

        // Authorization check
        if (!projectToDelete.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("User does not have permission to delete this project.");
        }

        projectRepository.delete(projectToDelete);
    }
}