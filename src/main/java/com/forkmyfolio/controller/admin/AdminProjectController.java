package com.forkmyfolio.controller.admin;

import com.forkmyfolio.dto.create.CreateProjectRequest;
import com.forkmyfolio.dto.response.ProjectDto;
import com.forkmyfolio.dto.update.UpdateProjectRequest;
import com.forkmyfolio.mapper.ProjectMapper;
import com.forkmyfolio.model.Project;
import com.forkmyfolio.model.User;
import com.forkmyfolio.service.ProjectService;
import com.forkmyfolio.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Admin controller for managing portfolio projects.
 */
@RestController
@RequestMapping("/api/v1/admin/projects")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin: Project Management", description = "Endpoints for managing portfolio projects.")
@SecurityRequirement(name = "bearerAuth")
public class AdminProjectController {

    private static final Logger logger = LoggerFactory.getLogger(AdminProjectController.class);

    private final ProjectService projectService;
    private final UserService userService;
    private final ProjectMapper projectMapper;

    @Autowired
    public AdminProjectController(ProjectService projectService, UserService userService, ProjectMapper projectMapper) {
        this.projectService = projectService;
        this.userService = userService;
        this.projectMapper = projectMapper;
    }

    // --- READ (List All) ---
    @GetMapping
    @Operation(summary = "List all projects", description = "Retrieves a list of all projects for the admin to manage.")
    public List<ProjectDto> getAllProjectsForAdmin() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' requested all project entries.", currentUser.getEmail());
        // The getPublicProjects method correctly retrieves all projects for the single owner
        List<Project> projects = projectService.getPublicProjects();
        return projects.stream()
                .map(projectMapper::toDto)
                .collect(Collectors.toList());
    }

    // --- READ (Single by UUID) ---
    @GetMapping("/{uuid}")
    @Operation(summary = "Get a single project by UUID", description = "Retrieves a single project by its UUID, typically for populating an edit form.")
    public ProjectDto getProjectByUuid(@Parameter(description = "The UUID of the project") @PathVariable UUID uuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' requested project with UUID: {}", currentUser.getEmail(), uuid);
        Project project = projectService.getProjectByUuid(uuid);
        return projectMapper.toDto(project);
    }

    // --- CREATE ---
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new project")
    public ProjectDto createProject(@Valid @RequestBody CreateProjectRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' creating new project: {}", currentUser.getEmail(), request.getTitle());
        Project newProject = projectMapper.toEntity(request, currentUser);
        Project savedProject = projectService.createProject(newProject);
        return projectMapper.toDto(savedProject);
    }

    // --- UPDATE ---
    @PutMapping("/{uuid}")
    @Operation(summary = "Update a project by UUID")
    public ProjectDto updateProject(@Parameter(description = "The UUID of the project") @PathVariable UUID uuid, @Valid @RequestBody UpdateProjectRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' updating project UUID: {}", currentUser.getEmail(), uuid);
        Project existingProject = projectService.getProjectByUuid(uuid);

        // Authorization check
        if (!existingProject.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("User does not have permission to update this project.");
        }

        projectMapper.applyUpdateFromRequest(request, existingProject);
        Project updatedProject = projectService.save(existingProject);
        return projectMapper.toDto(updatedProject);
    }

    // --- DELETE ---
    @DeleteMapping("/{uuid}")
    @Operation(summary = "Delete a project by UUID")
    public ResponseEntity<Void> deleteProject(@Parameter(description = "The UUID of the project") @PathVariable UUID uuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' deleting project UUID: {}", currentUser.getEmail(), uuid);
        projectService.deleteProject(uuid, currentUser);
        return ResponseEntity.noContent().build();
    }
}