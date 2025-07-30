package com.forkmyfolio.repository;

import com.forkmyfolio.model.ContactMessage;
import com.forkmyfolio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for {@link ContactMessage} entities.
 */
@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    Optional<ContactMessage> findByUuid(UUID uuid);

    List<ContactMessage> findAllByOrderByCreatedAtDesc();

    boolean existsByUuid(UUID uuid);

    void deleteByUuid(UUID uuid);

    List<ContactMessage> findByUserOrderByCreatedAtDesc(User user);
}