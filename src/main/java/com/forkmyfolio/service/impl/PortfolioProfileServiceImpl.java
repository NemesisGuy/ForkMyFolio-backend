package com.forkmyfolio.service.impl;

import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.model.PortfolioProfile;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.PortfolioProfileRepository;
import com.forkmyfolio.repository.UserRepository;
import com.forkmyfolio.service.PortfolioProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PortfolioProfileServiceImpl implements PortfolioProfileService {

    private final PortfolioProfileRepository portfolioProfileRepository;
    private final UserRepository userRepository;

    @Autowired
    public PortfolioProfileServiceImpl(PortfolioProfileRepository portfolioProfileRepository, UserRepository userRepository) {
        this.portfolioProfileRepository = portfolioProfileRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioProfile getPublicProfile() {
        // This method might need to be re-evaluated based on how the "owner" is defined.
        // For now, assuming it fetches a specific profile, e.g., for the main admin or a default user.
        // This implementation is a placeholder.
        return portfolioProfileRepository.findById(1L).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioProfile getProfileByUser(User user) {
        return portfolioProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("PortfolioProfile not found for user with id: " + user.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioProfile getProfileBySlug(String slug) {
        // FIX: Changed to use the correct repository method that finds active users by slug.
        User user = userRepository.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with slug: " + slug));
        if (user.getPortfolioProfile() == null) {
            throw new ResourceNotFoundException("PortfolioProfile not found for user with slug: " + slug);
        }
        return user.getPortfolioProfile();
    }

    @Override
    public PortfolioProfile createProfile(PortfolioProfile portfolioProfile) {
        if (portfolioProfileRepository.findByUser(portfolioProfile.getUser()).isPresent()) {
            throw new IllegalStateException("A portfolio profile already exists for this user.");
        }
        return portfolioProfileRepository.save(portfolioProfile);
    }

    @Override
    public PortfolioProfile save(PortfolioProfile portfolioProfile) {
        return portfolioProfileRepository.save(portfolioProfile);
    }

    @Override
    public PortfolioProfile updateProfileVisibility(User user, boolean isPublic) {
        PortfolioProfile profile = getProfileByUser(user);
        profile.setPublic(isPublic);
        return portfolioProfileRepository.save(profile);
    }
}