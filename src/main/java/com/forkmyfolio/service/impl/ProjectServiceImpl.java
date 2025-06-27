package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.CreateProjectRequest;
import com.forkmyfolio.dto.ProjectDto;
import com.forkmyfolio.dto.UpdateProjectRequest;
import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.model.Project;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.ProjectRepository;
import com.forkmyfolio.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link ProjectService} interface.
 * Handles business logic related to portfolio projects.
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    /**
     * Constructs a {@code ProjectServiceImpl} with the necessary {@link ProjectRepository}.
     *
     * @param projectRepository The repository for accessing project data.
     */
    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Project findProjectEntityById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ProjectDto getProjectById(Long id) {
        Project project = findProjectEntityById(id);
        return convertToDto(project);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ProjectDto createProject(CreateProjectRequest createProjectRequest, User currentUser) {
        // For now, any authenticated user with ADMIN role can create projects.
        // The project is associated with the admin user who created it.
        Project project = convertCreateRequestToEntity(createProjectRequest, currentUser);
        Project savedProject = projectRepository.save(project);
        return convertToDto(savedProject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ProjectDto updateProject(Long id, UpdateProjectRequest updateProjectRequest, User currentUser) {
        Project project = findProjectEntityById(id);

        // Authorization check: For now, any admin can update any project.
        // More specific ownership checks could be added if required (e.g., project.getUser().getId().equals(currentUser.getId()))
        // This is typically handled by @PreAuthorize on controller or service method in more complex scenarios.
        // Since the controller method will be @PreAuthorize("hasRole('ADMIN')"), this basic check is illustrative.
        // if (!currentUser.getRoles().contains(Role.ADMIN)) { // This specific check might be redundant if controller is secured
        //    throw new AccessDeniedException("User does not have permission to update this project.");
        // }


        updateProjectRequest.getTitle().ifPresent(project::setTitle);
        updateProjectRequest.getDescription().ifPresent(project::setDescription);
        updateProjectRequest.getTechStack().ifPresent(project::setTechStack);
        updateProjectRequest.getRepoUrl().ifPresent(project::setRepoUrl);
        updateProjectRequest.getLiveUrl().ifPresent(project::setLiveUrl);
        updateProjectRequest.getImageUrl().ifPresent(project::setImageUrl);
        // Note: The 'user' (owner) of the project is not changed during an update via this DTO.
        // createdAt is not updatable. updatedAt will be handled by @UpdateTimestamp.

        Project updatedProject = projectRepository.save(project);
        return convertToDto(updatedProject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteProject(Long id, User currentUser) {
        Project project = findProjectEntityById(id);
        // Authorization check: Similar to update, ensuring admin role via controller's @PreAuthorize.
        // More specific ownership checks could be added here if needed.
        projectRepository.delete(project);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProjectDto convertToDto(Project project) {
        if (project == null) {
            return null;
        }
        return new ProjectDto(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getTechStack(),
                project.getRepoUrl(),
                project.getLiveUrl(),
                project.getImageUrl(),
                project.getUser() != null ? project.getUser().getId() : null, // Handle potential null user
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Project convertCreateRequestToEntity(CreateProjectRequest request, User owner) {
        Project project = new Project();
        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setTechStack(request.getTechStack());
        project.setRepoUrl(request.getRepoUrl());
        project.setLiveUrl(request.getLiveUrl());
        project.setImageUrl(request.getImageUrl());
        project.setUser(owner); // Associate the project with the owner
        // createdAt and updatedAt will be handled by Hibernate
        return project;
    }
}
