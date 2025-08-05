package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.create.CreateContactMessageRequest;
import com.forkmyfolio.dto.response.ContactMessageDto;
import com.forkmyfolio.dto.response.UnreadMessageCountDto;
import com.forkmyfolio.dto.update.UpdateContactMessageRequest;
import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.mapper.ContactMessageMapper;
import com.forkmyfolio.model.ContactMessage;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.ContactMessageRepository;
import com.forkmyfolio.repository.UserRepository;
import com.forkmyfolio.service.ContactMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
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
    public List<ContactMessageDto> findAll() {
        List<ContactMessage> messages = contactMessageRepository.findAllByOrderByCreatedAtDesc();
        return contactMessageMapper.toDtoList(messages);
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
    public List<ContactMessageDto> getMessagesForUser(User user) {
        List<ContactMessage> messages = contactMessageRepository.findByUserOrderByCreatedAtDesc(user);
        return contactMessageMapper.toDtoList(messages);
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

    @Override
    @Transactional(readOnly = true)
    public UnreadMessageCountDto getUnreadMessageCount(User user) {
        log.debug("Counting unread messages for user: {}", user.getEmail());
        long count = contactMessageRepository.countByUserAndIsReadFalseAndIsArchivedFalse(user);
        log.info("Found {} unread messages for user: {}", count, user.getEmail());
        return new UnreadMessageCountDto(count);
    }

    @Override
    @Transactional
    public ContactMessageDto updateMessage(UUID messageUuid, UpdateContactMessageRequest request, User owner) {
        log.debug("Attempting to update message {} for user {}", messageUuid, owner.getEmail());
        ContactMessage message = contactMessageRepository.findByUuid(messageUuid)
                .orElseThrow(() -> new ResourceNotFoundException("ContactMessage not found with UUID: " + messageUuid));

        if (!message.getUser().getId().equals(owner.getId())) {
            log.warn("User {} attempted to update message {}, but does not own it.", owner.getEmail(), messageUuid);
            throw new AccessDeniedException("You are not authorized to modify this message.");
        }

        // Partially update fields only if they are provided in the request
        if (request.getPriority() != null) {
            message.setPriority(request.getPriority());
        }
        if (request.getRead() != null) {
            message.setRead(request.getRead());
        }
        if (request.getReplied() != null) {
            message.setReplied(request.getReplied());
        }
        if (request.getArchived() != null) {
            message.setArchived(request.getArchived());
        }

        ContactMessage updatedMessage = contactMessageRepository.save(message);
        log.info("Successfully updated message {}", messageUuid);

        return contactMessageMapper.toDto(updatedMessage);
    }
}