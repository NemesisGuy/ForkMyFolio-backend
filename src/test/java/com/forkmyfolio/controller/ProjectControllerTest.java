/*
package com.forkmyfolio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forkmyfolio.dto.create.CreateProjectRequest;
import com.forkmyfolio.dto.response.ProjectDto;
import com.forkmyfolio.dto.update.UpdateProjectRequest;
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
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void updateProject_asAdmin_withInvalidData_shouldReturnBadRequest() throws Exception {
        UpdateProjectRequest updateRequest = new UpdateProjectRequest();
        // Assuming title and description have @NotBlank or @Size constraints
        // To make it invalid, let's try setting an empty title if it's not allowed
        // Or a description that's too short if there's a @Size(min=...)
        // For this example, let's assume an empty title is invalid if title is Optional<String>
        // but the DTO has @NotBlank on the String field itself.
        // Let's use a DTO that would be invalid if title was mandatory and missing from Optional
        // For UpdateProjectRequest, fields are Optional. If we want to test validation
        // on the *content* of the Optional, the DTO's internal validation (if any) would apply.
        // For this example, let's simulate a constraint on title length if present.
        // The UpdateProjectRequest has Optional<String> title. Validation is on the String inside Optional if @Valid is used on Optional content.
        // Let's assume a direct (non-optional) String field in a hypothetical validated DTO to show concept:
        // Create an invalid request, e.g. if description was mandatory and set to empty.
        // For UpdateProjectRequest, all fields are optional.
        // Let's create a request that is valid in structure but might fail other business validation IF those were in place
        // For now, testing basic validation on UpdateProjectRequest as defined might be limited if all fields are optional.
        // Let's assume we add a @Size(min=3) to UpdateProjectRequest.titleContent if it were a String.
        // Since UpdateProjectRequest has Optional<String> fields, most @NotBlank style validation won't apply unless the Optional itself is validated or its content.
        // Let's test with a value that might be invalid for a field like repoUrl (if it expects URL format)

        UpdateProjectRequest invalidUpdateRequest = new UpdateProjectRequest();
        invalidUpdateRequest.setRepoUrl(Optional.of("not-a-url")); // Assuming @URL or similar on Project.repoUrl

        // For this to work, Project entity's repoUrl would need @URL
        // and UpdateProjectRequest.repoUrl would need @Valid on the Optional or its content.
        // Let's assume the DTO itself has validation like @Size for a title if present.
        // If UpdateProjectRequest had: Optional<@Size(min = 10) String> title;
        // then this would be invalid:
        invalidUpdateRequest.setTitle(Optional.of("short"));


        // Given the current DTO (all Optionals, no explicit validation annotations within UpdateProjectRequest for the Optional's content)
        // it's hard to make it fail validation directly unless we add such annotations.
        // Let's simulate a case where the *payload* is malformed instead, or a field type is wrong.
        // This is more of a generic 400 test rather than specific field validation.
        // For a more targeted validation test, UpdateProjectRequest would need internal constraints.

        String malformedJsonRequest = "{\"title\":\"New Title\", \"description\": \"Valid Description\", \"techStack\": \"not-a-list\"}";


        mockMvc.perform(put("/api/v1/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJsonRequest))
                .andExpect(status().isBadRequest()) // Expecting a 400 due to malformed JSON for techStack
                .andExpect(jsonPath("$.status", is("error"))) // Or validation_failed depending on how Spring handles deserialization error
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void updateProject_asAdmin_whenProjectNotFound_shouldReturnNotFound() throws Exception {
        UpdateProjectRequest updateRequest = new UpdateProjectRequest();
        updateRequest.setTitle(Optional.of("Valid Title"));

        given(userService.getCurrentAuthenticatedUser()).willReturn(adminUser);
        given(projectService.updateProject(eq(99L), any(UpdateProjectRequest.class), eq(adminUser)))
                .willThrow(new com.forkmyfolio.exception.ResourceNotFoundException("Project not found with id 99"));

        mockMvc.perform(put("/api/v1/projects/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is("fail")))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors[0].field", is("resource")))
                .andExpect(jsonPath("$.errors[0].message", is("Project not found with id 99")));
    }

    @Test
    void getProjectById_whenProjectNotFound_shouldReturnNotFound() throws Exception {
        given(projectService.getProjectById(99L)).willThrow(new com.forkmyfolio.exception.ResourceNotFoundException("Project not found with id 99"));

        mockMvc.perform(get("/api/v1/projects/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("fail")))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors[0].field", is("resource")))
                .andExpect(jsonPath("$.errors[0].message", is("Project not found with id 99")));
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
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is("forbidden")))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors[0].field", is("authorization")))
                .andExpect(jsonPath("$.errors[0].message", is("Access Denied: You do not have permission to access this resource.")));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void createProject_asAdmin_withInvalidData_shouldReturnBadRequest() throws Exception {
        CreateProjectRequest createRequest = new CreateProjectRequest(null, "Short", null, null, null, null); // Invalid: null title, short description

        // No need to mock userService or projectService as validation should prevent service calls

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("validation_failed")))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors", hasSize(2))) // Expecting errors for title and description
                .andExpect(jsonPath("$.errors[?(@.field == 'title')].message").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'description')].message").exists());
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
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is("forbidden")))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors[0].field", is("authorization")))
                .andExpect(jsonPath("$.errors[0].message", is("Access Denied: You do not have permission to access this resource.")));
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
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is("forbidden")))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors[0].field", is("authorization")))
                .andExpect(jsonPath("$.errors[0].message", is("Access Denied: You do not have permission to access this resource.")));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void deleteProject_asAdmin_whenProjectNotFound_shouldReturnNotFound() throws Exception {
        given(userService.getCurrentAuthenticatedUser()).willReturn(adminUser);
        // Simulate service throwing ResourceNotFoundException
        doNothing().when(projectService).deleteProject(eq(99L), eq(adminUser)); // This mock needs to throw
        org.mockito.Mockito.doThrow(new com.forkmyfolio.exception.ResourceNotFoundException("Project not found with id 99"))
            .when(projectService).deleteProject(eq(99L), eq(adminUser));


        mockMvc.perform(delete("/api/v1/projects/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is("fail")))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors[0].field", is("resource")))
                .andExpect(jsonPath("$.errors[0].message", is("Project not found with id 99")));
    }
}
*/
