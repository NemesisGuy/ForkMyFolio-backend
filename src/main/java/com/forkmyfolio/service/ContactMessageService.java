package com.forkmyfolio.service;

import com.forkmyfolio.dto.create.CreateContactMessageRequest;
import com.forkmyfolio.model.ContactMessage;
import com.forkmyfolio.model.User;

import java.util.List;
import java.util.UUID;

public interface ContactMessageService {
    ContactMessage saveMessage(String userSlug, CreateContactMessageRequest request);
    List<ContactMessage> findAll();
    void deleteByUuid(UUID uuid);
    List<ContactMessage> getMessagesForUser(User user);
    void deleteMessageAsOwner(UUID messageUuid, User owner);
}