package com.forkmyfolio.mapper;

import com.forkmyfolio.dto.create.CreatePortfolioProfileRequest;
import com.forkmyfolio.dto.response.PortfolioProfileDto;
import com.forkmyfolio.dto.update.UpdatePortfolioProfileRequest;
import com.forkmyfolio.model.PortfolioProfile;
import com.forkmyfolio.model.User;
import org.springframework.stereotype.Component;

@Component
public class PortfolioProfileMapper {

    /**
     * Converts a PortfolioProfile entity into a PortfolioProfileDto for public consumption.
     */
    public PortfolioProfileDto toDto(PortfolioProfile portfolioProfile) {
        if (portfolioProfile == null) {
            throw new IllegalArgumentException("Cannot map a null portfolioProfile.");
        }
        User user = portfolioProfile.getUser();
        if (user == null) {
            throw new IllegalArgumentException("Cannot map a portfolioProfile with a null user.");
        }

        PortfolioProfileDto dto = new PortfolioProfileDto();

        // --- Data from User Entity ---
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        // REMOVED: dto.setProfileImageUrl(user.getProfileImageUrl()); // THIS LINE IS GONE.

        // --- Data from PortfolioProfile Entity ---
        dto.setHeadline(portfolioProfile.getHeadline());
        dto.setSummary(portfolioProfile.getSummary());
        dto.setLocation(portfolioProfile.getLocation());
        dto.setPublicEmail(portfolioProfile.getPublicEmail());
        dto.setWebsiteUrl(portfolioProfile.getWebsiteUrl());
        dto.setLinkedinUrl(portfolioProfile.getLinkedinUrl());
        dto.setGithubUrl(portfolioProfile.getGithubUrl());
        dto.setResumeUrl(portfolioProfile.getResumeUrl());
        dto.setResumeImageUrl(portfolioProfile.getResumeImageUrl());
        dto.setCoverLetterTemplate(portfolioProfile.getCoverLetterTemplate());

        return dto;
    }

    /**
     * Applies updates from an UpdatePortfolioProfileRequest DTO to an existing PortfolioProfile entity.
     */
    public void applyUpdateFromRequest(UpdatePortfolioProfileRequest request, PortfolioProfile profile) {
        if (request == null || profile == null) return;

        if (request.getHeadline() != null) profile.setHeadline(request.getHeadline());
        if (request.getSummary() != null) profile.setSummary(request.getSummary());
        if (request.getPublicEmail() != null) profile.setPublicEmail(request.getPublicEmail());
        if (request.getLocation() != null) profile.setLocation(request.getLocation());
        if (request.getWebsiteUrl() != null) profile.setWebsiteUrl(request.getWebsiteUrl());
        if (request.getLinkedinUrl() != null) profile.setLinkedinUrl(request.getLinkedinUrl());
        if (request.getGithubUrl() != null) profile.setGithubUrl(request.getGithubUrl());
        if (request.getResumeUrl() != null) profile.setResumeUrl(request.getResumeUrl());
        if (request.getResumeImageUrl() != null) profile.setResumeImageUrl(request.getResumeImageUrl());
        if (request.getCoverLetterTemplate() != null) profile.setCoverLetterTemplate(request.getCoverLetterTemplate());
    }

    /**
     * Converts a CreatePortfolioProfileRequest DTO to a new PortfolioProfile entity.
     */
    public PortfolioProfile toEntity(CreatePortfolioProfileRequest request, User owner) {
        if (request == null || owner == null) return null;

        PortfolioProfile profile = new PortfolioProfile();
        profile.setUser(owner);
        profile.setHeadline(request.getHeadline());
        profile.setSummary(request.getSummary());
        profile.setPublicEmail(request.getPublicEmail());
        profile.setLocation(request.getLocation());
        profile.setWebsiteUrl(request.getWebsiteUrl());
        profile.setLinkedinUrl(request.getLinkedinUrl());
        profile.setGithubUrl(request.getGithubUrl());
        profile.setResumeUrl(request.getResumeUrl());
        profile.setResumeImageUrl(request.getResumeImageUrl());
        profile.setCoverLetterTemplate(request.getCoverLetterTemplate());
        return profile;
    }
}