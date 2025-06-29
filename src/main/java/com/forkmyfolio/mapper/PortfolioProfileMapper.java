package com.forkmyfolio.mapper;

import com.forkmyfolio.dto.PortfolioProfileDto;
import com.forkmyfolio.dto.UpdatePortfolioProfileRequest;
import com.forkmyfolio.model.PortfolioProfile;
import com.forkmyfolio.model.User;
import org.springframework.stereotype.Component;

/**
 * Mapper class responsible for converting between the PortfolioProfile domain model and its DTO representations.
 */
@Component
public class PortfolioProfileMapper {

    /**
     * Converts a PortfolioProfile entity into a PortfolioProfileDto for public consumption.
     * This method aggregates information from both the PortfolioProfile and its associated User entity.
     *
     * @param portfolioProfile The PortfolioProfile entity to convert.
     * @return A PortfolioProfileDto containing the combined public information.
     * @throws IllegalArgumentException if the portfolioProfile or its associated user is null.
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

        // Map fields from the associated User entity
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());

        // Map fields from the PortfolioProfile entity itself
        dto.setHeadline(portfolioProfile.getHeadline());
        dto.setSummary(portfolioProfile.getSummary());
        dto.setLocation(portfolioProfile.getLocation());
        dto.setPublicEmail(portfolioProfile.getPublicEmail());
        dto.setWebsiteUrl(portfolioProfile.getWebsiteUrl());
        dto.setLinkedinUrl(portfolioProfile.getLinkedinUrl());
        dto.setGithubUrl(portfolioProfile.getGithubUrl());
        dto.setResumeUrl(portfolioProfile.getResumeUrl());
        dto.setCoverLetterTemplate(portfolioProfile.getCoverLetterTemplate());

        return dto;
    }

    /**
     * Applies updates from an UpdatePortfolioProfileRequest DTO to an existing PortfolioProfile entity.
     * This method modifies the passed-in PortfolioProfile object directly. It does not save the entity.
     *
     * @param request The DTO containing the update information.
     * @param profile The PortfolioProfile entity to be updated.
     */
    public void applyUpdateFromRequest(UpdatePortfolioProfileRequest request, PortfolioProfile profile) {
        if (request == null || profile == null) {
            return;
        }

        // Note: The Update DTO uses regular fields, not Optional, so we check for null.
        if (request.getHeadline() != null) {
            profile.setHeadline(request.getHeadline());
        }
        if (request.getSummary() != null) {
            profile.setSummary(request.getSummary());
        }
        if (request.getPublicEmail() != null) {
            profile.setPublicEmail(request.getPublicEmail());
        }
        if (request.getLocation() != null) {
            profile.setLocation(request.getLocation());
        }
        if (request.getWebsiteUrl() != null) {
            profile.setWebsiteUrl(request.getWebsiteUrl());
        }
        if (request.getLinkedinUrl() != null) {
            profile.setLinkedinUrl(request.getLinkedinUrl());
        }
        if (request.getGithubUrl() != null) {
            profile.setGithubUrl(request.getGithubUrl());
        }
        if (request.getResumeUrl() != null) {
            profile.setResumeUrl(request.getResumeUrl());
        }
        if (request.getCoverLetterTemplate() != null) {
            profile.setCoverLetterTemplate(request.getCoverLetterTemplate());
        }
    }
}