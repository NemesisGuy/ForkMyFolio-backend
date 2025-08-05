package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.response.*;
import com.forkmyfolio.exception.PermissionDeniedException;
import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.mapper.*;
import com.forkmyfolio.model.PortfolioProfile;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.*;
import com.forkmyfolio.service.PortfolioService;
import com.forkmyfolio.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioServiceImpl implements PortfolioService {

    private final UserService userService;
    private final PortfolioProfileRepository portfolioProfileRepository;
    private final ProjectRepository projectRepository;
    private final UserSkillRepository userSkillRepository;
    private final ExperienceRepository experienceRepository;
    private final QualificationRepository qualificationRepository;
    private final TestimonialRepository testimonialRepository;

    // Mappers
    private final PublicUserMapper publicUserMapper;
    private final PortfolioProfileMapper portfolioProfileMapper;
    private final ProjectMapper projectMapper;
    private final SkillMapper skillMapper;
    private final ExperienceMapper experienceMapper;
    private final QualificationMapper qualificationMapper;
    private final TestimonialMapper testimonialMapper;

    @Override
    @Transactional(readOnly = true)
    public PortfolioDto getFullPublicPortfolioBySlug(String slug) {
        User user = userService.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio with slug: " + slug));

        PortfolioProfile profile = portfolioProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profile for user with slug: " + slug));

        // --- DIAGNOSTIC LOG ---
        // This will tell us exactly what the backend sees.
        log.info("Checking privacy for slug '{}'. isPublic flag is: {}", slug, profile.isPublic());

        if (!profile.isPublic()) {
            log.warn("Access DENIED for slug '{}'. Throwing PermissionDeniedException.", slug);
            throw new PermissionDeniedException("This portfolio is private and cannot be viewed.");
        }

        log.info("Access GRANTED for slug '{}'. Proceeding to build portfolio DTO.", slug);

        // If the check passes, proceed with gathering all public data.
        PublicUserDto userDto = publicUserMapper.toDto(user);
        PortfolioProfileDto profileDto = portfolioProfileMapper.toDto(profile);
        // Ensure the DTO reflects the true state of the entity
        profileDto.setPublic(profile.isPublic());

        List<ProjectDto> projects = projectRepository.findByUserAndVisibleTrue(user)
                .stream()
                .map(projectMapper::toDto)
                .collect(Collectors.toList());

        // FIX: Use the 'toDetailDto' mapper which correctly includes user-specific
        // data like proficiency level from the UserSkill entity.
        List<SkillDto> skills = userSkillRepository.findByUserAndVisibleTrue(user)
                .stream()
                .map(skillMapper::toDetailDto)
                .collect(Collectors.toList());

        List<ExperienceDto> experiences = experienceRepository.findByUserAndVisibleTrue(user)
                .stream()
                .map(experienceMapper::toDto)
                .collect(Collectors.toList());

        List<QualificationDto> qualifications = qualificationRepository.findByUserAndVisibleTrue(user)
                .stream()
                .map(qualificationMapper::toDto)
                .collect(Collectors.toList());

        List<TestimonialDto> testimonials = testimonialRepository.findByUserAndVisibleTrue(user)
                .stream()
                .map(testimonialMapper::toDto)
                .collect(Collectors.toList());

        PortfolioDto portfolioDto = new PortfolioDto();
        portfolioDto.setUser(userDto);
        portfolioDto.setProfile(profileDto);
        portfolioDto.setProjects(projects);
        portfolioDto.setSkills(skills);
        portfolioDto.setExperiences(experiences);
        portfolioDto.setQualifications(qualifications);
        portfolioDto.setTestimonials(testimonials);

        return portfolioDto;
    }
}