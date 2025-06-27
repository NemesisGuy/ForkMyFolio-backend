package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.ContactMessageDto;
import com.forkmyfolio.dto.CreateContactMessageRequest;
import com.forkmyfolio.model.ContactMessage;
import com.forkmyfolio.repository.ContactMessageRepository;
import com.forkmyfolio.service.ContactMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the {@link ContactMessageService} interface.
 * Handles business logic related to contact messages.
 */
@Service
public class ContactMessageServiceImpl implements ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;

    /**
     * Constructs a {@code ContactMessageServiceImpl} with the necessary repository.
     *
     * @param contactMessageRepository The repository for contact message data.
     */
    @Autowired
    public ContactMessageServiceImpl(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ContactMessageDto saveMessage(CreateContactMessageRequest createRequest) {
        ContactMessage contactMessage = convertCreateRequestToEntity(createRequest);
        ContactMessage savedMessage = contactMessageRepository.save(contactMessage);
        // As per requirements, this endpoint returns a "success message".
        // However, returning the created DTO is also a common and useful pattern.
        // For now, let's return the DTO, the controller can decide to wrap it in an ApiResponse.
        return convertToDto(savedMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContactMessageDto convertToDto(ContactMessage messageEntity) {
        if (messageEntity == null) {
            return null;
        }
        return new ContactMessageDto(
                messageEntity.getId(),
                messageEntity.getName(),
                messageEntity.getEmail(),
                messageEntity.getMessage(),
                messageEntity.getCreatedAt()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContactMessage convertCreateRequestToEntity(CreateContactMessageRequest request) {
        ContactMessage contactMessage = new ContactMessage();
        contactMessage.setName(request.getName());
        contactMessage.setEmail(request.getEmail());
        contactMessage.setMessage(request.getMessage());
        // createdAt will be handled by Hibernate @CreationTimestamp
        return contactMessage;
    }
}
