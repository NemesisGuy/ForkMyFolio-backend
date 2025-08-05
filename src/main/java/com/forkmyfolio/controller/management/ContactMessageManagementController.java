package com.forkmyfolio.controller.management;

import com.forkmyfolio.advice.ApiResponseWrapper;
import com.forkmyfolio.dto.response.ContactMessageDto;
import com.forkmyfolio.dto.response.UnreadMessageCountDto;
import com.forkmyfolio.dto.update.UpdateContactMessageRequest;
import com.forkmyfolio.model.User;
import com.forkmyfolio.service.ContactMessageService;
import com.forkmyfolio.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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

    @GetMapping
    @Operation(summary = "Get all of my received contact messages", description = "Retrieves a list of all contact messages sent to the authenticated user.")
    public ResponseEntity<ApiResponseWrapper<List<ContactMessageDto>>> getMyMessages() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        List<ContactMessageDto> messages = contactMessageService.getMessagesForUser(currentUser);
        return ResponseEntity.ok(new ApiResponseWrapper<>(messages));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get my unread message count", description = "Retrieves the number of unread, non-archived messages for the authenticated user.")
    public ResponseEntity<ApiResponseWrapper<UnreadMessageCountDto>> getUnreadMessageCount() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        UnreadMessageCountDto countDto = contactMessageService.getUnreadMessageCount(currentUser);
        return ResponseEntity.ok(new ApiResponseWrapper<>(countDto));
    }

    @PutMapping("/{uuid}")
    @Operation(summary = "Update a contact message", description = "Partially updates a message's attributes like priority, read status, etc. Only include the fields you want to change.")
    public ResponseEntity<ApiResponseWrapper<ContactMessageDto>> updateMyMessage(
            @PathVariable UUID uuid,
            @Valid @RequestBody UpdateContactMessageRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        ContactMessageDto updatedMessage = contactMessageService.updateMessage(uuid, request, currentUser);
        return ResponseEntity.ok(new ApiResponseWrapper<>(updatedMessage));
    }

    @DeleteMapping("/{uuid}")
    @Operation(summary = "Delete one of my contact messages", description = "Permanently deletes a specific contact message by its UUID.")
    public ResponseEntity<ApiResponseWrapper<Map<String, String>>> deleteMyMessage(@PathVariable UUID uuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        contactMessageService.deleteMessageAsOwner(uuid, currentUser);
        Map<String, String> response = Map.of("message", "Message deleted successfully.");
        return ResponseEntity.ok(new ApiResponseWrapper<>(response));
    }
}