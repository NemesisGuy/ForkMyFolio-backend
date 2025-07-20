package com.forkmyfolio.repository;

import com.forkmyfolio.model.Role;
import com.forkmyfolio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link User} entities.
 * Provides standard CRUD operations and custom query methods for users.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     * Email is a unique identifier for users.
     *
     * @param email The email address to search for.
     * @return An {@link Optional} containing the user if found, or empty if not.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists with the given email address.
     * This is more efficient than fetching the entire entity if only existence is needed.
     *
     * @param email The email address to check.
     * @return {@code true} if a user with the email exists, {@code false} otherwise.
     */
    Boolean existsByEmail(String email);

    Optional<User> findFirstByOrderByIdAsc();

    boolean existsByRolesContains(Role role);

}
