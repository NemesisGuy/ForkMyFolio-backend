package com.forkmyfolio.mapper;

import com.forkmyfolio.dto.response.PortfolioProfileDto;
import com.forkmyfolio.dto.update.UpdatePortfolioProfileRequest;
import com.forkmyfolio.model.PortfolioProfile;
import com.forkmyfolio.model.User;
import org.springframework.stereotype.Component;

@Component
public class PortfolioProfileMapper {

    public PortfolioProfileDto toDto(PortfolioProfile profile) {
        if (profile == null) {
            return null;
        }
        PortfolioProfileDto dto = new PortfolioProfileDto();
        User user = profile.getUser();

        // FIX: Populate DTO with fields from both the User and PortfolioProfile entities.
        // This ensures the response contains all necessary information.
        if (user != null) {
            dto.setSlug(user.getSlug());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setProfileImageUrl(user.getProfileImageUrl());
        }

        dto.setHeadline(profile.getHeadline());
        dto.setSummary(profile.getSummary());
        dto.setPublicEmail(profile.getPublicEmail());
        dto.setWebsiteUrl(profile.getWebsiteUrl());
        dto.setLinkedinUrl(profile.getLinkedinUrl());
        dto.setGithubUrl(profile.getGithubUrl());
        dto.setResumeUrl(profile.getResumeUrl());
        dto.setResumeImageUrl(profile.getResumeImageUrl());
        dto.setLocation(profile.getLocation());
        dto.setCoverLetterTemplate(profile.getCoverLetterTemplate());
        dto.setVisible(profile.isVisible());
        dto.setPublic(profile.isPublic());
        return dto;
    }

    public PortfolioProfile toEntity(UpdatePortfolioProfileRequest request) {
        if (request == null) {
            return null;
        }
        PortfolioProfile profile = new PortfolioProfile();
        profile.setHeadline(request.getHeadline());
        profile.setSummary(request.getSummary());
        profile.setPublicEmail(request.getPublicEmail());
        profile.setWebsiteUrl(request.getWebsiteUrl());
        profile.setLinkedinUrl(request.getLinkedinUrl());
        profile.setGithubUrl(request.getGithubUrl());
        profile.setResumeUrl(request.getResumeUrl());
        profile.setResumeImageUrl(request.getResumeImageUrl());
        profile.setLocation(request.getLocation());
        profile.setCoverLetterTemplate(request.getCoverLetterTemplate());
        profile.setVisible(request.isVisible());
        profile.setPublic(request.isPublic());
        return profile;
    }

    /**
     * Applies updates from a DTO to an existing entity. Used during backup restores.
     * @param dto The source DTO.
     * @param entity The target entity to update.
     */
    public void applyUpdateFromDto(PortfolioProfileDto dto, PortfolioProfile entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setHeadline(dto.getHeadline());
        entity.setSummary(dto.getSummary());
        entity.setPublicEmail(dto.getPublicEmail());
        entity.setWebsiteUrl(dto.getWebsiteUrl());
        entity.setLinkedinUrl(dto.getLinkedinUrl());
        entity.setGithubUrl(dto.getGithubUrl());
        entity.setResumeUrl(dto.getResumeUrl());
        entity.setResumeImageUrl(dto.getResumeImageUrl());
        entity.setLocation(dto.getLocation());
        entity.setCoverLetterTemplate(dto.getCoverLetterTemplate());
        entity.setVisible(dto.isVisible());
        entity.setPublic(dto.isPublic());
    }
}