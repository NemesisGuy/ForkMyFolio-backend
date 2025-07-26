package com.forkmyfolio.service.pdf.templates;

import com.forkmyfolio.model.*;
import com.forkmyfolio.service.PdfGenerationService;
import com.forkmyfolio.service.pdf.PortfolioData;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A sophisticated, single-column "Elegance" template, now in a high-contrast light mode.
 */
public class EleganceTemplate implements PortfolioPdfTemplate {

    // --- Template-Specific Colors (Light Mode) ---
    private static final Color BG_COLOR = ColorConstants.WHITE;
    private static final Color TEXT_COLOR = new DeviceRgb(34, 40, 49); // #222831 (Dark Charcoal)
    private static final Color ACCENT_COLOR = new DeviceRgb(0, 173, 181); // #00ADB5 (Teal)
    private static final Color SUBTLE_COLOR = new DeviceRgb(108, 117, 125); // #6c757d (Medium Gray)

    // --- Template-Specific Fonts ---
    private PdfFont headingFont;
    private PdfFont bodyFont;
    private PdfFont bodyItalicFont;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");

    @Override
    public void generate(Document document, PdfGenerationService.PdfContext ctx, PortfolioData data) throws IOException {
        // This template uses its own fonts and colors, so we set them up here.
        initializeTemplateResources();
        document.setBackgroundColor(BG_COLOR);
        document.setFontColor(TEXT_COLOR);
        document.setFont(bodyFont);
        document.setMargins(40, 40, 40, 40);

        // 1. Header
        createHeader(document, data.profile(), ctx.solidIconFont, ctx.brandsIconFont);

        // 2. Summary
        createSection(document, "Summary", data.profile().getSummary());

        // 3. Skills
        createSkillsSection(document, data.skills());

        // 4. Experience
        createExperienceSection(document, data.experiences());

        // 5. Projects
        createProjectsSection(document, data.projects());

        // 6. Qualifications
        createQualificationsSection(document, data.qualifications());
    }

    private void initializeTemplateResources() throws IOException {
        this.headingFont = loadFontFromResource("fonts/Montserrat-Bold.ttf");
        this.bodyFont = loadFontFromResource("fonts/Lato-Regular.ttf");
        this.bodyItalicFont = loadFontFromResource("fonts/Lato-Italic.ttf");
    }

    private void createHeader(Document doc, PortfolioProfile profile, PdfFont solidIconFont, PdfFont brandsIconFont) {
        User user = profile.getUser();
        // Name
        doc.add(new Paragraph(user.getFirstName().toUpperCase() + " " + user.getLastName().toUpperCase())
                .setFont(headingFont).setFontSize(32).setMarginBottom(0));
        // Headline
        doc.add(new Paragraph(profile.getHeadline())
                .setFont(bodyFont).setFontSize(14).setFontColor(ACCENT_COLOR).setMarginTop(0).setMarginBottom(10));

        // --- IMPROVED: DYNAMIC CONTACT SECTION ---
        List<Paragraph> contactItems = new ArrayList<>();

        // Email
        if (profile.getPublicEmail() != null && !profile.getPublicEmail().isBlank()) {
            contactItems.add(createContactItem("\uf0e0", "Email", "mailto:" + profile.getPublicEmail(), solidIconFont));
        }
        // Website
        if (profile.getWebsiteUrl() != null && !profile.getWebsiteUrl().isBlank()) {
            contactItems.add(createContactItem("\uf0c1", "Website", profile.getWebsiteUrl(), solidIconFont));
        }
        // LinkedIn
        if (profile.getLinkedinUrl() != null && !profile.getLinkedinUrl().isBlank()) {
            contactItems.add(createContactItem("\uf08c", "LinkedIn", profile.getLinkedinUrl(), brandsIconFont));
        }
        // GitHub
        if (profile.getGithubUrl() != null && !profile.getGithubUrl().isBlank()) {
            contactItems.add(createContactItem("\uf09b", "GitHub", profile.getGithubUrl(), brandsIconFont));
        }

        if (!contactItems.isEmpty()) {
            // Create a table with a dynamic number of columns for perfect spacing
            Table contactTable = new Table(UnitValue.createPercentArray(contactItems.size())).useAllAvailableWidth().setBorder(Border.NO_BORDER);
            for (Paragraph item : contactItems) {
                contactTable.addCell(new Cell().add(item).setBorder(Border.NO_BORDER).setPadding(2));
            }
            doc.add(contactTable);
        }

        // Separator
        doc.add(new Paragraph().setBorderBottom(new SolidBorder(ACCENT_COLOR, 1f)).setMarginTop(15));
    }

