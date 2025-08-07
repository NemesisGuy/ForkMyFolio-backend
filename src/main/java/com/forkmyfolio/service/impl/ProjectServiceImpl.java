package com.forkmyfolio.service.impl;

import com.forkmyfolio.exception.PermissionDeniedException;
import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.model.Project;
import com.forkmyfolio.model.Skill;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.ProjectRepository;
import com.forkmyfolio.repository.SkillRepository;
import com.forkmyfolio.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final SkillRepository skillRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Project> getProjectsForUser(User user) {
        // The service layer's responsibility is to fetch the domain entities.
        List<Project> projects = projectRepository.findByUserOrderByDisplayOrderAsc(user);
        // We initialize the skills collection to prevent LazyInitializationException later in the mapping process.
        projects.forEach(project -> Hibernate.initialize(project.getSkills()));
        return projects;
    }

    @Override
    @Transactional(readOnly = true)
    public Project findProjectByUuidAndUser(UUID uuid, User user) {
        Project project = projectRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Project with UUID " + uuid + " not found."));
        if (!project.getUser().getId().equals(user.getId())) {
            throw new PermissionDeniedException("You do not have permission to access this project.");
        }
        Hibernate.initialize(project.getSkills()); // Initialize skills to prevent LazyInitializationException in the mapper.
        return project;
    }

    @Override
    @Transactional
    public Project createProject(Project project, Set<String> skillNames) {
        Set<Skill> skills = findAndValidateSkills(skillNames);
        project.setSkills(skills);
        Project savedProject = projectRepository.save(project);
        Hibernate.initialize(savedProject.getSkills()); // Initialize for the response DTO
        return savedProject;
    }

    @Override
    @Transactional
    public Project updateProject(UUID uuid, Project projectWithUpdates, Set<String> skillNames, User user) {
        Project existingProject = findProjectByUuidAndUser(uuid, user); // This also handles permission check

        existingProject.setTitle(projectWithUpdates.getTitle());
        existingProject.setDescription(projectWithUpdates.getDescription());
        existingProject.setRepoUrl(projectWithUpdates.getRepoUrl());
        existingProject.setLiveUrl(projectWithUpdates.getLiveUrl());
        existingProject.setImageUrl(projectWithUpdates.getImageUrl());
        existingProject.setVisible(projectWithUpdates.isVisible());
        existingProject.setDisplayOrder(projectWithUpdates.getDisplayOrder());

        Set<Skill> skills = findAndValidateSkills(skillNames);
        existingProject.setSkills(skills);

        Project updatedProject = projectRepository.save(existingProject);
        Hibernate.initialize(updatedProject.getSkills()); // Initialize for the response DTO
        return updatedProject;
    }

    @Override
    @Transactional
    public void deleteProject(UUID uuid, User user) {
        Project project = findProjectByUuidAndUser(uuid, user); // Ensures user owns the project before deleting
        projectRepository.delete(project);
    }

    private Set<Skill> findAndValidateSkills(Set<String> skillNames) {
        if (skillNames == null || skillNames.isEmpty()) {
            return new HashSet<>();
        }
        // This assumes your SkillRepository has a 'findByNameIn' method.
        return new HashSet<>(skillRepository.findByNameIn(skillNames));
    }
}