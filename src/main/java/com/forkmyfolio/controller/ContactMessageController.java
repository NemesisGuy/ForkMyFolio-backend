package com.forkmyfolio.controller;

import com.forkmyfolio.aop.TrackVisitor;
import com.forkmyfolio.dto.create.CreateContactMessageRequest;
import com.forkmyfolio.mapper.ContactMessageMapper;
import com.forkmyfolio.model.ContactMessage;
import com.forkmyfolio.model.User;
import com.forkmyfolio.model.enums.VisitorStatType;
import com.forkmyfolio.service.ContactMessageService;
import com.forkmyfolio.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/portfolios/{slug}/contact-messages")
@Tag(name = "Contact Messages", description = "Endpoints for submitting contact messages to a specific user.")
@RequiredArgsConstructor
@Slf4j
public class ContactMessageController {

    private final ContactMessageService contactMessageService;
    private final UserService userService;
    private final ContactMessageMapper contactMessageMapper;

    @PostMapping
    @Operation(summary = "Submit a new contact message to a user",
            description = "Allows visitors to submit a contact message to the user identified by the slug.")
    @ResponseStatus(HttpStatus.CREATED)
    @TrackVisitor(VisitorStatType.CONTACT_MESSAGE_SUBMISSION)
    public Map<String, String> submitContactMessage(
            @Parameter(description = "The unique, URL-friendly slug of the user to contact.", example = "jane-doe")
            @PathVariable String slug,
            @Valid @RequestBody CreateContactMessageRequest createRequest) {

        log.info("Received contact message for user with slug '{}' from: {}", slug, createRequest.getEmail());
        contactMessageService.saveMessage(slug, createRequest);
        log.info("Successfully saved contact message for user with slug '{}'.", slug);

        return Map.of("message", "Contact message submitted successfully.");
    }
}