package com.forkmyfolio.repository;

import com.forkmyfolio.model.Experience;
import com.forkmyfolio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    // Find all experiences for a specific user, ordered by start date descending
    List<Experience> findByUserOrderByStartDateDesc(User user);

    Optional<Experience> findByUuid(UUID uuid);

    List<Experience> findAllByOrderByStartDateDesc();

    void deleteByUser(User user);
}