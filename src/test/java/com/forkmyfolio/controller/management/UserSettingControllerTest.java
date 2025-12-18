package com.forkmyfolio.controller.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forkmyfolio.dto.response.UserSettingDto;
import com.forkmyfolio.dto.update.UpdateUserSettingRequest;
import com.forkmyfolio.service.impl.UserSettingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserSettingControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UserSettingService userSettingService;

    @InjectMocks
    private UserSettingController userSettingController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userSettingController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @WithMockUser
    void getMySettings_shouldReturnUserSettings() throws Exception {
        UserSettingDto settingDto = new UserSettingDto();
        settingDto.setName("theme");
        settingDto.setValue("dark");

        when(userSettingService.getMyEffectiveSettings()).thenReturn(Collections.singletonList(settingDto));

        mockMvc.perform(get("/api/v1/me/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("theme"))
                .andExpect(jsonPath("$.data[0].value").value("dark"));
    }

    @Test
    @WithMockUser
    void updateMySettings_shouldUpdateAndReturnSettings() throws Exception {
        UpdateUserSettingRequest request = new UpdateUserSettingRequest();
        request.setUuid(UUID.randomUUID());
        request.setValue("light");

        UserSettingDto updatedDto = new UserSettingDto();
        updatedDto.setName("theme");
        updatedDto.setValue("light");

        when(userSettingService.updateMySettings(anyList())).thenReturn(Collections.singletonList(updatedDto));

        mockMvc.perform(put("/api/v1/me/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Collections.singletonList(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].value").value("light"));
    }
}
