/*
package com.forkmyfolio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forkmyfolio.dto.create.CreateSkillRequest;
import com.forkmyfolio.dto.response.SkillDto;
import com.forkmyfolio.model.Role;
import com.forkmyfolio.model.Skill;
import com.forkmyfolio.model.User;
import com.forkmyfolio.service.SkillService;
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
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow; // Added import
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class SkillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SkillService skillService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User adminUser;
    private SkillDto skillDto1;
    private SkillDto skillDto2;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setEmail("admin@example.com");
        adminUser.setRoles(Set.of(Role.ADMIN));

        LocalDateTime now = LocalDateTime.now();

        skillDto1 = new SkillDto(1L, "Java", Skill.SkillLevel.EXPERT, 1L, now.minusDays(2), now.minusHours(1));
        skillDto2 = new SkillDto(2L, "Spring Boot", Skill.SkillLevel.INTERMEDIATE, 1L, now.minusDays(1), now);
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void createSkill_asAdmin_withInvalidData_shouldReturnBadRequest() throws Exception {
        // CreateSkillRequest has @NotBlank on name and @NotNull on level
        CreateSkillRequest createRequest = new CreateSkillRequest(null, null); // Invalid: null name and level

        mockMvc.perform(post("/api/v1/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("validation_failed")))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors", hasSize(2))) // Expecting errors for name and level
                .andExpect(jsonPath("$.errors[?(@.field == 'name')].message").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'level')].message").exists());
    }

    @Test
    void getSkillById_whenSkillNotFound_shouldReturnNotFound() throws Exception {
        given(skillService.getSkillById(99L)).willThrow(new com.forkmyfolio.exception.ResourceNotFoundException("Skill not found with id 99"));

        mockMvc.perform(get("/api/v1/skills/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("fail")))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors[0].field", is("resource")))
                .andExpect(jsonPath("$.errors[0].message", is("Skill not found with id 99")));
    }

    @Test
    void getAllSkills_shouldReturnListOfSkills() throws Exception {
        given(skillService.getAllSkills()).willReturn(Arrays.asList(skillDto1, skillDto2));

        mockMvc.perform(get("/api/v1/skills"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].name", is("Java")))
                .andExpect(jsonPath("$.data[1].name", is("Spring Boot")));
    }

    @Test
    void getSkillById_whenSkillExists_shouldReturnSkill() throws Exception {
        given(skillService.getSkillById(1L)).willReturn(skillDto1);

        mockMvc.perform(get("/api/v1/skills/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.name", is("Java")));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void createSkill_asAdmin_shouldReturnCreatedSkill() throws Exception {
        CreateSkillRequest createRequest = new CreateSkillRequest("Python", Skill.SkillLevel.EXPERT);
        SkillDto createdDto = new SkillDto(3L, "Python", Skill.SkillLevel.EXPERT, 1L, LocalDateTime.now(), LocalDateTime.now());

        given(userService.getCurrentAuthenticatedUser()).willReturn(adminUser);
        given(skillService.createSkill(any(CreateSkillRequest.class), eq(adminUser))).willReturn(createdDto);

        mockMvc.perform(post("/api/v1/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.name", is("Python")));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void createSkill_asUser_shouldReturnForbidden() throws Exception {
        CreateSkillRequest createRequest = new CreateSkillRequest("Illegal Skill", Skill.SkillLevel.BEGINNER);

        mockMvc.perform(post("/api/v1/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is("forbidden")))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors[0].field", is("authorization")))
                .andExpect(jsonPath("$.errors[0].message", is("Access Denied: You do not have permission to access this resource.")));
    }

   */
/* @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void deleteSkill_asAdmin_shouldReturnSuccess() throws Exception {
        given(userService.getCurrentAuthenticatedUser()).willReturn(adminUser);
        doNothing().when(skillService).deleteSkill(eq(1L), eq(adminUser));

        mockMvc.perform(delete("/api/v1/skills/1"))
                .andExpect(status().isNoContent());
    }*//*


    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void deleteSkill_asUser_shouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/skills/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is("forbidden")))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors[0].field", is("authorization")))
                .andExpect(jsonPath("$.errors[0].message", is("Access Denied: You do not have permission to access this resource.")));
    }
*/
/*
    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void deleteSkill_asAdmin_whenSkillNotFound_shouldReturnNotFound() throws Exception {
        given(userService.getCurrentAuthenticatedUser()).willReturn(adminUser);
        doThrow(new com.forkmyfolio.exception.ResourceNotFoundException("Skill not found with id 99"))
            .when(skillService).deleteSkill(eq(99L), eq(adminUser));

        mockMvc.perform(delete("/api/v1/skills/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is("fail")))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors[0].field", is("resource")))
                .andExpect(jsonPath("$.errors[0].message", is("Skill not found with id 99")));
    }*//*

}
*/
