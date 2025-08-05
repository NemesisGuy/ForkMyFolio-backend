package com.forkmyfolio.mapper;

import com.forkmyfolio.dto.create.CreateContactMessageRequest;
import com.forkmyfolio.dto.response.ContactMessageDto;
import com.forkmyfolio.model.ContactMessage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Component responsible for mapping between ContactMessage entities and DTOs.
 */
@Component
public class ContactMessageMapper {

    public ContactMessage toEntity(CreateContactMessageRequest request) {
        if (request == null) {
            return null;
        }
        ContactMessage message = new ContactMessage();
        message.setName(request.getName());
        message.setEmail(request.getEmail());
        message.setMessage(request.getMessage());
        return message;
    }

    public ContactMessageDto toDto(ContactMessage entity) {
        if (entity == null) {
            return null;
        }
        return new ContactMessageDto(
                entity.getUuid(),
                entity.getName(),
                entity.getEmail(),
                entity.getMessage(),
                entity.getCreatedAt(),
                entity.isRead(),
                entity.getPriority().name(), // Convert enum to string
                entity.isReplied(),
                entity.isArchived()
        );
    }

    public List<ContactMessageDto> toDtoList(List<ContactMessage> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}