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

    @Query("SELECT u FROM User u")
    List<User> findAllWithPortfolioData();

    @Query("SELECT u FROM User u WHERE u.slug = :slug")
    Optional<User> findBySlugWithAllPortfolioData(@Param("slug") String slug);

    Optional<User> findByUuid(UUID uuid);

    boolean existsByEmail(String email);

    /**
     * Finds a user by their email address. The service layer is responsible for initializing any needed portfolio data.
     * @param email The user's email.
     * @return An Optional containing the User, if found.
     */
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailWithAllPortfolioData(@Param("email") String email);
}