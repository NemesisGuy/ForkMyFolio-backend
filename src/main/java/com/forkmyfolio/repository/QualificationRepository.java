package com.forkmyfolio.repository;

import com.forkmyfolio.model.Qualification;
import com.forkmyfolio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QualificationRepository extends JpaRepository<Qualification, Long> {

    Optional<Qualification> findByUuid(UUID uuid);

    // Find all qualifications for a user, ordered by year descending
    List<Qualification> findByUserOrderByCompletionYearDesc(User user);

    void deleteByUser(User user);
}