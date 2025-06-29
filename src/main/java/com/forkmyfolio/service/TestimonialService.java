package com.forkmyfolio.service;

import com.forkmyfolio.model.Testimonial;
import com.forkmyfolio.model.User;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for business logic related to Testimonials.
 * This service operates solely on domain models and is DTO-agnostic.
 */
public interface TestimonialService {

    /**
     * Retrieves the list of public testimonials for the portfolio owner.
     * @return A list of {@link Testimonial} entities.
     */
    List<Testimonial> getPublicTestimonials();

    /**
     * Retrieves a single testimonial by its public UUID.
     * @param uuid The UUID of the testimonial.
     * @return The {@link Testimonial} entity.
     */
    Testimonial getTestimonialByUuid(UUID uuid);

    /**
     * Creates and persists a new testimonial.
     * @param testimonial The pre-constructed testimonial entity to save.
     * @return The persisted {@link Testimonial} entity.
     */
    Testimonial createTestimonial(Testimonial testimonial);

    /**
     * Saves an updated testimonial entity.
     * @param testimonial The testimonial entity with updated fields to be saved.
     * @return The updated and persisted {@link Testimonial} entity.
     */
    Testimonial save(Testimonial testimonial);

    /**
     * Deletes a testimonial by its public UUID.
     * @param uuid The UUID of the testimonial to delete.
     * @param currentUser The user performing the action.
     * @throws AccessDeniedException if the user is not authorized.
     */
    void deleteTestimonial(UUID uuid, User currentUser);
}