package com.forkmyfolio.controller;

import com.forkmyfolio.aop.SkipApiResponseWrapper;
import com.forkmyfolio.aop.TrackVisitor;
import com.forkmyfolio.dto.response.*;
import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.mapper.*;
import com.forkmyfolio.model.User;
import com.forkmyfolio.model.UserSkill;
import com.forkmyfolio.model.enums.VisitorStatType;
import com.forkmyfolio.service.PortfolioService;
import com.forkmyfolio.service.UserService;
import com.forkmyfolio.service.impl.MarkdownGenerationService;
import com.forkmyfolio.service.impl.PdfGenerationService;
import com.forkmyfolio.service.impl.UserSettingService;
import com.forkmyfolio.service.impl.VCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/portfolios")
@Tag(name = "Public Portfolios", description = "Endpoints for viewing public user portfolios.")
@RequiredArgsConstructor
public class PortfolioController {

    // Services
    private final PortfolioService portfolioService;
    private final UserService userService;
    private final UserSettingService userSettingService;
    private final PdfGenerationService pdfGenerationService;
    private final MarkdownGenerationService markdownGenerationService;
    private final VCardService vCardService;

    // Mappers for DTO conversion
    private final PublicUserMapper publicUserMapper;
    private final PortfolioProfileMapper portfolioProfileMapper;
    private final ProjectMapper projectMapper;
    private final ExperienceMapper experienceMapper;
    private final QualificationMapper qualificationMapper;
    private final TestimonialMapper testimonialMapper;
    private final UserSkillMapper userSkillMapper;

    @GetMapping("/{slug}")
    @Operation(summary = "Get a user's full public portfolio by their slug",
            description = "Retrieves all publicly visible sections of a user's portfolio, such as their profile, projects, skills, and experience.")
    @TrackVisitor(VisitorStatType.TOTAL_VISITS)
    public ResponseEntity<PortfolioDto> getPortfolioBySlug(
            @Parameter(description = "The unique, URL-friendly slug of the user.", example = "jane-doe")
            @PathVariable String slug) {

        // 1. The service returns a single, fully-populated User entity. No more repository calls in the controller.
        User user = portfolioService.getPublicPortfolioUserBySlug(slug);

        // 2. Create the skill lookup map. This provides the necessary context for detailed skill mapping.
        Map<UUID, UserSkill> skillLookup = user.getUserSkills().stream()
                .collect(Collectors.toMap(us -> us.getSkill().getUuid(), us -> us));

        // 3. Map entities to DTOs, filtering for visibility and using the context-aware mappers.
        PublicUserDto userDto = publicUserMapper.toDto(user);
        PortfolioProfileDto profileDto = portfolioProfileMapper.toDto(user.getPortfolioProfile());

        List<ProjectDto> projectDtos = user.getProjects().stream()
                .filter(p -> p.isVisible())
                .map(project -> projectMapper.toDto(project, skillLookup)) // Use the context-aware mapper
                .collect(Collectors.toList());

        List<ExperienceDto> experienceDtos = user.getExperiences().stream()
                .filter(e -> e.isVisible())
                .map(experience -> experienceMapper.toDto(experience, skillLookup)) // Use the context-aware mapper
                .collect(Collectors.toList());

        List<UserSkillDto> skillDtos = user.getUserSkills().stream()
                .filter(us -> us.isVisible())
                .map(userSkillMapper::toDto)
                .collect(Collectors.toList());

        List<QualificationDto> qualificationDtos = user.getQualifications().stream()
                .filter(q -> q.isVisible())
                .map(qualificationMapper::toDto)
                .collect(Collectors.toList());

        List<TestimonialDto> testimonialDtos = user.getTestimonials().stream()
                .filter(t -> t.isVisible())
                .map(testimonialMapper::toDto)
                .collect(Collectors.toList());

        // 4. Assemble the final DTO for the API response.
        PortfolioDto portfolioDto = new PortfolioDto();
        portfolioDto.setUser(userDto);
        portfolioDto.setProfile(profileDto);
        portfolioDto.setProjects(projectDtos);
        portfolioDto.setSkills(skillDtos);
        portfolioDto.setExperiences(experienceDtos);
        portfolioDto.setQualifications(qualificationDtos);
        portfolioDto.setTestimonials(testimonialDtos);

        return ResponseEntity.ok(portfolioDto);
    }

    @GetMapping("/{slug}/settings")
    @Operation(summary = "Get public settings for a portfolio", description = "Retrieves the list of effective settings for a user's public portfolio, identified by their unique slug.")
    public ResponseEntity<List<UserSettingDto>> getPortfolioSettings(
            @Parameter(description = "The unique, URL-friendly slug of the user.", example = "jane-doe")
            @PathVariable String slug) {
        User user = userService.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio " + "slug: " + slug));
        List<UserSettingDto> settings = userSettingService.getEffectiveSettingsForUser(user);
        // The ApiResponseWrapper will be applied automatically
        return ResponseEntity.ok(settings);
    }

    @GetMapping("/{slug}/pdf")
    @Operation(summary = "Download a user's portfolio as a PDF",
            description = "Generates and downloads a PDF version of the user's portfolio using a specified template.")
    @SkipApiResponseWrapper // This response is a file stream, not JSON, so we skip the standard wrapper.
    @TrackVisitor(VisitorStatType.PDF_DOWNLOAD)
    public ResponseEntity<byte[]> downloadPortfolioAsPdf(
            @Parameter(description = "The unique, URL-friendly slug of the user.", example = "jane-doe")
            @PathVariable String slug,
            @Parameter(description = "The name of the PDF template to use.", example = "modern")
            @RequestParam(defaultValue = "modern") String template) {

        PdfGenerationService.PdfFile pdfFile = pdfGenerationService.generatePortfolioPdf(slug, template);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", pdfFile.suggestedFilename());

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfFile.content());
    }

    @GetMapping("/{slug}/markdown")
    @Operation(summary = "Download a user's portfolio as a Markdown file",
            description = "Generates and downloads a GitHub-friendly Markdown (.md) version of the user's portfolio.")
    @SkipApiResponseWrapper // This response is a file stream, not JSON
    @TrackVisitor(VisitorStatType.MARKDOWN_DOWNLOAD)
    public ResponseEntity<byte[]> downloadPortfolioAsMarkdown(
            @Parameter(description = "The unique, URL-friendly slug of the user.", example = "jane-doe")
            @PathVariable String slug) {

        MarkdownGenerationService.MarkdownFile mdFile = markdownGenerationService.generatePortfolioMarkdown(slug);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/markdown;charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", mdFile.suggestedFilename());

        return ResponseEntity.ok()
                .headers(headers)
                .body(mdFile.content());
    }

    @GetMapping("/{slug}/vcard")
    @Operation(summary = "Download a user's contact info as a vCard",
            description = "Generates and downloads a vCard (.vcf) file for the specified user.")
    @SkipApiResponseWrapper // This response is a file stream, not JSON
    @TrackVisitor(VisitorStatType.VCARD_DOWNLOAD)
    public ResponseEntity<byte[]> downloadVCard(
            @Parameter(description = "The unique, URL-friendly slug of the user.", example = "jane-doe")
            @PathVariable String slug) {

        VCardService.VCardFile vCardFile = vCardService.generateVCard(slug);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("text/vcard"));
        headers.setContentDispositionFormData("attachment", vCardFile.suggestedFilename());

        return ResponseEntity.ok().headers(headers).body(vCardFile.content());
    }
}