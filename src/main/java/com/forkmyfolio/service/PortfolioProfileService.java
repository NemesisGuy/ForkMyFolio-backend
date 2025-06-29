package com.forkmyfolio.service;

import com.forkmyfolio.model.PortfolioProfile;
import com.forkmyfolio.model.User;

/**
 * Service interface for business logic related to the PortfolioProfile.
 * This service operates solely on domain models and is DTO-agnostic.
 */
public interface PortfolioProfileService {

    /**
     * Retrieves the public portfolio profile of the owner.
     * @return The {@link PortfolioProfile} entity.
     */
    PortfolioProfile getPublicProfile();

    /**
     * Retrieves the portfolio profile associated with a specific user.
     * Creates a new, empty profile if one doesn't exist.
     * @param user The user whose profile is to be retrieved.
     * @return The existing or newly created {@link PortfolioProfile} entity.
     */
    PortfolioProfile getProfileForUser(User user);

    /**
     * Saves a PortfolioProfile entity. Used for both create and update operations.
     * @param portfolioProfile The profile entity to be saved.
     * @return The persisted {@link PortfolioProfile} entity.
     */
    PortfolioProfile save(PortfolioProfile portfolioProfile);
}