    private Paragraph createContactItem(String iconChar, String text, String link, PdfFont iconFont) {
        Paragraph p = new Paragraph().setTextAlignment(TextAlignment.CENTER);
        if (iconFont != null && iconFont.containsGlyph(iconChar.codePointAt(0))) {
            p.add(new Text(iconChar + " ").setFont(iconFont).setFontColor(ACCENT_COLOR));
        }
        // Make the text a styled, clickable link
        Text linkText = new Text(text)
                .setAction(PdfAction.createURI(link))
                .setFontColor(ACCENT_COLOR) // Use accent color for the link text
                .setUnderline(0.7f, -2);    // Add a subtle underline

        p.add(linkText);
        return p;
    }

    private void createSection(Document doc, String title, String content) {
        if (content == null || content.isBlank()) return;
        doc.add(new Paragraph(title.toUpperCase())
                .setFont(headingFont).setFontSize(14).setFontColor(ACCENT_COLOR)
                .setMarginTop(20).setMarginBottom(5));
        doc.add(new Paragraph(content).setMultipliedLeading(1.2f));
    }

    private void createSkillsSection(Document doc, List<Skill> skills) {
        if (skills.isEmpty()) return;
        doc.add(new Paragraph("SKILLS")
                .setFont(headingFont).setFontSize(14).setFontColor(ACCENT_COLOR)
                .setMarginTop(20).setMarginBottom(10));

        Paragraph skillParagraph = new Paragraph();
        skills.sort(Comparator.comparing(Skill::getLevel).reversed().thenComparing(Skill::getName));

        for (Skill skill : skills) {
            Color pillColor = switch (skill.getLevel()) {
                case EXPERT -> PdfGenerationService.SKILL_EXPERT_COLOR;
                case INTERMEDIATE -> PdfGenerationService.SKILL_INTERMEDIATE_COLOR;
                case BEGINNER -> PdfGenerationService.SKILL_BEGINNER_COLOR;
            };
            // White text on the colored pills still provides good contrast.
            Text skillPill = new Text(" " + skill.getName() + " ")
                    .setBackgroundColor(pillColor)
                    .setFontColor(ColorConstants.WHITE)
                    .setFontSize(9);

            skillParagraph.add(skillPill).add(new Text("  ")); // Add pill and space
        }
        doc.add(skillParagraph);
    }

    private void createExperienceSection(Document doc, List<Experience> experiences) {
        if (experiences.isEmpty()) return;
        doc.add(new Paragraph("EXPERIENCE")
                .setFont(headingFont).setFontSize(14).setFontColor(ACCENT_COLOR)
                .setMarginTop(20).setMarginBottom(5));

        experiences.sort(Comparator.comparing(Experience::getStartDate).reversed());
        for (Experience exp : experiences) {
            String dateText = exp.getStartDate().format(DATE_FORMATTER) + " - " +
                    (exp.getEndDate() == null ? "Present" : exp.getEndDate().format(DATE_FORMATTER));

            Table itemHeader = new Table(UnitValue.createPercentArray(new float[]{3, 1})).useAllAvailableWidth();
            itemHeader.addCell(new Cell().add(new Paragraph(exp.getJobTitle()).setFont(bodyFont).setFontSize(12).setBold())
                    .setBorder(Border.NO_BORDER));
            itemHeader.addCell(new Cell().add(new Paragraph(dateText).setTextAlignment(TextAlignment.RIGHT))
                    .setBorder(Border.NO_BORDER));
            doc.add(itemHeader.setMarginBottom(0).setPadding(0));

            doc.add(new Paragraph(exp.getCompanyName() + (exp.getLocation() != null ? ", " + exp.getLocation() : ""))
                    .setFont(bodyItalicFont).setFontColor(SUBTLE_COLOR).setFontSize(10).setMarginTop(0));

            doc.add(new Paragraph(exp.getDescription()).setMarginTop(5).setMarginBottom(15).setMultipliedLeading(1.2f));
        }
    }

