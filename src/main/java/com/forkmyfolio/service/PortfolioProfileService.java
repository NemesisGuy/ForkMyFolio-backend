package com.forkmyfolio.service;

import com.forkmyfolio.model.PortfolioProfile;
import com.forkmyfolio.model.User;

public interface PortfolioProfileService {

    /**
     * Retrieves the public portfolio profile of the owner.
     * @return The {@link PortfolioProfile} entity.
     */
    PortfolioProfile getPublicProfile();

    /**
     * Retrieves the portfolio profile associated with a specific user.
     * @param user The user whose profile is to be retrieved.
     * @return The existing {@link PortfolioProfile} entity.
     * @throws com.forkmyfolio.exception.ResourceNotFoundException if no profile exists for the user.
     */
    PortfolioProfile getProfileByUser(User user);

    /**
     * Creates and persists a new portfolio profile.
     * Throws an exception if a profile already exists for the user.
     * @param portfolioProfile The pre-constructed profile entity to save.
     * @return The persisted {@link PortfolioProfile} entity.
     */
    PortfolioProfile createProfile(PortfolioProfile portfolioProfile);

    /**
     * Saves changes to an existing portfolio profile.
     * @param portfolioProfile The profile entity with updated fields to be saved.
     * @return The persisted {@link PortfolioProfile} entity.
     */
    PortfolioProfile save(PortfolioProfile portfolioProfile);
}