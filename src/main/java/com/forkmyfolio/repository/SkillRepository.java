package com.forkmyfolio.repository;

import com.forkmyfolio.model.Skill;
import com.forkmyfolio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Skill} entities.
 * Provides standard CRUD operations for skills.
 * Additional query methods can be added here as needed.
 */
@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    /**
     * Finds all skills associated with a specific user ID.
     *
     * @param userId The ID of the user whose skills are to be retrieved.
     * @return A list of {@link Skill} entities belonging to the specified user.
     * Returns an empty list if the user has no skills or the user does not exist.
     */
    List<Skill> findByUserId(Long userId);

    List<Skill> findByUser(User owner);

    Optional<Skill> findByUuid(UUID uuid);

    void deleteByUser(User user);
}
