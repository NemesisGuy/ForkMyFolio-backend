package com.forkmyfolio.service.impl;

import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.model.*;
import com.forkmyfolio.repository.*;
import com.forkmyfolio.service.PortfolioProfileService;
import com.forkmyfolio.service.UserService;
import com.forkmyfolio.service.pdf.PortfolioData;
import com.forkmyfolio.service.pdf.templates.*;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PdfGenerationService {


    //<editor-fold desc="Color & Font Constants">
    // These are public so templates can access them easily.
    public static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(34, 49, 63);
    public static final DeviceRgb SECONDARY_COLOR = new DeviceRgb(128, 128, 128);
    public static final DeviceRgb ACCENT_COLOR = new DeviceRgb(52, 152, 219);
    public static final DeviceRgb SIDEBAR_BG = new DeviceRgb(245, 245, 245);
    public static final DeviceRgb SIDEBAR_TEXT = new DeviceRgb(51, 51, 51);
    public static final DeviceRgb SKILL_EXPERT_COLOR = new DeviceRgb(22, 160, 133);      // #16A085 (Teal Green)
    //</editor-fold>
    public static final DeviceRgb SKILL_ADVANCED_COLOR = new DeviceRgb(52, 152, 219);    // #3498DB (Soft Blue)
    public static final DeviceRgb SKILL_INTERMEDIATE_COLOR = new DeviceRgb(241, 196, 15); // #F1C40F (Golden Yellow)
    public static final DeviceRgb SKILL_BEGINNER_COLOR = new DeviceRgb(231, 76, 60);     // #E74C3C (Soft Red)
    private static final Logger log = LoggerFactory.getLogger(PdfGenerationService.class);
    //<editor-fold desc="Repositories">
    private final ExperienceRepository experienceRepository;
    private final ProjectRepository projectRepository;
    private final QualificationRepository qualificationRepository;
    private final PortfolioProfileService portfolioProfileService;
    private final SettingRepository settingRepository;
    private final UserService userService;

    //</editor-fold>
    // A map to hold available templates. This makes adding new ones easy.
    private final Map<String, PortfolioPdfTemplate> templates = Map.of(
            "modern", new ModernTemplate(),
            "elegance", new EleganceTemplate(),
            "classic", new ClassicTemplate(),
            "metro-dark", new MetroDarkTemplate(),
            "business-card", new BusinessCardTemplate()
    );

    private static PdfFont loadOptionalFontFromResource(String resourcePath) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                log.warn("Font resource not found on classpath: '{}'. Icons may not display.", resourcePath);
                return null;
            }
            byte[] fontBytes = is.readAllBytes();
            return PdfFontFactory.createFont(fontBytes, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
        } catch (IOException e) {
            log.error("Failed to load font resource '{}': {}", resourcePath, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Returns a list of available template names.
     *
     * @return A list of strings representing the template keys.
     */
    public List<String> getAvailableTemplateNames() {
        return new ArrayList<>(templates.keySet());
    }

    /**
     * Generates a portfolio PDF for a specific user using a specified template.
     *
     * @param user         The User entity for whom the portfolio is being generated.
     * @param templateName The name of the template to use (e.g., "modern").
     * @return A PdfFile record containing the byte content and suggested filename.
     */
    @Transactional(readOnly = true)
    public PdfFile generatePortfolioPdf(User user, String templateName) {
        // The user object passed in is detached. We must fetch a fresh, fully-initialized
        // user within this transaction to avoid LazyInitializationException on collections like 'userSettings'.
        User freshUser = userService.findBySlugWithAllPortfolioData(user.getSlug())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with slug: " + user.getSlug()));

        // The templateName parameter is now ignored in favor of settings.
        // This ensures the user's chosen default is always respected for their portfolio.
        String effectiveTemplateName = freshUser.getUserSettings().stream()
                .filter(s -> "portfolio.pdf.template".equals(s.getName()))
                .map(UserSetting::getValue)
                .findFirst()
                .orElseGet(() -> settingRepository.findByName("portfolio.pdf.template")
                        .map(Setting::getValue)
                        .orElse("modern")); // Final fallback if user and global settings are missing.

        log.info("Starting PDF generation process for user '{}' with effective template: {}", freshUser.getSlug(), effectiveTemplateName);

        // FIX: The 'profile' variable was missing. It needs to be fetched using the
        // PortfolioProfileService to ensure the get-or-create logic is applied.
        PortfolioProfile profile = portfolioProfileService.getProfileByUser(freshUser);
        // Fetch all items for the specific user, using sorted methods where appropriate.
        List<Experience> experiences = experienceRepository.findByUserOrderByDisplayOrderAsc(freshUser);
        List<Qualification> qualifications = qualificationRepository.findByUserOrderByCompletionYearDescStartYearDesc(freshUser);
        List<Project> projects = projectRepository.findByUserOrderByDisplayOrderAsc(freshUser);

        // The User entity now holds the skill relationships. We must fetch from there.
        // We also need to construct a transient Skill object for the PDF data,
        // combining global skill info with user-specific details.
        List<Skill> skills = freshUser.getUserSkills().stream()
                .filter(UserSkill::isVisible) // Only include skills the user wants to show
                .map(userSkill -> {
                    Skill globalSkill = userSkill.getSkill();
                    // Create a transient Skill object for the PDF, populated with the correct data
                    Skill pdfSkill = new Skill();
                    pdfSkill.setName(globalSkill.getName());
                    pdfSkill.setCategory(globalSkill.getCategory());
                    pdfSkill.setIcon(globalSkill.getIcon());
                    // Use user-specific data where it exists
                    pdfSkill.setLevel(userSkill.getLevel());
                    pdfSkill.setDescription(userSkill.getDescription());
                    pdfSkill.setVisible(userSkill.isVisible());
                    return pdfSkill;
                })
                .collect(Collectors.toList());

        PortfolioData portfolioData = new PortfolioData(profile, experiences, qualifications, projects, skills);

        log.info("Generating PDF for user: {}", freshUser.getEmail());

        // 2. Select the template
        PortfolioPdfTemplate template = templates.get(effectiveTemplateName.toLowerCase());
        if (template == null) {
            log.error("Effective PDF template '{}' not found. Defaulting to 'modern'.", effectiveTemplateName);
            template = templates.get("modern");
        }

        // 3. Generate the PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, template.getPageSize());
        document.setMargins(0, 0, 0, 0);

        try {
            PdfContext ctx = new PdfContext(pdf);
            template.generate(document, ctx, portfolioData);
        } catch (IOException e) {
            log.error("Failed to load fonts or other resources for PDF generation. Aborting.", e);
            throw new RuntimeException("Failed to generate PDF due to a resource loading issue.", e);
        } finally {
            document.close();
        }

        log.info("PDF generation complete. Final document size: {} bytes.", baos.size());
        String filename = String.format("%s%s-Resume-%s.pdf",
                freshUser.getFirstName(),
                freshUser.getLastName(),
                LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        );
        return new PdfFile(baos.toByteArray(), filename);
    }

    /**
     * The context object holds resources for a single PDF generation, like fonts.
     * It's public so it can be accessed by templates in other packages.
     */
    public static class PdfContext {
        public final PdfDocument pdfDocument;
        public final PdfFont nameFont, headlineFont, sectionHeaderFont, itemTitleFont, itemSubtitleFont, bodyFont, dateFont, solidIconFont, brandsIconFont;

        public PdfContext(PdfDocument pdfDocument) throws IOException {
            this.pdfDocument = pdfDocument;
            this.nameFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            this.headlineFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);
            this.sectionHeaderFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            this.itemTitleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            this.itemSubtitleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);
            this.bodyFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            this.dateFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            this.solidIconFont = loadOptionalFontFromResource("fonts/Font Awesome 6 Free-Solid-900.otf");
            this.brandsIconFont = loadOptionalFontFromResource("fonts/Font Awesome 6 Brands-Regular-400.otf");
        }
    }

    public record PdfFile(byte[] content, String suggestedFilename) {
    }
}