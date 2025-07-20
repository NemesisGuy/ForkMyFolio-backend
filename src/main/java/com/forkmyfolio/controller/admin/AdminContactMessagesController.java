package com.forkmyfolio.controller.admin;

import com.forkmyfolio.dto.response.ContactMessageDto;
import com.forkmyfolio.mapper.ContactMessageMapper;
import com.forkmyfolio.service.ContactMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


/**
 * Controller for admin-only operations related to contact form messages.
 * All endpoints are secured and require ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/v1/admin/contact-messages")
@RequiredArgsConstructor
@Tag(name = "Admin: Contact Messages", description = "Endpoints for managing contact form submissions")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminContactMessagesController {

    private final ContactMessageService contactMessageService;
    private final ContactMessageMapper contactMessageMapper;

    /**
     * Retrieves all contact messages from the database.
     *
     * @return A list of all contact messages.
     */
    @Operation(summary = "Get all contact messages", description = "Fetches a list of all messages submitted through the contact form.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all messages")
    @GetMapping
    public ResponseEntity<List<ContactMessageDto>> getAllContactMessages() {
        var messages = contactMessageService.findAll();
        return ResponseEntity.ok(contactMessageMapper.toDtoList(messages));
    }

    /**
     * Deletes a contact message by its unique identifier.
     *
     * @param uuid The UUID of the message to delete.
     * @return An HTTP 204 No Content response on successful deletion.
     */
    @Operation(summary = "Delete a contact message", description = "Deletes a specific contact message by its UUID.")
    @ApiResponse(responseCode = "204", description = "Message deleted successfully")
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteContactMessage(@PathVariable UUID uuid) {
        contactMessageService.deleteByUuid(uuid);
        return ResponseEntity.noContent().build();
    }
}