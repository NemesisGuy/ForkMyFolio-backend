package com.forkmyfolio.controller.management;

import com.forkmyfolio.advice.ApiResponseWrapper;
import com.forkmyfolio.dto.create.CreateProjectRequest;
import com.forkmyfolio.dto.response.ProjectDto;
import com.forkmyfolio.dto.update.UpdateProjectRequest;
import com.forkmyfolio.mapper.ProjectMapper;
import com.forkmyfolio.model.Project;
import com.forkmyfolio.model.User;
import com.forkmyfolio.service.ProjectService;
import com.forkmyfolio.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/me/projects")
@Tag(name = "Project Management (Me)", description = "Endpoints for the authenticated user to manage their portfolio projects.")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
public class ProjectManagementController {

    private final ProjectService projectService;
    private final UserService userService;
    private final ProjectMapper projectMapper;

    @GetMapping
    @Operation(summary = "Get all of my projects")
    public ResponseEntity<ApiResponseWrapper<List<ProjectDto>>> getMyProjects() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        List<Project> projects = projectService.getProjectsForUser(currentUser);

        // Map the list of Project entities to a list of ProjectDtos
        List<ProjectDto> projectDtos = projects.stream()
                .map(projectMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponseWrapper<>(projectDtos));
    }

    @GetMapping("/{uuid}")
    @Operation(summary = "Get a specific project by its UUID")
    public ResponseEntity<ApiResponseWrapper<ProjectDto>> getProjectByUuid(@PathVariable UUID uuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        Project project = projectService.findProjectByUuidAndUser(uuid, currentUser);
        ProjectDto projectDto = projectMapper.toDto(project);
        return ResponseEntity.ok(new ApiResponseWrapper<>(projectDto));
    }

    @PostMapping
    @Operation(summary = "Create a new project")
    public ResponseEntity<ApiResponseWrapper<ProjectDto>> createProject(@Valid @RequestBody CreateProjectRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();

        // Map the incoming DTO to a Project entity
        Project projectToCreate = projectMapper.toEntity(request);
        projectToCreate.setUser(currentUser); // Associate the project with the current user

        // The service layer operates on the pure entity
        Project createdProject = projectService.createProject(projectToCreate, request.getSkills());

        // Map the resulting entity back to a DTO for the response
        ProjectDto responseDto = projectMapper.toDto(createdProject);
        return new ResponseEntity<>(new ApiResponseWrapper<>(responseDto), HttpStatus.CREATED);
    }

    @PutMapping("/{uuid}")
    @Operation(summary = "Update an existing project")
    public ResponseEntity<ApiResponseWrapper<ProjectDto>> updateProject(@PathVariable UUID uuid, @Valid @RequestBody UpdateProjectRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();

        // Map the incoming DTO to a Project entity containing the updated data
        Project projectWithUpdates = projectMapper.toEntity(request);

        // The service layer handles the update logic using the pure entity
        Project updatedProject = projectService.updateProject(uuid, projectWithUpdates, request.getSkills(), currentUser);

        // Map the updated entity back to a DTO for the response
        ProjectDto responseDto = projectMapper.toDto(updatedProject);
        return ResponseEntity.ok(new ApiResponseWrapper<>(responseDto));
    }

    @DeleteMapping("/{uuid}")
    @Operation(summary = "Delete a project")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID uuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        projectService.deleteProject(uuid, currentUser);
        return ResponseEntity.noContent().build();
    }
}