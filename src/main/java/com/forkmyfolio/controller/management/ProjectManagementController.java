package com.forkmyfolio.controller.management;

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
@Tag(name = "Project Management (Me)", description = "Endpoints for the authenticated user to manage their own portfolio projects.")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
public class ProjectManagementController {

    private final ProjectService projectService;
    private final UserService userService;
    private final ProjectMapper projectMapper;

    @GetMapping
    @Operation(summary = "Get all of my projects")
    public ResponseEntity<List<ProjectDto>> getMyProjects() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        List<Project> projects = projectService.getProjectsForUser(currentUser);
        List<ProjectDto> projectDtos = projects.stream()
                .map(projectMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(projectDtos);
    }

    @GetMapping("/{uuid}")
    @Operation(summary = "Get one of my projects by its UUID")
    public ResponseEntity<ProjectDto> getMyProjectByUuid(@PathVariable UUID uuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        Project project = projectService.findProjectByUuidAndUser(uuid, currentUser);
        return ResponseEntity.ok(projectMapper.toDto(project));
    }

    @PostMapping
    @Operation(summary = "Create a new project for myself")
    public ResponseEntity<ProjectDto> createMyProject(@Valid @RequestBody CreateProjectRequest createRequest) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        Project newProject = projectMapper.toEntity(createRequest, currentUser);
        // CORRECT: Pass the set of skill names from the request DTO.
        // Note: You must update CreateProjectRequest to have a `getSkills()` method returning Set<String>.
        Project createdProject = projectService.createProject(newProject, createRequest.getSkills());
        return new ResponseEntity<>(projectMapper.toDto(createdProject), HttpStatus.CREATED);
    }

    @PutMapping("/{uuid}")
    @Operation(summary = "Update one of my projects")
    public ResponseEntity<ProjectDto> updateMyProject(@PathVariable UUID uuid, @Valid @RequestBody UpdateProjectRequest updateRequest) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        Project updatedProjectData = projectMapper.toEntity(updateRequest);
        // CORRECT: Pass the set of skill names from the request DTO.
        // Note: You must update UpdateProjectRequest to have a `getSkills()` method returning Set<String>.
        Project updatedProject = projectService.updateProject(uuid, updatedProjectData, updateRequest.getSkills(), currentUser);
        return ResponseEntity.ok(projectMapper.toDto(updatedProject));
    }

    @DeleteMapping("/{uuid}")
    @Operation(summary = "Delete one of my projects")
    public ResponseEntity<Void> deleteMyProject(@PathVariable UUID uuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        projectService.deleteProject(uuid, currentUser);
        return ResponseEntity.noContent().build();
    }
}