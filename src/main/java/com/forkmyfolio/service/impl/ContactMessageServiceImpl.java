package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.create.CreateContactMessageRequest;
import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.mapper.ContactMessageMapper;
import com.forkmyfolio.model.ContactMessage;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.ContactMessageRepository;
import com.forkmyfolio.repository.UserRepository;
import com.forkmyfolio.service.ContactMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContactMessageServiceImpl implements ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;
    private final UserRepository userRepository;
    private final ContactMessageMapper contactMessageMapper;

    @Override
    @Transactional
    public ContactMessage saveMessage(String userSlug, CreateContactMessageRequest request) {
        User targetUser = userRepository.findBySlugAndActiveTrue(userSlug)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with slug: " + userSlug));

        ContactMessage message = contactMessageMapper.toEntity(request);
        message.setUser(targetUser); // Associate message with the portfolio owner
        return contactMessageRepository.save(message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactMessage> findAll() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional
    public void deleteByUuid(UUID uuid) {
        if (!contactMessageRepository.existsByUuid(uuid)) {
            throw new ResourceNotFoundException("ContactMessage not found with UUID: " + uuid);
        }
        contactMessageRepository.deleteByUuid(uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactMessage> getMessagesForUser(User user) {
        return contactMessageRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    @Transactional
    public void deleteMessageAsOwner(UUID messageUuid, User owner) {
        ContactMessage message = contactMessageRepository.findByUuid(messageUuid)
                .orElseThrow(() -> new ResourceNotFoundException("ContactMessage not found with UUID: " + messageUuid));

        if (!message.getUser().getId().equals(owner.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this message.");
        }
        contactMessageRepository.delete(message);
    }
}