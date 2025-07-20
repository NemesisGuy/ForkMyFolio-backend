package com.forkmyfolio.repository;

import com.forkmyfolio.model.Project;
import com.forkmyfolio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Project} entities.
 * Provides standard CRUD operations for projects.
 * Additional query methods can be added here as needed.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Finds all projects associated with a specific user ID.
     *
     * @param userId The ID of the user whose projects are to be retrieved.
     * @return A list of {@link Project} entities belonging to the specified user.
     * Returns an empty list if the user has no projects or the user does not exist.
     */
    List<Project> findByUserId(Long userId);

    List<Project> findByUser(User owner);

    Optional<Project> findByUuid(UUID uuid);

    void deleteByUser(User user);
}
