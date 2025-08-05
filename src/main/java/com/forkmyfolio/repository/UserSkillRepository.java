package com.forkmyfolio.repository;

import com.forkmyfolio.model.Skill;
import com.forkmyfolio.model.User;
import com.forkmyfolio.model.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing UserSkill entities.
 */
@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

    /**
     * Finds all UserSkill relationships for a given user.
     *
     * @param user The user entity.
     * @return A list of UserSkill entities.
     */
    List<UserSkill> findByUser(User user);

    /**
     * Checks if a UserSkill relationship exists for a given user and skill.
     *
     * @param user  The user entity.
     * @param skill The skill entity.
     * @return true if the relationship exists, false otherwise.
     */
    boolean existsByUserAndSkill(User user, Skill skill);

    /**
     * Finds a UserSkill relationship by its public-facing UUID.
     *
     * @param uuid The UUID of the UserSkill.
     * @return An Optional containing the UserSkill if found.
     */
    Optional<UserSkill> findByUuid(UUID uuid);

    Optional<UserSkill> findByUserAndSkillUuid(User user, UUID skillUuid);

    Optional<UserSkill> findByUserAndSkill(User user, Skill globalSkill);

    /**
     * Finds all skills for a given user that are marked as visible.
     * This is the correct method to align with the 'visible' field in the UserSkill entity.
     *
     * @param user The user whose visible skills to find.
     * @return A list of visible UserSkill entities.
     */
    List<UserSkill> findByUserAndVisibleTrue(User user);
}