package com.forkmyfolio.controller;

import com.forkmyfolio.dto.ApiResponse;
import com.forkmyfolio.dto.CreateContactMessageRequest;
// import com.forkmyfolio.dto.ContactMessageDto; // If we decide to return the DTO
import com.forkmyfolio.service.ContactMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections; // Added import

/**
 * Controller for handling incoming contact messages.
 */
@RestController
@RequestMapping("/api/v1/contact-messages")
@Tag(name = "Contact Messages", description = "Endpoints for submitting contact messages")
public class ContactMessageController {

    private final ContactMessageService contactMessageService;

    /**
     * Constructs a ContactMessageController with the necessary service.
     * @param contactMessageService Service for contact message operations.
     */
    @Autowired
    public ContactMessageController(ContactMessageService contactMessageService) {
        this.contactMessageService = contactMessageService;
    }

    /**
     * Submits a new contact message.
     * This endpoint is publicly accessible.
     *
     * @param createContactMessageRequest DTO containing the contact message details.
     * @return ResponseEntity with a success message and HTTP status 201.
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
    public ResponseEntity<com.forkmyfolio.dto.response.ApiResponseWrapper<Object>> submitContactMessage(@Valid @RequestBody CreateContactMessageRequest createContactMessageRequest) {
        contactMessageService.saveMessage(createContactMessageRequest);
        // Use ApiResponseWrapper for success response
        return new ResponseEntity<>(new com.forkmyfolio.dto.response.ApiResponseWrapper<>(Collections.singletonMap("message", "Contact message submitted successfully.")), HttpStatus.CREATED);
    }
}
