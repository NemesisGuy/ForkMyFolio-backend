/*
package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.create.CreateProjectRequest;
import com.forkmyfolio.dto.response.ProjectDto;
import com.forkmyfolio.dto.update.UpdateProjectRequest;
import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.model.Project;
import com.forkmyfolio.model.Role;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private User user;
    private Project project1;
    private ProjectDto projectDto1;
    private CreateProjectRequest createProjectRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRoles(Set.of(Role.ADMIN)); // Assume admin for creation/update tests

        LocalDateTime now = LocalDateTime.now();
        project1 = new Project();
        project1.setId(1L);
        project1.setTitle("Test Project 1");
        project1.setDescription("Description for project 1");
        project1.setUser(user);
        project1.setTechStack(List.of("Java", "Spring"));
        project1.setCreatedAt(now.minusDays(1));
        project1.setUpdatedAt(now);

        projectDto1 = new ProjectDto(
            project1.getId(), project1.getTitle(), project1.getDescription(),
            project1.getTechStack(), project1.getRepoUrl(), project1.getLiveUrl(),
            project1.getImageUrl(), user.getId(), project1.getCreatedAt(), project1.getUpdatedAt()
        );

        createProjectRequest = new CreateProjectRequest(
            "New Project", "New project description that is long enough.",
            List.of("Kotlin"), "http://repo.new", "http://live.new", "http://image.new"
        );
    }

    @Test
    void getAllProjects_shouldReturnListOfProjectDtos() {
        when(projectRepository.findAll()).thenReturn(Collections.singletonList(project1));
        List<ProjectDto> result = projectService.getAllProjects();
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(project1.getTitle(), result.get(0).getTitle());
        verify(projectRepository).findAll();
    }

    @Test
    void findProjectEntityById_whenExists_shouldReturnProject() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project1));
        Project found = projectService.findProjectEntityById(1L);
        assertNotNull(found);
        assertEquals(project1.getTitle(), found.getTitle());
    }

    @Test
    void findProjectEntityById_whenNotExists_shouldThrowResourceNotFoundException() {
        when(projectRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> projectService.findProjectEntityById(2L));
    }

    @Test
    void getProjectById_whenExists_shouldReturnProjectDto() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project1));
        ProjectDto resultDto = projectService.getProjectById(1L);
        assertNotNull(resultDto);
        assertEquals(project1.getTitle(), resultDto.getTitle());
    }


    @Test
    void createProject_shouldSaveAndReturnProjectDto() {
        // Mock the save operation to return the project with ID and timestamps
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project p = invocation.getArgument(0);
            p.setId(2L); // Simulate DB assigning an ID
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            return p;
        });

        ProjectDto result = projectService.createProject(createProjectRequest, user);

        assertNotNull(result);
        assertEquals(createProjectRequest.getTitle(), result.getTitle());
        assertEquals(user.getId(), result.getUserId());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void updateProject_whenProjectExists_shouldUpdateAndReturnProjectDto() {
        UpdateProjectRequest updateRequest = new UpdateProjectRequest();
        updateRequest.setTitle(Optional.of("Updated Title"));
        updateRequest.setDescription(Optional.of("Updated description which is also long enough."));
        // Assume techStack, repoUrl, liveUrl, imageUrl are not updated in this specific test case for simplicity

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project1));
        // Mock save to return the updated project
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectDto updatedDto = projectService.updateProject(1L, updateRequest, user);

        assertNotNull(updatedDto);
        assertEquals("Updated Title", updatedDto.getTitle());
        assertEquals("Updated description which is also long enough.", updatedDto.getDescription());
        // Verify that original fields not in updateRequest are preserved (implicitly by using project1 fields in assertion)
        assertEquals(project1.getTechStack(), updatedDto.getTechStack());
        verify(projectRepository).findById(1L);
        verify(projectRepository).save(project1); // Ensure the same instance is saved
    }

    @Test
    void updateProject_whenUpdatingOnlyOptionalFields_shouldWork() {
        UpdateProjectRequest updateRequest = new UpdateProjectRequest();
        String newRepoUrl = "http://new.repo.com";
        updateRequest.setRepoUrl(Optional.of(newRepoUrl));

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project1));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectDto updatedDto = projectService.updateProject(1L, updateRequest, user);

        assertEquals(newRepoUrl, updatedDto.getRepoUrl());
        assertEquals(project1.getTitle(), updatedDto.getTitle()); // Title should be unchanged
    }


    @Test
    void updateProject_whenProjectNotExists_shouldThrowResourceNotFoundException() {
        UpdateProjectRequest updateRequest = new UpdateProjectRequest();
        updateRequest.setTitle(Optional.of("Updated Title"));
        when(projectRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.updateProject(2L, updateRequest, user));
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void deleteProject_whenProjectExists_shouldCallRepositoryDelete() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project1));
        doNothing().when(projectRepository).delete(project1);

        projectService.deleteProject(1L, user);

        verify(projectRepository).findById(1L);
        verify(projectRepository).delete(project1);
    }

    @Test
    void deleteProject_whenProjectNotExists_shouldThrowResourceNotFoundException() {
        when(projectRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> projectService.deleteProject(2L, user));
        verify(projectRepository, never()).delete(any(Project.class));
    }

    @Test
    void convertToDto_shouldCorrectlyMapFields() {
        ProjectDto dto = projectService.convertToDto(project1);
        assertEquals(project1.getId(), dto.getId());
        assertEquals(project1.getTitle(), dto.getTitle());
        assertEquals(project1.getDescription(), dto.getDescription());
        assertEquals(project1.getTechStack(), dto.getTechStack());
        assertEquals(project1.getRepoUrl(), dto.getRepoUrl());
        assertEquals(project1.getLiveUrl(), dto.getLiveUrl());
        assertEquals(project1.getImageUrl(), dto.getImageUrl());
        assertEquals(project1.getUser().getId(), dto.getUserId());
        assertEquals(project1.getCreatedAt(), dto.getCreatedAt());
        assertEquals(project1.getUpdatedAt(), dto.getUpdatedAt());
    }

    @Test
    void convertToDto_withNullProject_shouldReturnNull() {
        assertNull(projectService.convertToDto(null));
    }

    @Test
    void convertToDto_withProjectHavingNullUser_shouldHandleNullUserId() {
        project1.setUser(null); // Simulate a project with no assigned user (though our design might not allow this)
        ProjectDto dto = projectService.convertToDto(project1);
        assertNotNull(dto);
        assertNull(dto.getUserId());
    }

    @Test
    void convertCreateRequestToEntity_shouldCorrectlyMapFields() {
        Project entity = projectService.convertCreateRequestToEntity(createProjectRequest, user);
        assertEquals(createProjectRequest.getTitle(), entity.getTitle());
        assertEquals(createProjectRequest.getDescription(), entity.getDescription());
        assertEquals(createProjectRequest.getTechStack(), entity.getTechStack());
        assertEquals(createProjectRequest.getRepoUrl(), entity.getRepoUrl());
        assertEquals(createProjectRequest.getLiveUrl(), entity.getLiveUrl());
        assertEquals(createProjectRequest.getImageUrl(), entity.getImageUrl());
        assertEquals(user, entity.getUser());
        assertNull(entity.getId()); // ID should be null before saving
        assertNull(entity.getCreatedAt()); // Timestamps handled by Hibernate
        assertNull(entity.getUpdatedAt());
    }
}
*/
