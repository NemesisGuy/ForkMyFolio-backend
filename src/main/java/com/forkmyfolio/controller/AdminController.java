package com.forkmyfolio.controller;

import com.forkmyfolio.dto.*;
import com.forkmyfolio.mapper.*;
import com.forkmyfolio.model.*;
import com.forkmyfolio.model.Skill;
import com.forkmyfolio.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller for all administrative actions, secured for ADMIN role.
 * This is the central point for managing all portfolio content.
 */
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Endpoints for portfolio content management (Admin only)")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    // --- Services ---
    private final UserService userService;
    private final PortfolioProfileService portfolioProfileService;
    private final ProjectService projectService;
    private final SkillService skillService;
    private final ExperienceService experienceService;
    private final TestimonialService testimonialService;
    private final QualificationService qualificationService;

    // --- Mappers ---
    private final UserMapper userMapper;
    private final ProjectMapper projectMapper;
    private final SkillMapper skillMapper;
    private final ExperienceMapper experienceMapper;
    private final TestimonialMapper testimonialMapper;
    private final QualificationMapper qualificationMapper;
    private final PortfolioProfileMapper portfolioProfileMapper;

    @Autowired
    public AdminController(UserService userService, PortfolioProfileService portfolioProfileService, ProjectService projectService, SkillService skillService, ExperienceService experienceService, TestimonialService testimonialService, QualificationService qualificationService, UserMapper userMapper, ProjectMapper projectMapper, SkillMapper skillMapper, ExperienceMapper experienceMapper, TestimonialMapper testimonialMapper, QualificationMapper qualificationMapper, PortfolioProfileMapper portfolioProfileMapper) {
        this.userService = userService;
        this.portfolioProfileService = portfolioProfileService;
        this.projectService = projectService;
        this.skillService = skillService;
        this.experienceService = experienceService;
        this.testimonialService = testimonialService;
        this.qualificationService = qualificationService;
        this.userMapper = userMapper;
        this.projectMapper = projectMapper;
        this.skillMapper = skillMapper;
        this.experienceMapper = experienceMapper;
        this.testimonialMapper = testimonialMapper;
        this.qualificationMapper = qualificationMapper;
        this.portfolioProfileMapper = portfolioProfileMapper;
    }

    // --- Account & Profile Management ---

    @GetMapping("/account")
    @Operation(summary = "Get current admin user account", description = "Fetches account info (name, email, roles) for the logged-in admin.")
    public UserDto getAdminAccount() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' requested their account details.", currentUser.getEmail());
        return userMapper.toDto(currentUser);
    }
    @PutMapping("/account")
    @Operation(summary = "Update admin user account", description = "Updates the admin's own account details (e.g., first and last name).")
    public UserDto updateAdminAccount(@Valid @RequestBody UpdateUserAccountRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' is updating their account details.", currentUser.getEmail());

        // Controller calls the service with primitive values from the DTO
        User updatedUser = userService.updateUserAccount(
                currentUser.getId(),
                request.getFirstName(),
                request.getLastName()
        );

        logger.info("Successfully updated account details for user '{}'.", currentUser.getEmail());
        return userMapper.toDto(updatedUser);
    }

    // --- FIX: THE ENDPOINT IS RENAMED HERE ---
    @PutMapping("/portfolio-profile")
    @Operation(summary = "Update public portfolio profile", description = "Updates the main public profile information (headline, summary, links, etc.).")
    public ResponseEntity<Void> updatePortfolioProfile(@Valid @RequestBody UpdatePortfolioProfileRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' is updating their public portfolio profile.", currentUser.getEmail());

        PortfolioProfile existingProfile = portfolioProfileService.getProfileForUser(currentUser);
        portfolioProfileMapper.applyUpdateFromRequest(request, existingProfile);
        portfolioProfileService.save(existingProfile);

        logger.info("Successfully updated public portfolio profile for user '{}'.", currentUser.getEmail());
        return ResponseEntity.ok().build();
    }

    // --- Project Management ---

    @PostMapping("/projects")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new project")
    public ProjectDto createProject(@Valid @RequestBody CreateProjectRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' creating new project: {}", currentUser.getEmail(), request.getTitle());
        Project newProject = projectMapper.toEntity(request, currentUser);
        Project savedProject = projectService.createProject(newProject);
        return projectMapper.toDto(savedProject);
    }

    @PutMapping("/projects/{uuid}")
    @Operation(summary = "Update a project by UUID")
    public ProjectDto updateProject(@Parameter(description = "The UUID of the project") @PathVariable UUID uuid, @Valid @RequestBody UpdateProjectRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' updating project UUID: {}", currentUser.getEmail(), uuid);
        Project existingProject = projectService.getProjectByUuid(uuid);
        projectMapper.applyUpdateFromRequest(request, existingProject);
        Project updatedProject = projectService.save(existingProject);
        return projectMapper.toDto(updatedProject);
    }

    @DeleteMapping("/projects/{uuid}")
    @Operation(summary = "Delete a project by UUID")
    public ResponseEntity<Void> deleteProject(@Parameter(description = "The UUID of the project") @PathVariable UUID uuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' deleting project UUID: {}", currentUser.getEmail(), uuid);
        projectService.deleteProject(uuid, currentUser);
        return ResponseEntity.noContent().build();
    }

    // --- Skill Management ---

    @PostMapping("/skills")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new skill")
    public SkillDto createSkill(@Valid @RequestBody CreateSkillRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' creating new skill: {}", currentUser.getEmail(), request.getName());
        Skill newSkill = skillMapper.toEntity(request, currentUser);
        Skill savedSkill = skillService.createSkill(newSkill);
        return skillMapper.toDto(savedSkill);
    }

    @DeleteMapping("/skills/{uuid}")
    @Operation(summary = "Delete a skill by UUID")
    public ResponseEntity<Void> deleteSkill(@Parameter(description = "The UUID of the skill") @PathVariable UUID uuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' deleting skill UUID: {}", currentUser.getEmail(), uuid);
        skillService.deleteSkill(uuid, currentUser);
        return ResponseEntity.noContent().build();
    }

    // --- Experience Management ---

    @PostMapping("/experience")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new experience entry")
    public ExperienceDto createExperience(@Valid @RequestBody CreateExperienceRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' creating new experience: {}", currentUser.getEmail(), request.getJobTitle());
        Experience newExperience = experienceMapper.toEntity(request, currentUser);
        Experience savedExperience = experienceService.createExperience(newExperience);
        return experienceMapper.toDto(savedExperience);
    }

    @PutMapping("/experience/{uuid}")
    @Operation(summary = "Update an experience entry by UUID")
    public ExperienceDto updateExperience(@Parameter(description = "The UUID of the experience") @PathVariable UUID uuid, @Valid @RequestBody UpdateExperienceRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' updating experience UUID: {}", currentUser.getEmail(), uuid);
        Experience existingExperience = experienceService.getExperienceByUuid(uuid);
        experienceMapper.applyUpdateFromRequest(request, existingExperience);
        Experience updatedExperience = experienceService.save(existingExperience);
        return experienceMapper.toDto(updatedExperience);
    }

    @DeleteMapping("/experience/{uuid}")
    @Operation(summary = "Delete an experience entry by UUID")
    public ResponseEntity<Void> deleteExperience(@Parameter(description = "The UUID of the experience") @PathVariable UUID uuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' deleting experience UUID: {}", currentUser.getEmail(), uuid);
        experienceService.deleteExperience(uuid, currentUser);
        return ResponseEntity.noContent().build();
    }

    // --- Testimonial Management ---

    @PostMapping("/testimonials")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new testimonial")
    public TestimonialDto createTestimonial(@Valid @RequestBody CreateTestimonialRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' creating new testimonial by: {}", currentUser.getEmail(), request.getAuthorName());
        Testimonial newTestimonial = testimonialMapper.toEntity(request, currentUser);
        Testimonial savedTestimonial = testimonialService.createTestimonial(newTestimonial);
        return testimonialMapper.toDto(savedTestimonial);
    }

    @PutMapping("/testimonials/{uuid}")
    @Operation(summary = "Update a testimonial by UUID")
    public TestimonialDto updateTestimonial(@Parameter(description = "The UUID of the testimonial") @PathVariable UUID uuid, @Valid @RequestBody UpdateTestimonialRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' updating testimonial UUID: {}", currentUser.getEmail(), uuid);
        Testimonial existingTestimonial = testimonialService.getTestimonialByUuid(uuid);
        testimonialMapper.applyUpdateFromRequest(request, existingTestimonial);
        Testimonial updatedTestimonial = testimonialService.save(existingTestimonial);
        return testimonialMapper.toDto(updatedTestimonial);
    }

    @DeleteMapping("/testimonials/{uuid}")
    @Operation(summary = "Delete a testimonial by UUID")
    public ResponseEntity<Void> deleteTestimonial(@Parameter(description = "The UUID of the testimonial") @PathVariable UUID uuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' deleting testimonial UUID: {}", currentUser.getEmail(), uuid);
        testimonialService.deleteTestimonial(uuid, currentUser);
        return ResponseEntity.noContent().build();
    }

    // --- Qualification Management ---

    @PostMapping("/qualifications")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new qualification")
    public QualificationDto createQualification(@Valid @RequestBody CreateQualificationRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' creating new qualification: {}", currentUser.getEmail(), request.getQualificationName());
        Qualification newQualification = qualificationMapper.toEntity(request, currentUser);
        Qualification savedQualification = qualificationService.createQualification(newQualification);
        return qualificationMapper.toDto(savedQualification);
    }

    @PutMapping("/qualifications/{uuid}")
    @Operation(summary = "Update a qualification by UUID")
    public QualificationDto updateQualification(@Parameter(description = "The UUID of the qualification") @PathVariable UUID uuid, @Valid @RequestBody UpdateQualificationRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' updating qualification UUID: {}", currentUser.getEmail(), uuid);
        Qualification existingQualification = qualificationService.getQualificationByUuid(uuid);
        qualificationMapper.applyUpdateFromRequest(request, existingQualification);
        Qualification updatedQualification = qualificationService.save(existingQualification);
        return qualificationMapper.toDto(updatedQualification);
    }

    @DeleteMapping("/qualifications/{uuid}")
    @Operation(summary = "Delete a qualification by UUID")
    public ResponseEntity<Void> deleteQualification(@Parameter(description = "The UUID of the qualification") @PathVariable UUID uuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' deleting qualification UUID: {}", currentUser.getEmail(), uuid);
        qualificationService.deleteQualification(uuid, currentUser);
        return ResponseEntity.noContent().build();
    }
}