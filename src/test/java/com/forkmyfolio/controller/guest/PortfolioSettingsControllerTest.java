package com.forkmyfolio.controller.guest;

import com.forkmyfolio.dto.response.UserSettingDto;
import com.forkmyfolio.model.User;
import com.forkmyfolio.service.UserService;
import com.forkmyfolio.service.impl.UserSettingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PortfolioSettingsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private UserSettingService userSettingService;

    @InjectMocks
    private PortfolioSettingsController portfolioSettingsController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(portfolioSettingsController).build();
    }

    @Test
    void getPortfolioSettings_whenUserExists_shouldReturnSettings() throws Exception {
        // given
        String slug = "test-slug";
        User user = new User();
        user.setSlug(slug);
        UserSettingDto userSettingDto = new UserSettingDto();
        userSettingDto.setName("test-setting");
        userSettingDto.setValue("test-value");

        when(userService.findBySlug(slug)).thenReturn(Optional.of(user));
        when(userSettingService.getEffectiveSettingsForUser(any(User.class))).thenReturn(Collections.singletonList(userSettingDto));

        // when & then
        mockMvc.perform(get("/api/v1/portfolios/{slug}/settings", slug)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("test-setting"))
                .andExpect(jsonPath("$[0].value").value("test-value"));
    }

    @Test
    void getPortfolioSettings_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
        // given
        String slug = "non-existent-slug";
        when(userService.findBySlug(slug)).thenReturn(Optional.empty());

        // when & then
        mockMvc.perform(get("/api/v1/portfolios/{slug}/settings", slug)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
