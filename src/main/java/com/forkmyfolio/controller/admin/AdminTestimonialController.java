package com.forkmyfolio.controller.admin;

import com.forkmyfolio.dto.CreateTestimonialRequest;
import com.forkmyfolio.dto.TestimonialDto;
import com.forkmyfolio.dto.UpdateTestimonialRequest;
import com.forkmyfolio.mapper.TestimonialMapper;
import com.forkmyfolio.model.Testimonial;
import com.forkmyfolio.model.User;
import com.forkmyfolio.service.TestimonialService;
import com.forkmyfolio.service.UserService;
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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/testimonials")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin: Testimonial Management", description = "Endpoints for managing testimonials.")
@SecurityRequirement(name = "bearerAuth")
public class AdminTestimonialController {

    private static final Logger logger = LoggerFactory.getLogger(AdminTestimonialController.class);

    private final TestimonialService testimonialService;
    private final UserService userService;
    private final TestimonialMapper testimonialMapper;

    @Autowired
    public AdminTestimonialController(TestimonialService testimonialService, UserService userService, TestimonialMapper testimonialMapper) {
        this.testimonialService = testimonialService;
        this.userService = userService;
        this.testimonialMapper = testimonialMapper;
    }

    // --- READ (List All) ---
    @GetMapping
    @Operation(summary = "List all testimonials", description = "Retrieves a list of all testimonial entries for the admin to manage.")
    public List<TestimonialDto> getAllTestimonialsForAdmin() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' requested all testimonial entries.", currentUser.getEmail());
        List<Testimonial> testimonials = testimonialService.getPublicTestimonials();
        return testimonials.stream()
                .map(testimonialMapper::toDto)
                .collect(Collectors.toList());
    }

    // --- READ (Single by UUID) ---
    @GetMapping("/{uuid}")
    @Operation(summary = "Get a single testimonial by UUID", description = "Retrieves a single testimonial by its UUID for editing.")
    public TestimonialDto getTestimonialByUuid(@Parameter(description = "The UUID of the testimonial") @PathVariable UUID uuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' requested testimonial with UUID: {}", currentUser.getEmail(), uuid);
        Testimonial testimonial = testimonialService.getTestimonialByUuid(uuid);
        return testimonialMapper.toDto(testimonial);
    }

    // --- CREATE ---
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new testimonial")
    public TestimonialDto createTestimonial(@Valid @RequestBody CreateTestimonialRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' creating new testimonial by: {}", currentUser.getEmail(), request.getAuthorName());
        Testimonial newTestimonial = testimonialMapper.toEntity(request, currentUser);
        Testimonial savedTestimonial = testimonialService.createTestimonial(newTestimonial);
        return testimonialMapper.toDto(savedTestimonial);
    }

    // --- UPDATE ---
    @PutMapping("/{uuid}")
    @Operation(summary = "Update a testimonial by UUID")
    public TestimonialDto updateTestimonial(@Parameter(description = "The UUID of the testimonial") @PathVariable UUID uuid, @Valid @RequestBody UpdateTestimonialRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' updating testimonial UUID: {}", currentUser.getEmail(), uuid);
        Testimonial existingTestimonial = testimonialService.getTestimonialByUuid(uuid);

        // Authorization check
        if (!existingTestimonial.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("User does not have permission to update this testimonial.");
        }

        testimonialMapper.applyUpdateFromRequest(request, existingTestimonial);
        Testimonial updatedTestimonial = testimonialService.save(existingTestimonial);
        return testimonialMapper.toDto(updatedTestimonial);
    }

    // --- DELETE ---
    @DeleteMapping("/{uuid}")
    @Operation(summary = "Delete a testimonial by UUID")
    public ResponseEntity<Void> deleteTestimonial(@Parameter(description = "The UUID of the testimonial") @PathVariable UUID uuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        logger.info("ADMIN User '{}' deleting testimonial UUID: {}", currentUser.getEmail(), uuid);
        testimonialService.deleteTestimonial(uuid, currentUser);
        return ResponseEntity.noContent().build();
    }
}