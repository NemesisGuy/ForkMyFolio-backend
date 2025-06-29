package com.forkmyfolio.service.impl;

import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.model.Experience;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.ExperienceRepository;
import com.forkmyfolio.repository.UserRepository;
import com.forkmyfolio.service.ExperienceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ExperienceServiceImpl implements ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final UserRepository userRepository;

    @Autowired
    public ExperienceServiceImpl(ExperienceRepository experienceRepository, UserRepository userRepository) {
        this.experienceRepository = experienceRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Experience> getPublicExperience() {
        User owner = userRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("Portfolio owner user not found."));
        return experienceRepository.findByUserOrderByStartDateDesc(owner);
    }

    @Override
    @Transactional(readOnly = true)
    public Experience getExperienceByUuid(UUID uuid) {
        return experienceRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Experience not found with UUID: " + uuid));
    }

    @Override
    @Transactional
    public Experience createExperience(Experience experience) {
        return experienceRepository.save(experience);
    }

    @Override
    @Transactional
    public Experience save(Experience experience) {
        return experienceRepository.save(experience);
    }

    @Override
    @Transactional
    public void deleteExperience(UUID uuid, User currentUser) {
        Experience experienceToDelete = getExperienceByUuid(uuid);
        if (!experienceToDelete.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("User does not have permission to delete this experience.");
        }
        experienceRepository.delete(experienceToDelete);
    }
}