package com.forkmyfolio.service.impl;

import com.forkmyfolio.model.PortfolioProfile;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.PortfolioProfileRepository;
import com.forkmyfolio.service.PortfolioProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioProfileServiceImpl implements PortfolioProfileService {

    private final PortfolioProfileRepository portfolioProfileRepository;

    @Override
    @Transactional
    public PortfolioProfile getProfileByUser(User user) {
        // FIX: Implement a "get-or-create" pattern. If a profile doesn't exist,
        // create and persist a new, default one. This ensures every user has a
        // profile record from their first interaction, preventing null pointer issues.
        return portfolioProfileRepository.findByUser(user)
                .orElseGet(() -> {
                    log.info("No PortfolioProfile found for user ID: {}. Creating and persisting a new default instance.", user.getId());
                    PortfolioProfile newProfile = new PortfolioProfile();
                    newProfile.setUser(user);
                    return portfolioProfileRepository.save(newProfile);
                });
    }

    @Override
    @Transactional
    public PortfolioProfile createOrUpdateProfile(PortfolioProfile profileUpdates, User user) {
        // This method now safely handles both creation of a new profile and updates to an existing one.
        PortfolioProfile existingProfile = getProfileByUser(user);

        existingProfile.setHeadline(profileUpdates.getHeadline());
        existingProfile.setSummary(profileUpdates.getSummary());
        existingProfile.setPublicEmail(profileUpdates.getPublicEmail());
        existingProfile.setWebsiteUrl(profileUpdates.getWebsiteUrl());
        existingProfile.setLinkedinUrl(profileUpdates.getLinkedinUrl());
        existingProfile.setGithubUrl(profileUpdates.getGithubUrl());
        existingProfile.setResumeUrl(profileUpdates.getResumeUrl());
        existingProfile.setResumeImageUrl(profileUpdates.getResumeImageUrl());
        existingProfile.setLocation(profileUpdates.getLocation());
        existingProfile.setCoverLetterTemplate(profileUpdates.getCoverLetterTemplate());
        existingProfile.setVisible(profileUpdates.isVisible());
        existingProfile.setPublic(profileUpdates.isPublic());

        return portfolioProfileRepository.save(existingProfile);
    }

    @Override
    @Transactional
    public PortfolioProfile updateProfileVisibility(User user, boolean isPublic) {
        PortfolioProfile profile = getProfileByUser(user);
        profile.setPublic(isPublic);
        log.info("Updating portfolio visibility for user {} to: {}", user.getSlug(), isPublic);
        return portfolioProfileRepository.save(profile);
    }
}