    private void createProjectsSection(Document doc, List<Project> projects) {
        if (projects.isEmpty()) return;
        doc.add(new Paragraph("PROJECTS")
                .setFont(headingFont).setFontSize(14).setFontColor(ACCENT_COLOR)
                .setMarginTop(20).setMarginBottom(5));

        for (Project proj : projects) {
            Text titleText = new Text(proj.getTitle()).setFont(bodyFont).setFontSize(12).setBold();
            String projectUrl = proj.getLiveUrl() != null ? proj.getLiveUrl() : proj.getRepoUrl();
            if (projectUrl != null && !projectUrl.isBlank()) {
                titleText.setAction(PdfAction.createURI(projectUrl)).setUnderline().setFontColor(ACCENT_COLOR);
            }
            doc.add(new Paragraph(titleText));

            if (proj.getTechStack() != null && !proj.getTechStack().isEmpty()) {
                String techStackString = String.join(" • ", proj.getTechStack());
                doc.add(new Paragraph(techStackString)
                        .setFont(bodyItalicFont).setFontColor(SUBTLE_COLOR).setFontSize(10).setMarginTop(0));
            }
            doc.add(new Paragraph(proj.getDescription()).setMarginTop(5).setMarginBottom(15).setMultipliedLeading(1.2f));
        }
    }

    private void createQualificationsSection(Document doc, List<Qualification> qualifications) {
        if (qualifications.isEmpty()) return;
        doc.add(new Paragraph("QUALIFICATIONS")
                .setFont(headingFont).setFontSize(14).setFontColor(ACCENT_COLOR)
                .setMarginTop(20).setMarginBottom(5));

        qualifications.sort(Comparator.comparing(Qualification::getCompletionYear).reversed());
        for (Qualification qual : qualifications) {
            Table itemHeader = new Table(UnitValue.createPercentArray(new float[]{3, 1})).useAllAvailableWidth();
            itemHeader.addCell(new Cell().add(new Paragraph(qual.getQualificationName()).setFont(bodyFont).setFontSize(12).setBold())
                    .setBorder(Border.NO_BORDER));
            itemHeader.addCell(new Cell().add(new Paragraph(String.valueOf(qual.getCompletionYear())).setTextAlignment(TextAlignment.RIGHT))
                    .setBorder(Border.NO_BORDER));
            doc.add(itemHeader.setMarginBottom(0).setPadding(0));

            doc.add(new Paragraph(qual.getInstitutionName())
                    .setFont(bodyItalicFont).setFontColor(SUBTLE_COLOR).setFontSize(10).setMarginTop(0));

            if (qual.getGrade() != null && !qual.getGrade().isBlank()) {
                doc.add(new Paragraph(qual.getGrade()).setMarginTop(5).setMarginBottom(15).setMultipliedLeading(1.2f));
            } else {
                doc.add(new Paragraph().setMarginBottom(15)); // Add space even if no grade
            }
        }
    }

    private PdfFont loadFontFromResource(String resourcePath) throws IOException {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Font resource not found on classpath: " + resourcePath);
            }
            byte[] fontBytes = is.readAllBytes();
            return PdfFontFactory.createFont(fontBytes, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
        }
    }
}