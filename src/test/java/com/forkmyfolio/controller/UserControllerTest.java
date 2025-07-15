package com.forkmyfolio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forkmyfolio.dto.response.UserDto;
import com.forkmyfolio.model.Role;
import com.forkmyfolio.model.User;
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
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;

/**
 * Integration tests for {@link UserController}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User mockUser;
    private UserDto mockUserDto;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("testuser@example.com");
        mockUser.setFirstName("Test");
        mockUser.setLastName("User");
        mockUser.setPassword("password"); // Not directly used in response, but good for completeness
        mockUser.setRoles(Set.of(Role.USER));
        mockUser.setProfileImageUrl("http://example.com/profile.jpg");
        mockUser.setCreatedAt(LocalDateTime.now().minusDays(1));
        mockUser.setUpdatedAt(LocalDateTime.now());


        mockUserDto = new UserDto(
                mockUser.getId(),
                mockUser.getEmail(),
                mockUser.getFirstName(),
                mockUser.getLastName(),
                mockUser.getProfileImageUrl(),
                mockUser.getRoles(),
                mockUser.getCreatedAt()
        );
    }
/*

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"USER"})
    void getCurrentUserProfile_whenAuthenticated_shouldReturnUserProfile() throws Exception {
        given(userService.getCurrentAuthenticatedUser()).willReturn(mockUser);
        given(userService.convertToDto(mockUser)).willReturn(mockUserDto);

        mockMvc.perform(get("/api/v1/users/me/profile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.id", is(mockUser.getId().intValue())))
                .andExpect(jsonPath("$.data.email", is(mockUser.getEmail())))
                .andExpect(jsonPath("$.data.firstName", is(mockUser.getFirstName())))
                .andExpect(jsonPath("$.data.lastName", is(mockUser.getLastName())))
                .andExpect(jsonPath("$.data.profileImageUrl", is(mockUser.getProfileImageUrl())));
    }
*/

    @Test
    void getCurrentUserProfile_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
        // No @WithMockUser, so request is anonymous
        mockMvc.perform(get("/api/v1/users/me/profile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("unauthorized")))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors[0].field", is("authentication")))
                .andExpect(jsonPath("$.errors[0].message").value(org.hamcrest.Matchers.containsString("Full authentication is required")));
    }
}
