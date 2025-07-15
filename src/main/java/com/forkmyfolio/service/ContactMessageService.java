package com.forkmyfolio.service;

import com.forkmyfolio.model.ContactMessage;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing contact messages.
 */
public interface ContactMessageService {

    /**
     * Saves a new contact message submitted through the platform.
     *
     * @param message The {@link ContactMessage} entity to save.
     * @return The saved {@link ContactMessage} entity.
     */
    ContactMessage saveMessage(ContactMessage message);

    /**
     * Finds all contact messages, typically for admin purposes.
     * @return a list of all {@link ContactMessage} entities.
     */
    List<ContactMessage> findAll();

    /**
     * Deletes a contact message by its UUID.
     * @param uuid The UUID of the message to delete.
     */
    void deleteByUuid(UUID uuid);
}
