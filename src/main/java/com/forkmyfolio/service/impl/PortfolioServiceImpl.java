package com.forkmyfolio.service.impl;

import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.model.User;
import com.forkmyfolio.service.PortfolioProfileService;
import com.forkmyfolio.service.PortfolioService;
import com.forkmyfolio.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioServiceImpl implements PortfolioService {

    private final UserService userService;
    private final PortfolioProfileService portfolioProfileService;

    @Override
    @Transactional(readOnly = true)
    public User getPublicPortfolioUserBySlug(String slug) {
        // The error "could not initialize proxy - no Session" occurs when lazy-loaded
        // collections are accessed after the database transaction has closed.
        // The fix is to explicitly initialize all necessary collections *within* this
        // @Transactional method before returning the User object.

        // 1. Fetch the user.
        User user = userService.findBySlug(slug).orElseThrow(() -> new ResourceNotFoundException("User with slug '" + slug + "' not found."));

        // 2. Get the profile and perform permission checks.
        // Using the service guarantees a non-null profile is returned (it's created if missing).
        // We only need to check if it's public.
        if (!portfolioProfileService.getProfileByUser(user).isPublic()) {
            log.warn("Access denied for portfolio with slug '{}'. Profile is set to private.", slug);
            throw new ResourceNotFoundException("Public portfolio for user '" + slug + "' is not available or is private.");
        }

        // 3. **THE FIX**: Force initialization of all lazy collections needed for the API response.
        // By "touching" them here using Hibernate.initialize(), we force them to be loaded from the database
        // while the session is still open.
        Hibernate.initialize(user.getPortfolioProfile());
        Hibernate.initialize(user.getQualifications());
        Hibernate.initialize(user.getTestimonials());

        // Also initialize nested collections to prevent further lazy-loading issues.
        user.getProjects().forEach(project -> Hibernate.initialize(project.getSkills()));
        user.getExperiences().forEach(experience -> Hibernate.initialize(experience.getSkills()));
        user.getUserSkills().forEach(userSkill -> Hibernate.initialize(userSkill.getSkill()));


        log.info("Access GRANTED for slug '{}'. Returning fully initialized user entity.", slug);

        return user;
    }
}