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
    /**
     * Applies updates from a PortfolioProfileDto (from a backup) to an existing PortfolioProfile entity.
     */
    public void applyUpdateFromDto(PortfolioProfileDto dto, PortfolioProfile profile) {
        if (dto == null || profile == null) return;

        // Note: We don't update the user's first/last name from the profile backup,
        // as that's part of the User entity itself.

        profile.setHeadline(dto.getHeadline());
        profile.setSummary(dto.getSummary());
        profile.setLocation(dto.getLocation());
        profile.setPublicEmail(dto.getPublicEmail());
        profile.setWebsiteUrl(dto.getWebsiteUrl());
        profile.setLinkedinUrl(dto.getLinkedinUrl());
        profile.setGithubUrl(dto.getGithubUrl());
        profile.setResumeUrl(dto.getResumeUrl());
        profile.setResumeImageUrl(dto.getResumeImageUrl());
        profile.setCoverLetterTemplate(dto.getCoverLetterTemplate());
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

    /**
     * Applies updates from an UpdatePortfolioProfileRequest to an existing PortfolioProfile entity.
     *
     * @param request The DTO with the fields to update.
     * @param profile The existing entity to be updated.
     */
    public void applyUpdateFromRequest(UpdatePortfolioProfileRequest request, PortfolioProfile profile) {
        if (request == null || profile == null) {
            return;
        }

        request.getHeadline().ifPresent(profile::setHeadline);
        request.getSummary().ifPresent(profile::setSummary);
        request.getPublicEmail().ifPresent(profile::setPublicEmail);
        request.getWebsiteUrl().ifPresent(profile::setWebsiteUrl);
        request.getLinkedinUrl().ifPresent(profile::setLinkedinUrl);
        request.getGithubUrl().ifPresent(profile::setGithubUrl);
        request.getResumeUrl().ifPresent(profile::setResumeUrl);
        request.getResumeImageUrl().ifPresent(profile::setResumeImageUrl);
        request.getLocation().ifPresent(profile::setLocation);
        request.getCoverLetterTemplate().ifPresent(profile::setCoverLetterTemplate);
    }
}