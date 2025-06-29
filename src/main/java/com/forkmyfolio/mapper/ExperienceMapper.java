package com.forkmyfolio.mapper;

import com.forkmyfolio.dto.CreateExperienceRequest;
import com.forkmyfolio.dto.ExperienceDto;
import com.forkmyfolio.dto.UpdateExperienceRequest;
import com.forkmyfolio.model.Experience;
import com.forkmyfolio.model.User;
import org.springframework.stereotype.Component;

@Component
public class ExperienceMapper {

    public ExperienceDto toDto(Experience experience) {
        if (experience == null) return null;
        ExperienceDto dto = new ExperienceDto();
        dto.setUuid(experience.getUuid());
        dto.setJobTitle(experience.getJobTitle());
        dto.setCompanyName(experience.getCompanyName());
        dto.setLocation(experience.getLocation());
        dto.setStartDate(experience.getStartDate());
        dto.setEndDate(experience.getEndDate());
        dto.setDescription(experience.getDescription());
        return dto;
    }

    public Experience toEntity(CreateExperienceRequest request, User owner) {
        if (request == null) return null;
        Experience experience = new Experience();
        experience.setJobTitle(request.getJobTitle());
        experience.setCompanyName(request.getCompanyName());
        experience.setLocation(request.getLocation());
        experience.setStartDate(request.getStartDate());
        experience.setEndDate(request.getEndDate());
        experience.setDescription(request.getDescription());
        experience.setUser(owner);
        return experience;
    }

    public void applyUpdateFromRequest(UpdateExperienceRequest request, Experience experience) {
        if (request == null || experience == null) return;
        request.getJobTitle().ifPresent(experience::setJobTitle);
        request.getCompanyName().ifPresent(experience::setCompanyName);
        request.getLocation().ifPresent(experience::setLocation);
        request.getStartDate().ifPresent(experience::setStartDate);
        request.getEndDate().ifPresent(experience::setEndDate);
        request.getDescription().ifPresent(experience::setDescription);
    }
}