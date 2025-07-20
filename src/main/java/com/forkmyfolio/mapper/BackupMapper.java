package com.forkmyfolio.mapper;

import com.forkmyfolio.dto.response.PortfolioBackupDto;
import com.forkmyfolio.service.model.PortfolioBackupData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Maps the service-level PortfolioBackupData object to the API-level PortfolioBackupDto.
 */
@Component
@RequiredArgsConstructor
public class BackupMapper {

    private final PortfolioProfileMapper portfolioProfileMapper;
    private final ProjectMapper projectMapper;
    private final SkillMapper skillMapper;
    private final ExperienceMapper experienceMapper;
    private final TestimonialMapper testimonialMapper;
    private final QualificationMapper qualificationMapper;

    public PortfolioBackupDto toDto(PortfolioBackupData data) {
        if (data == null) {
            return null;
        }

        PortfolioBackupDto dto = new PortfolioBackupDto();

        if (data.getProfile() != null) {
            dto.setProfile(portfolioProfileMapper.toDto(data.getProfile()));
        }
        if (data.getProjects() != null) {
            dto.setProjects(data.getProjects().stream().map(projectMapper::toDto).collect(Collectors.toList()));
        }
        if (data.getSkills() != null) {
            dto.setSkills(data.getSkills().stream().map(skillMapper::toDto).collect(Collectors.toList()));
        }
        if (data.getExperiences() != null) {
            dto.setExperiences(data.getExperiences().stream().map(experienceMapper::toDto).collect(Collectors.toList()));
        }
        if (data.getTestimonials() != null) {
            dto.setTestimonials(data.getTestimonials().stream().map(testimonialMapper::toDto).collect(Collectors.toList()));
        }
        if (data.getQualifications() != null) {
            dto.setQualifications(data.getQualifications().stream().map(qualificationMapper::toDto).collect(Collectors.toList()));
        }

        return dto;
    }
}