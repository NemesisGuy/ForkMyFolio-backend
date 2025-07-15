package com.forkmyfolio.controller;

import com.forkmyfolio.dto.create.CreateContactMessageRequest;
import com.forkmyfolio.mapper.ContactMessageMapper;
import com.forkmyfolio.model.ContactMessage;
import com.forkmyfolio.service.ContactMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

/**
 * Controller for handling incoming contact messages.
 */
@RestController
@RequestMapping("/api/v1/contact-messages")
@Tag(name = "Contact Messages", description = "Endpoints for submitting contact messages")
public class ContactMessageController {

    private final ContactMessageService contactMessageService;
    private final ContactMessageMapper contactMessageMapper;
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ContactMessageController.class);

    /**
     * Constructs a ContactMessageController with the necessary service.
     *
     * @param contactMessageService Service for contact message operations.
     * @param contactMessageMapper  Mapper for converting between entities and DTOs.
     */
    @Autowired
    public ContactMessageController(ContactMessageService contactMessageService, ContactMessageMapper contactMessageMapper) {
        this.contactMessageService = contactMessageService;
        this.contactMessageMapper = contactMessageMapper;
    }

    /**
     * Submits a new contact message.
     * This endpoint is publicly accessible.
     *
     * @param createContactMessageRequest DTO containing the contact message details.
     * @return A map containing a success message.
     */
    @PostMapping
    @Operation(summary = "Submit a new contact message",
            description = "Allows users to submit a contact message. This endpoint is publicly accessible.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Message submitted successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.forkmyfolio.dto.response.ApiResponseWrapper.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.forkmyfolio.dto.response.ApiResponseWrapper.class)))
    })
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> submitContactMessage(@Valid @RequestBody CreateContactMessageRequest createContactMessageRequest) {
        // Log the incoming request with key details at INFO level
        logger.info("Received a new contact message from: {} <{}>",
                createContactMessageRequest.getName(),
                createContactMessageRequest.getEmail());

        // Log the full message content at DEBUG level for development troubleshooting
        logger.debug("Contact message details: {}", createContactMessageRequest);

        // Convert DTO to entity and call the service to save the message
        ContactMessage messageToSave = contactMessageMapper.toEntity(createContactMessageRequest);
        contactMessageService.saveMessage(messageToSave);

        // Log the successful outcome at INFO level
        logger.info("Successfully saved contact message from '{}'.", createContactMessageRequest.getEmail());

        return Collections.singletonMap("message", "Contact message submitted successfully.");
    }
}