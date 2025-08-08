package com.forkmyfolio.repository;

import com.forkmyfolio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findBySlug(String slug);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.portfolioProfile p WHERE u.slug = :slug AND u.active = true")
    Optional<User> findBySlugAndActiveTrue(@Param("slug") String slug);

    @Query("SELECT u FROM User u JOIN u.portfolioProfile p WHERE u.slug = :slug AND p.isPublic = :isPublic AND u.active = true")
    Optional<User> findBySlugAndPortfolioProfileIsPublic(@Param("slug") String slug, @Param("isPublic") boolean isPublic);

    boolean existsBySlug(String candidate);

    // FIX: This query now eagerly fetches all related portfolio collections for all users,
    // preventing LazyInitializationException and N+1 query problems in the backup process.
    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.portfolioProfile " +
            "LEFT JOIN FETCH u.projects p LEFT JOIN FETCH p.skills " +
            "LEFT JOIN FETCH u.userSkills us LEFT JOIN FETCH us.skill " +
            "LEFT JOIN FETCH u.experiences e LEFT JOIN FETCH e.skills " +
            "LEFT JOIN FETCH u.qualifications " +
            "LEFT JOIN FETCH u.testimonials")
    List<User> findAllWithPortfolioData();

    // FIX: This query now eagerly fetches all related portfolio collections for a specific user by slug.
    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.portfolioProfile " +
            "LEFT JOIN FETCH u.projects p LEFT JOIN FETCH p.skills " +
            "LEFT JOIN FETCH u.userSkills us LEFT JOIN FETCH us.skill " +
            "LEFT JOIN FETCH u.experiences e LEFT JOIN FETCH e.skills " +
            "LEFT JOIN FETCH u.qualifications " +
            "LEFT JOIN FETCH u.testimonials " +
            "WHERE u.slug = :slug")
    Optional<User> findBySlugWithAllPortfolioData(@Param("slug") String slug);

    Optional<User> findByUuid(UUID uuid);

    boolean existsByEmail(String email);

    // FIX: This query now eagerly fetches all related portfolio collections for a specific user by email.
    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.portfolioProfile " +
            "LEFT JOIN FETCH u.projects p LEFT JOIN FETCH p.skills " +
            "LEFT JOIN FETCH u.userSkills us LEFT JOIN FETCH us.skill " +
            "LEFT JOIN FETCH u.experiences e LEFT JOIN FETCH e.skills " +
            "LEFT JOIN FETCH u.qualifications " +
            "LEFT JOIN FETCH u.testimonials " +
            "WHERE u.email = :email")
    Optional<User> findByEmailWithAllPortfolioData(@Param("email") String email);
}