package com.forkmyfolio.service;

import com.forkmyfolio.dto.ContactMessageDto;
import com.forkmyfolio.dto.CreateContactMessageRequest;
import com.forkmyfolio.model.ContactMessage;

/**
 * Service interface for managing contact messages.
 */
public interface ContactMessageService {

    /**
     * Saves a new contact message submitted through the platform.
     *
     * @param createRequest DTO containing the details of the contact message.
     * @return The saved {@link ContactMessageDto}.
     */
    ContactMessageDto saveMessage(CreateContactMessageRequest createRequest);

    /**
     * Converts a {@link ContactMessage} entity to a {@link ContactMessageDto}.
     * @param messageEntity The contact message entity.
     * @return The corresponding DTO.
     */
    ContactMessageDto convertToDto(ContactMessage messageEntity);

    /**
     * Converts a {@link CreateContactMessageRequest} DTO to a {@link ContactMessage} entity.
     * @param request The DTO.
     * @return The contact message entity.
     */
    ContactMessage convertCreateRequestToEntity(CreateContactMessageRequest request);
}
