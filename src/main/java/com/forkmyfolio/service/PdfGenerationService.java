package com.forkmyfolio.service;

import com.forkmyfolio.model.*;
import com.forkmyfolio.repository.ExperienceRepository;
import com.forkmyfolio.repository.ProjectRepository;
import com.forkmyfolio.repository.QualificationRepository;
import com.forkmyfolio.repository.SkillRepository;
import com.forkmyfolio.service.pdf.PortfolioData;
import com.forkmyfolio.service.pdf.templates.*;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PdfGenerationService {

    private static final Logger log = LoggerFactory.getLogger(PdfGenerationService.class);

    //<editor-fold desc="Repositories">
    private final PortfolioProfileService portfolioProfileService;
    private final ExperienceRepository experienceRepository;
    private final ProjectRepository projectRepository;
    private final SkillRepository skillRepository;
    private final QualificationRepository qualificationRepository;
    //</editor-fold>

    //<editor-fold desc="Color & Font Constants">
    // These are public so templates can access them easily.
    public static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(34, 49, 63);
    public static final DeviceRgb SECONDARY_COLOR = new DeviceRgb(128, 128, 128);
    public static final DeviceRgb ACCENT_COLOR = new DeviceRgb(52, 152, 219);
    public static final DeviceRgb SIDEBAR_BG = new DeviceRgb(245, 245, 245);
    public static final DeviceRgb SIDEBAR_TEXT = new DeviceRgb(51, 51, 51);
    public static final DeviceRgb SKILL_EXPERT_COLOR = new DeviceRgb(40, 167, 69);      // #28A745 (Green)
    public static final DeviceRgb SKILL_INTERMEDIATE_COLOR = new DeviceRgb(0, 123, 255); // #007BFF (Blue)
    public static final DeviceRgb SKILL_BEGINNER_COLOR = new DeviceRgb(255, 193, 7);     // #FFC107 (Amber)
    //</editor-fold>

    // A map to hold available templates. This makes adding new ones easy.
    private final Map<String, PortfolioPdfTemplate> templates = Map.of(
            "modern", new ModernTemplate(),
            "elegance", new EleganceTemplate(),
            "classic", new ClassicTemplate(),
            "metro-dark", new MetroDarkTemplate(),      // <-- ADD THIS
            "business-card", new BusinessCardTemplate() // <-- ADD THIS // <-- REGISTER THE NEW TEMPLATE
            // Future templates can be added here:
            // "classic", new ClassicTemplate()
    );
    /**
     * Returns a list of available template names.
     * @return A list of strings representing the template keys.
     */
    public List<String> getAvailableTemplateNames() {
        return new ArrayList<>(templates.keySet());
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
     * Generates a portfolio PDF using a specified template.
     *
     * @param templateName The name of the template to use (e.g., "modern").
     * @return A PdfFile record containing the byte content and suggested filename.
     */
    public PdfFile generatePortfolioPdf(String templateName) {
        log.info("Starting PDF generation process with template: {}", templateName);

        PortfolioPdfTemplate template = templates.get(templateName.toLowerCase());
        if (template == null) {
            log.error("Requested PDF template '{}' not found. Defaulting to 'modern'.", templateName);
            template = templates.get("modern");
        }

        // 1. Gather all data
        PortfolioProfile profile = portfolioProfileService.getPublicProfile();
        List<Experience> experiences = experienceRepository.findAll();
        List<Qualification> qualifications = qualificationRepository.findAll();
        List<Project> projects = projectRepository.findAll();
        List<Skill> skills = skillRepository.findAll();
        PortfolioData portfolioData = new PortfolioData(profile, experiences, qualifications, projects, skills);

        User user = profile.getUser();
        log.info("Generating PDF for user: {}", user.getEmail());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, template.getPageSize());
        document.setMargins(0, 0, 0, 0);

        try {
            // 2. Create the context with fonts
            PdfContext ctx = new PdfContext(pdf);

            // 3. Delegate the entire layout generation to the chosen template
            template.generate(document, ctx, portfolioData);

        } catch (IOException e) {
            log.error("Failed to load fonts or other resources for PDF generation. Aborting.", e);
            throw new RuntimeException("Failed to generate PDF due to a resource loading issue.", e);
        } finally {
            document.close();
        }

        log.info("PDF generation complete. Final document size: {} bytes.", baos.size());
        String filename = String.format("%s%s-Resume-%s.pdf",
                user.getFirstName(),
                user.getLastName(),
                LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        );
        return new PdfFile(baos.toByteArray(), filename);
    }

    public record PdfFile(byte[] content, String suggestedFilename) {}
}