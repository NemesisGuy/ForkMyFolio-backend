/*
package com.forkmyfolio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forkmyfolio.dto.create.CreateContactMessageRequest;
import com.forkmyfolio.dto.response.ContactMessageDto; // Import if service returns DTO
import com.forkmyfolio.service.ContactMessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class ContactMessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactMessageService contactMessageService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void submitContactMessage_withValidData_shouldReturnSuccess() throws Exception {
        CreateContactMessageRequest request = new CreateContactMessageRequest("John Doe", "john.doe@example.com", "This is a test message.");

        // If service returns DTO (even if controller wraps it)
        ContactMessageDto mockSavedDto = new ContactMessageDto(1L, request.getName(), request.getEmail(), request.getMessage(), LocalDateTime.now());
        given(contactMessageService.saveMessage(any(CreateContactMessageRequest.class))).willReturn(mockSavedDto);


        mockMvc.perform(post("/api/v1/contact-messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.message", is("Contact message submitted successfully.")));
    }

    @Test
    void submitContactMessage_withInvalidData_shouldReturnBadRequest() throws Exception {
        CreateContactMessageRequest request = new CreateContactMessageRequest("", "not-an-email", "short"); // Invalid data

        // No need to mock service call as validation should fail before that

        mockMvc.perform(post("/api/v1/contact-messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("validation_failed")))
                .andExpect(jsonPath("$.errors[?(@.field == 'name')].message").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'email')].message").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'message')].message").exists());
    }
}
*/
