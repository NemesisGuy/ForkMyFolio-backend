package com.forkmyfolio.service;

import com.forkmyfolio.dto.create.CreateContactMessageRequest;
import com.forkmyfolio.dto.response.ContactMessageDto;
import com.forkmyfolio.dto.response.UnreadMessageCountDto;
import com.forkmyfolio.dto.update.UpdateContactMessageRequest;
import com.forkmyfolio.model.ContactMessage;
import com.forkmyfolio.model.User;

import java.util.List;
import java.util.UUID;

public interface ContactMessageService {
    ContactMessage saveMessage(String userSlug, CreateContactMessageRequest request);

    List<ContactMessageDto> findAll();

    void deleteByUuid(UUID uuid);

    List<ContactMessageDto> getMessagesForUser(User user);

    void deleteMessageAsOwner(UUID messageUuid, User owner);

    UnreadMessageCountDto getUnreadMessageCount(User user);

    /**
     * Updates a message's attributes based on the provided request.
     *
     * @param messageUuid The UUID of the message to update.
     * @param request     The DTO containing the fields to update.
     * @param owner       The user who owns the message.
     * @return The updated message as a DTO.
     */
    ContactMessageDto updateMessage(UUID messageUuid, UpdateContactMessageRequest request, User owner);
}