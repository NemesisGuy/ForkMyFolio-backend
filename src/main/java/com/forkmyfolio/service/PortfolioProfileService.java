package com.forkmyfolio.service;

import com.forkmyfolio.model.PortfolioProfile;
import com.forkmyfolio.model.User;

/**
 * Service interface for managing a user's portfolio profile.
 */
public interface PortfolioProfileService {

    /**
     * Retrieves the portfolio profile for a given user. If a profile does not exist,
     * a new, transient default instance is created and returned.
     *
     * @param user The user whose profile to retrieve.
     * @return The existing or a new default PortfolioProfile.
     */
    PortfolioProfile getProfileByUser(User user);

    PortfolioProfile createOrUpdateProfile(PortfolioProfile profileUpdates, User user);

    /**
     * Updates the public visibility of a user's portfolio.
     *
     * @param user     The user whose profile to update.
     * @param isPublic The new visibility status.
     * @return The updated PortfolioProfile.
     */
    PortfolioProfile updateProfileVisibility(User user, boolean isPublic);
}