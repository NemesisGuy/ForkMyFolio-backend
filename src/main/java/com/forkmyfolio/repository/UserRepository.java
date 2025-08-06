package com.forkmyfolio.repository;

import com.forkmyfolio.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing User entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUuid(UUID uuid);

    Boolean existsByEmail(String email);

    boolean existsBySlug(String slug);

    Optional<User> findBySlugAndActiveTrue(String slug);

    /**
     * Finds a user by email and eagerly fetches all associated portfolio data
     * using the 'User.withAllPortfolioData' entity graph. This is the designated method
     * for use cases like generating a full backup, preventing LazyInitializationException.
     *
     * @param email The email of the user to find.
     * @return An Optional containing the fully initialized User entity.
     */
    @EntityGraph(value = "User.withAllPortfolioData")
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailWithAllPortfolioData(String email);

    /**
     * Finds all users and eagerly fetches all associated portfolio data for each one
     * using the 'User.withAllPortfolioData' entity graph. This is used for the
     * system-wide admin backup.
     *
     * @return A list of fully initialized User entities.
     */
    @Query("SELECT u FROM User u")
    @EntityGraph(value = "User.withAllPortfolioData")
    List<User> findAllWithPortfolioData();

    /**
     * FIX: Finds a user by slug and eagerly fetches all associated portfolio data.
     * This is crucial for building the public portfolio DTO without lazy loading issues.
     *
     * @param slug The slug of the user to find.
     * @return An Optional containing the fully initialized User entity.
     */
    @EntityGraph(value = "User.withAllPortfolioData")
    @Query("SELECT u FROM User u WHERE u.slug = :slug AND u.active = true")
    Optional<User> findBySlugWithAllPortfolioData(String slug);
}