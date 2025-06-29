package com.forkmyfolio.service.impl;

import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.model.PortfolioProfile;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.PortfolioProfileRepository; // Renamed repository
import com.forkmyfolio.repository.UserRepository;
import com.forkmyfolio.service.PortfolioProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortfolioProfileServiceImpl implements PortfolioProfileService {

    private final UserRepository userRepository;
    private final PortfolioProfileRepository portfolioProfileRepository; // Renamed repository

    @Autowired
    public PortfolioProfileServiceImpl(UserRepository userRepository, PortfolioProfileRepository portfolioProfileRepository) {
        this.userRepository = userRepository;
        this.portfolioProfileRepository = portfolioProfileRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioProfile getPublicProfile() {
        User owner = userRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("Portfolio owner user not found."));

        return portfolioProfileRepository.findByUser(owner)
                .orElseThrow(() -> new ResourceNotFoundException("PortfolioProfile not found for owner with ID: " + owner.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioProfile getProfileForUser(User user) {
        // Find the portfolioProfile for the given user, or create a new one if it doesn't exist.
        return portfolioProfileRepository.findByUser(user)
                .orElseGet(() -> {
                    PortfolioProfile newPortfolioProfile = new PortfolioProfile();
                    newPortfolioProfile.setUser(user);
                    return newPortfolioProfile;
                });
    }

    @Override
    @Transactional
    public PortfolioProfile save(PortfolioProfile portfolioProfile) {
        // The service's job is simply to persist the entity passed to it.
        return portfolioProfileRepository.save(portfolioProfile);
    }
}