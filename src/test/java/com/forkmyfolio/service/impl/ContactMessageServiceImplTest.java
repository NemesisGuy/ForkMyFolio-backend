package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.ContactMessageDto;
import com.forkmyfolio.dto.CreateContactMessageRequest;
import com.forkmyfolio.model.ContactMessage;
import com.forkmyfolio.repository.ContactMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ContactMessageServiceImplTest {

    @Mock
    private ContactMessageRepository contactMessageRepository;

    @InjectMocks
    private ContactMessageServiceImpl contactMessageService;

    private CreateContactMessageRequest createRequest;
    private ContactMessage contactMessage;

    @BeforeEach
    void setUp() {
        createRequest = new CreateContactMessageRequest(
                "John Doe",
                "john.doe@example.com",
                "This is a test message, and it is definitely long enough."
        );

        contactMessage = new ContactMessage();
        contactMessage.setId(1L);
        contactMessage.setName(createRequest.getName());
        contactMessage.setEmail(createRequest.getEmail());
        contactMessage.setMessage(createRequest.getMessage());
        contactMessage.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void saveMessage_shouldSaveAndReturnContactMessageDto() {
        // Mock the save operation to return the message with ID and timestamp
        when(contactMessageRepository.save(any(ContactMessage.class))).thenAnswer(invocation -> {
            ContactMessage msg = invocation.getArgument(0);
            msg.setId(1L); // Simulate DB assigning an ID
            msg.setCreatedAt(LocalDateTime.now()); // Simulate DB setting timestamp
            return msg;
        });

        ContactMessageDto resultDto = contactMessageService.saveMessage(createRequest);

        assertNotNull(resultDto);
        assertEquals(createRequest.getName(), resultDto.getName());
        assertEquals(createRequest.getEmail(), resultDto.getEmail());
        assertEquals(createRequest.getMessage(), resultDto.getMessage());
        assertNotNull(resultDto.getCreatedAt()); // Check that timestamp is set
        verify(contactMessageRepository).save(any(ContactMessage.class));
    }

    @Test
    void convertToDto_shouldCorrectlyMapFields() {
        ContactMessageDto dto = contactMessageService.convertToDto(contactMessage);
        assertEquals(contactMessage.getId(), dto.getId());
        assertEquals(contactMessage.getName(), dto.getName());
        assertEquals(contactMessage.getEmail(), dto.getEmail());
        assertEquals(contactMessage.getMessage(), dto.getMessage());
        assertEquals(contactMessage.getCreatedAt(), dto.getCreatedAt());
    }

    @Test
    void convertToDto_withNullMessage_shouldReturnNull() {
        assertNull(contactMessageService.convertToDto(null));
    }

    @Test
    void convertCreateRequestToEntity_shouldCorrectlyMapFields() {
        ContactMessage entity = contactMessageService.convertCreateRequestToEntity(createRequest);
        assertEquals(createRequest.getName(), entity.getName());
        assertEquals(createRequest.getEmail(), entity.getEmail());
        assertEquals(createRequest.getMessage(), entity.getMessage());
        assertNull(entity.getId()); // ID should be null before saving
        assertNull(entity.getCreatedAt()); // Timestamp handled by Hibernate
    }
}
