package com.forkmyfolio.service.impl;

import com.forkmyfolio.exception.ConflictException;
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
public class PortfolioProfileServiceImpl implements PortfolioProfileService {

    private final UserRepository userRepository;
    private final PortfolioProfileRepository portfolioProfileRepository;

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
        return getProfileByUser(owner);
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioProfile getProfileByUser(User user) {
        return portfolioProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("PortfolioProfile not found for user with ID: " + user.getId()));
    }

    @Override
    @Transactional
    public PortfolioProfile createProfile(PortfolioProfile portfolioProfile) {
        // Business rule: A user can only have one profile. Prevent duplicates.
        portfolioProfileRepository.findByUser(portfolioProfile.getUser()).ifPresent(p -> {
            throw new ConflictException("A portfolio profile already exists for this user. Use PUT to update.");
        });
        return portfolioProfileRepository.save(portfolioProfile);
    }

    @Override
    @Transactional
    public PortfolioProfile save(PortfolioProfile portfolioProfile) {
        // This method is for updating an existing entity.
        return portfolioProfileRepository.save(portfolioProfile);
    }
}