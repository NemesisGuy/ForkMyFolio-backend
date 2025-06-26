package com.forkmyfolio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forkmyfolio.dto.CreateProjectRequest;
import com.forkmyfolio.dto.ProjectDto;
import com.forkmyfolio.dto.UpdateProjectRequest;
import com.forkmyfolio.model.Project;
import com.forkmyfolio.model.Role;
import com.forkmyfolio.model.User;
import com.forkmyfolio.service.ProjectService;
import com.forkmyfolio.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private UserService userService; // Mocked because ProjectController uses it to get current user

    @Autowired
    private ObjectMapper objectMapper;

    private User adminUser;
    private ProjectDto projectDto1;
    private ProjectDto projectDto2;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setEmail("admin@example.com");
        adminUser.setRoles(Set.of(Role.ADMIN));

        LocalDateTime now = LocalDateTime.now();
        projectDto1 = new ProjectDto(1L, "Project Alpha", "Desc Alpha", List.of("Java"), "repo1", "live1", "img1", 1L, now, now);
        projectDto2 = new ProjectDto(2L, "Project Beta", "Desc Beta", List.of("Python"), "repo2", "live2", "img2", 1L, now.minusDays(1), now.minusHours(5));
    }

    @Test
    void getAllProjects_shouldReturnListOfProjects() throws Exception {
        given(projectService.getAllProjects()).willReturn(Arrays.asList(projectDto1, projectDto2));

        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].title", is("Project Alpha")))
                .andExpect(jsonPath("$.data[1].title", is("Project Beta")));
    }

    @Test
    void getProjectById_whenProjectExists_shouldReturnProject() throws Exception {
        given(projectService.getProjectById(1L)).willReturn(projectDto1);

        mockMvc.perform(get("/api/v1/projects/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.title", is("Project Alpha")));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void createProject_asAdmin_shouldReturnCreatedProject() throws Exception {
        CreateProjectRequest createRequest = new CreateProjectRequest("New Valid Project", "This is a valid project description.", List.of("Go"), "http://example.com/repo", "http://example.com/live", "http://example.com/image.png");
        ProjectDto createdDto = new ProjectDto(3L, "New Valid Project", "This is a valid project description.", List.of("Go"), "http://example.com/repo", "http://example.com/live", "http://example.com/image.png", 1L, LocalDateTime.now(), LocalDateTime.now());

        given(userService.getCurrentAuthenticatedUser()).willReturn(adminUser);
        given(projectService.createProject(any(CreateProjectRequest.class), eq(adminUser))).willReturn(createdDto);

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.title", is("New Valid Project")));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void createProject_asUser_shouldReturnForbidden() throws Exception {
        // Use a valid payload here to ensure the 403 is due to authorization, not validation
        CreateProjectRequest createRequest = new CreateProjectRequest("User Project Attempt", "Valid description for user project attempt.", null, "http://example.com/user_repo", null, null);

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void updateProject_asAdmin_shouldReturnUpdatedProject() throws Exception {
        UpdateProjectRequest updateRequest = new UpdateProjectRequest();
        updateRequest.setTitle(Optional.of("Updated Title"));

        ProjectDto updatedDto = new ProjectDto(1L, "Updated Title", projectDto1.getDescription(), projectDto1.getTechStack(), projectDto1.getRepoUrl(), projectDto1.getLiveUrl(), projectDto1.getImageUrl(), 1L, projectDto1.getCreatedAt(), LocalDateTime.now());

        given(userService.getCurrentAuthenticatedUser()).willReturn(adminUser);
        given(projectService.updateProject(eq(1L), any(UpdateProjectRequest.class), eq(adminUser))).willReturn(updatedDto);

        mockMvc.perform(put("/api/v1/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.title", is("Updated Title")));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void updateProject_asUser_shouldReturnForbidden() throws Exception {
        UpdateProjectRequest updateRequest = new UpdateProjectRequest();
        updateRequest.setTitle(Optional.of("Updated Title by User"));

        mockMvc.perform(put("/api/v1/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void deleteProject_asAdmin_shouldReturnSuccess() throws Exception {
        given(userService.getCurrentAuthenticatedUser()).willReturn(adminUser);
        doNothing().when(projectService).deleteProject(eq(1L), eq(adminUser));

        mockMvc.perform(delete("/api/v1/projects/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void deleteProject_asUser_shouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/projects/1"))
                .andExpect(status().isForbidden());
    }
}
