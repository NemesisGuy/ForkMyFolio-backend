package com.forkmyfolio.service.impl;

import com.forkmyfolio.model.ContactMessage;
import com.forkmyfolio.repository.ContactMessageRepository;
import com.forkmyfolio.service.ContactMessageService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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
    public ContactMessage saveMessage(ContactMessage message) {
        return contactMessageRepository.save(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ContactMessage> findAll() {
        return contactMessageRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteByUuid(UUID uuid) {
        // First, find the entity by its UUID.
        // This ensures the entity exists before attempting to delete it.
        ContactMessage messageToDelete = contactMessageRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("ContactMessage not found with UUID: " + uuid));
        // Then, delete the found entity.
        contactMessageRepository.delete(messageToDelete);
    }

}
