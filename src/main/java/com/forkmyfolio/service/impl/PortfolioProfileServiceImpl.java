package com.forkmyfolio.service.impl;

import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.model.PortfolioProfile;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.PortfolioProfileRepository;
import com.forkmyfolio.repository.UserRepository;
import com.forkmyfolio.service.PortfolioProfileService;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PortfolioProfileServiceImpl implements PortfolioProfileService {

    private final PortfolioProfileRepository portfolioProfileRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PortfolioProfile getPublicProfile() {
        // This is a legacy method from the single-user architecture.
        // It finds the first user and returns their profile.
        User owner = userRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio owner user not found."));
        return getProfileByUser(owner);
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioProfile getProfileByUser(User user) {
        return portfolioProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("PortfolioProfile not found for user: " + user.getEmail()));
    }

    @Override
    @Transactional
    public PortfolioProfile createProfile(PortfolioProfile portfolioProfile) {
        // Basic check to prevent creating duplicate profiles for a user.
        portfolioProfileRepository.findByUser(portfolioProfile.getUser()).ifPresent(p -> {
            throw new IllegalStateException("A portfolio profile already exists for this user.");
        });
        PortfolioProfile savedProfile = portfolioProfileRepository.save(portfolioProfile);
        // Explicitly initialize the lazy-loaded User proxy before the transaction ends.
        Hibernate.initialize(savedProfile.getUser());
        return savedProfile;
    }

    @Override
    @Transactional
    public PortfolioProfile save(PortfolioProfile portfolioProfile) {
        // This method is used for updates. Ownership checks happen in the controller layer.
        PortfolioProfile savedProfile = portfolioProfileRepository.save(portfolioProfile);
        // Explicitly initialize the lazy-loaded User proxy before the transaction ends.
        Hibernate.initialize(savedProfile.getUser());
        return savedProfile;
    }
}