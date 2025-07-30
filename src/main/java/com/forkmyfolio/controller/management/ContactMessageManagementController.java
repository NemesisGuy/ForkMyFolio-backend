package com.forkmyfolio.controller.management;

import com.forkmyfolio.dto.response.ContactMessageDto;
import com.forkmyfolio.mapper.ContactMessageMapper;
import com.forkmyfolio.model.ContactMessage;
import com.forkmyfolio.model.User;
import com.forkmyfolio.service.ContactMessageService;
import com.forkmyfolio.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me/contact-messages")
@Tag(name = "Contact Message Management (Me)", description = "Endpoints for the authenticated user to manage their received contact messages.")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
public class ContactMessageManagementController {

    private final ContactMessageService contactMessageService;
    private final UserService userService;
    private final ContactMessageMapper contactMessageMapper;

    @GetMapping
    @Operation(summary = "Get all of my received contact messages")
    public ResponseEntity<List<ContactMessageDto>> getMyMessages() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        List<ContactMessage> messages = contactMessageService.getMessagesForUser(currentUser);
        return ResponseEntity.ok(contactMessageMapper.toDtoList(messages));
    }

    @DeleteMapping("/{uuid}")
    @Operation(summary = "Delete one of my contact messages")
    public ResponseEntity<Void> deleteMyMessage(@PathVariable UUID uuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        contactMessageService.deleteMessageAsOwner(uuid, currentUser);
        return ResponseEntity.noContent().build();
    }
}