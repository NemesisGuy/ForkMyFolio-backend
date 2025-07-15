package com.forkmyfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for representing ContactMessage information in API responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactMessageDto {

    /**
     * Unique identifier for the contact message.
     */
    private Long id;

    /**
     * Name of the person who sent the message.
     */
    private String name;

    /**
     * Email address of the sender.
     */
    private String email;

    /**
     * The content of the message.
     */
    private String message;

    /**
     * Timestamp of when the message was created/received.
     */
    private LocalDateTime createdAt;
}
