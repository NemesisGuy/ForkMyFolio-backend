package com.forkmyfolio.repository;

import com.forkmyfolio.model.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link ContactMessage} entities.
 * Provides standard CRUD operations for contact messages.
 * As contact messages are typically write-once and read by admins,
 * complex query methods are less common but can be added if needed.
 */
@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
    // Basic CRUD operations are inherited from JpaRepository.
    // Custom query methods can be added here if specific finders are needed,
    // for example, finding messages by email or within a certain date range.
}
