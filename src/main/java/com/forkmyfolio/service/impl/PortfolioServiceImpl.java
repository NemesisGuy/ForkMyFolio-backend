package com.forkmyfolio.service.impl;

import com.forkmyfolio.exception.PermissionDeniedException;
import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.model.PortfolioProfile;
import com.forkmyfolio.model.User;
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

    @Override
    @Transactional(readOnly = true)
    public User getPublicPortfolioUserBySlug(String slug) {
        // The error "could not initialize proxy - no Session" occurs when lazy-loaded
        // collections are accessed after the database transaction has closed.
        // The fix is to explicitly initialize all necessary collections *within* this
        // @Transactional method before returning the User object.

        // 1. Fetch the user.
        User user = userService.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio with slug: " + slug + " not found."));

        // 2. Perform permission checks first to fail fast.
        PortfolioProfile profile = user.getPortfolioProfile();
        if (profile == null) {
            throw new ResourceNotFoundException("Profile for user with slug: " + slug + " not found.");
        }

        log.info("Checking privacy for slug '{}'. isPublic flag is: {}", slug, profile.isPublic());
        if (!profile.isPublic()) {
            log.warn("Access DENIED for slug '{}'. Throwing PermissionDeniedException.", slug);
            throw new PermissionDeniedException("This portfolio is private and cannot be viewed.");
        }

        // 3. **THE FIX**: Force initialization of all lazy collections needed for the API response.
        // By "touching" them here using Hibernate.initialize(), we force them to be loaded from the database
        // while the session is still open.
        Hibernate.initialize(user.getPortfolioProfile());
        Hibernate.initialize(user.getProjects());
        Hibernate.initialize(user.getUserSkills());
        Hibernate.initialize(user.getExperiences());
        Hibernate.initialize(user.getQualifications());
        Hibernate.initialize(user.getTestimonials());

        // Also initialize nested collections to prevent further lazy-loading issues.
        user.getProjects().forEach(project -> Hibernate.initialize(project.getSkills()));
        user.getExperiences().forEach(experience -> Hibernate.initialize(experience.getSkills()));
        // FIX: The previous error was because the Skill object inside UserSkill was not initialized.
        // This line resolves the "Could not initialize proxy [com.forkmyfolio.model.Skill...]" error.
        user.getUserSkills().forEach(userSkill -> Hibernate.initialize(userSkill.getSkill()));


        log.info("Access GRANTED for slug '{}'. Returning fully initialized user entity.", slug);
        return user;
    }
}