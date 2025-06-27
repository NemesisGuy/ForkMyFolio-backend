package com.forkmyfolio.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a contact message submitted through the platform.
 */
@Entity
@Table(name = "contact_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactMessage {

    /**
     * Unique identifier for the contact message.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the person who sent the message.
     * Cannot be blank.
     */
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false)
    private String name;

    /**
     * Email address of the sender.
     * Must be a valid email format and cannot be blank.
     */
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Column(nullable = false)
    private String email;

    /**
     * The content of the message.
     * Cannot be blank and has a maximum length.
     * Stored as TEXT for potentially longer messages.
     */
    @NotBlank(message = "Message cannot be blank")
    @Size(min = 10, max = 5000, message = "Message must be between 10 and 5000 characters")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * Timestamp of when the message was created/received.
     * Automatically set by Hibernate.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
