package com.forkmyfolio.service.impl;

import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.model.Project;
import com.forkmyfolio.model.Skill;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.ProjectRepository;
import com.forkmyfolio.service.ProjectService;
import com.forkmyfolio.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of the {@link ProjectService} interface.
 * Handles business logic related to portfolio projects.
 */
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final SkillService skillService;

    @Override
    @Transactional(readOnly = true)
    public List<Project> getProjectsForUser(User user) {
        return projectRepository.findByUserOrderByDisplayOrderAsc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Project findProjectByUuidAndUser(UUID uuid, User user) {
        Project project = projectRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Project with UUID: " + uuid));

        if (!project.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("User does not have permission to access this project.");
        }
        return project;
    }

    @Override
    @Transactional
    public Project createProject(Project project, Set<String> skillNames) {
        // Delegate to SkillService to find or create the associated global skills.
        Set<Skill> skills = skillService.findOrCreateSkills(skillNames);
        project.setSkills(skills);
        return projectRepository.save(project);
    }

    @Override
    @Transactional
    public Project updateProject(UUID uuid, Project updatedProjectData, Set<String> skillNames, User currentUser) {
        Project existingProject = findProjectByUuidAndUser(uuid, currentUser);

        // Update fields from the provided data object
        existingProject.setTitle(updatedProjectData.getTitle());
        existingProject.setDescription(updatedProjectData.getDescription());
        existingProject.setRepoUrl(updatedProjectData.getRepoUrl());
        existingProject.setLiveUrl(updatedProjectData.getLiveUrl());
        existingProject.setImageUrl(updatedProjectData.getImageUrl());
        existingProject.setVisible(updatedProjectData.isVisible());
        existingProject.setDisplayOrder(updatedProjectData.getDisplayOrder());

        // Update associated skills by finding or creating them in the global pool.
        Set<Skill> skillsToAssociate = skillService.findOrCreateSkills(skillNames);
        existingProject.setSkills(skillsToAssociate);

        return projectRepository.save(existingProject);
    }

    @Override
    @Transactional
    public void deleteProject(UUID uuid, User currentUser) {
        Project projectToDelete = findProjectByUuidAndUser(uuid, currentUser);
        projectRepository.delete(projectToDelete);
    }
}