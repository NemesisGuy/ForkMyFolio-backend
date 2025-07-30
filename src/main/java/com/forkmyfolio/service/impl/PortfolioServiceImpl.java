package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.response.*;
import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.mapper.*;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.*;
import com.forkmyfolio.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private final UserRepository userRepository;
    private final PortfolioProfileRepository portfolioProfileRepository;
    private final ProjectRepository projectRepository;
    private final SkillRepository skillRepository;
    private final ExperienceRepository experienceRepository;
    private final QualificationRepository qualificationRepository;
    private final TestimonialRepository testimonialRepository;

    // Mappers
    private final UserMapper userMapper;
    private final PortfolioProfileMapper portfolioProfileMapper;
    private final ProjectMapper projectMapper;
    private final SkillMapper skillMapper;
    private final ExperienceMapper experienceMapper;
    private final QualificationMapper qualificationMapper;
    private final TestimonialMapper testimonialMapper;

    @Override
    @Transactional(readOnly = true)
    public PortfolioDto getFullPublicPortfolioBySlug(String slug) {
        // Find the user by their slug. Only find active users.
        User user = userRepository.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found for slug: " + slug));

        PortfolioDto portfolioDto = new PortfolioDto();
        portfolioDto.setUser(userMapper.toPublicUserDto(user));

        // Fetch and map all visible components of the portfolio
        portfolioProfileRepository.findByUserAndVisibleTrue(user)
                .ifPresent(profile -> portfolioDto.setProfile(portfolioProfileMapper.toDto(profile)));

        portfolioDto.setProjects(projectRepository.findByUserAndVisibleTrue(user).stream()
                .map(projectMapper::toDto)
                .collect(Collectors.toList()));

        portfolioDto.setSkills(skillRepository.findByUserAndVisibleTrue(user).stream()
                .map(skillMapper::toDto)
                .collect(Collectors.toList()));

        portfolioDto.setExperiences(experienceRepository.findByUserAndVisibleTrue(user).stream()
                .map(experienceMapper::toDto)
                .collect(Collectors.toList()));

        portfolioDto.setQualifications(qualificationRepository.findByUserAndVisibleTrue(user).stream()
                .map(qualificationMapper::toDto)
                .collect(Collectors.toList()));

        portfolioDto.setTestimonials(testimonialRepository.findByUserAndVisibleTrue(user).stream()
                .map(testimonialMapper::toDto)
                .collect(Collectors.toList()));

        return portfolioDto;
    }
}