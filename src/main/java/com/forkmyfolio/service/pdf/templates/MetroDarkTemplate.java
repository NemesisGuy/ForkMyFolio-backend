package com.forkmyfolio.service.pdf.templates;

import com.forkmyfolio.model.*;
import com.forkmyfolio.service.impl.PdfGenerationService;
import com.forkmyfolio.service.pdf.PortfolioData;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;

import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.List;

public class MetroDarkTemplate implements PortfolioPdfTemplate {

    // --- ENHANCED Dark Mode Palette ---
    private static final Color BG_COLOR = new DeviceRgb(20, 20, 25);         // #141419 (Deep Charcoal)
    private static final Color TEXT_COLOR = new DeviceRgb(235, 235, 235);   // #EBEBEB (Bright Off-white)
    private static final Color ACCENT_COLOR = new DeviceRgb(0, 150, 255);   // #0096FF (Vibrant Blue)
    private static final Color SUBTLE_COLOR = new DeviceRgb(160, 160, 160); // #A0A0A0 (Medium Gray)

    private PdfFont headingFont;
    private PdfFont bodyFont;
    private PdfFont bodyItalicFont;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");

    @Override
    public void generate(Document document, PdfGenerationService.PdfContext ctx, PortfolioData data) throws IOException {
        initializeTemplateResources();
        document.setBackgroundColor(BG_COLOR);
        document.setFontColor(TEXT_COLOR);
        document.setFont(bodyFont);
        document.setMargins(40, 40, 40, 40);

        User user = data.profile().getUser();

        // --- Header ---
        document.add(new Paragraph(user.getFirstName().toUpperCase() + " " + user.getLastName().toUpperCase())
                .setFont(headingFont).setFontSize(36).setTextAlignment(TextAlignment.CENTER).setMarginBottom(0));
        document.add(new Paragraph(data.profile().getHeadline())
                .setFont(bodyItalicFont).setFontSize(14).setFontColor(ACCENT_COLOR).setTextAlignment(TextAlignment.CENTER).setMarginBottom(10));

        // --- IMPROVED: Clickable and robust contact info ---
        createContactInfo(document, data.profile());

        // --- Body ---
        createSection(document, "Profile", data.profile().getSummary());
        createExperienceSection(document, data.experiences());
        createProjectsSection(document, data.projects());
        createQualificationsSection(document, data.qualifications());
    }

    private void createContactInfo(Document doc, PortfolioProfile profile) {
        Paragraph contactParagraph = new Paragraph().setTextAlignment(TextAlignment.CENTER).setFontSize(10).setMarginBottom(20);
        List<Text> contactParts = new ArrayList<>();

        if (profile.getPublicEmail() != null && !profile.getPublicEmail().isBlank()) {
            Text emailText = new Text(profile.getPublicEmail())
                    .setAction(PdfAction.createURI("mailto:" + profile.getPublicEmail()))
                    .setFontColor(SUBTLE_COLOR)
                    .setUnderline();
            contactParts.add(emailText);
        }

        if (profile.getWebsiteUrl() != null && !profile.getWebsiteUrl().isBlank()) {
            Text websiteText = new Text(profile.getWebsiteUrl().replaceFirst("^(https?://)", ""))
                    .setAction(PdfAction.createURI(profile.getWebsiteUrl()))
                    .setFontColor(SUBTLE_COLOR)
                    .setUnderline();
            contactParts.add(websiteText);
        }

        for (int i = 0; i < contactParts.size(); i++) {
            contactParagraph.add(contactParts.get(i));
            if (i < contactParts.size() - 1) {
                contactParagraph.add(new Text("  •  ").setFontColor(SUBTLE_COLOR));
            }
        }

        if (!contactParts.isEmpty()) {
            doc.add(contactParagraph);
        }
    }

    private void createSection(Document doc, String title, String content) {
        if (content == null || content.isBlank()) return;
        doc.add(new Paragraph(title.toUpperCase())
                .setFont(headingFont).setFontSize(12).setFontColor(ACCENT_COLOR)
                .setMarginTop(15).setMarginBottom(5));
        doc.add(new Div().setHeight(1).setBackgroundColor(ACCENT_COLOR).setMarginBottom(10));
        doc.add(new Paragraph(content).setMultipliedLeading(1.3f));
    }

    private void createSectionHeader(Document doc, String title) {
        doc.add(new Paragraph(title.toUpperCase())
                .setFont(headingFont).setFontSize(12).setFontColor(ACCENT_COLOR)
                .setMarginTop(15).setMarginBottom(5));
        doc.add(new Div().setHeight(1).setBackgroundColor(ACCENT_COLOR).setMarginBottom(10));
    }

    private void createExperienceSection(Document doc, java.util.List<Experience> experiences) {
        if (experiences.isEmpty()) return;
        createSectionHeader(doc, "Experience");
        experiences.sort(Comparator.comparing(Experience::getStartDate).reversed());
        for (Experience exp : experiences) {
            String dateText = exp.getStartDate().format(DATE_FORMATTER) + " - " + (exp.getEndDate() == null ? "Present" : exp.getEndDate().format(DATE_FORMATTER));
            doc.add(new Paragraph()
                    .add(new Text(exp.getJobTitle()).setBold())
                    .add(" at ")
                    .add(new Text(exp.getCompanyName()).setBold())
                    .add(new Text("  |  " + dateText).setFontColor(SUBTLE_COLOR).setFontSize(9))
                    .setMarginBottom(0));
            doc.add(new Paragraph(exp.getDescription()).setMarginLeft(15).setMarginTop(5).setMarginBottom(15).setMultipliedLeading(1.2f));
        }
    }

    private void createProjectsSection(Document doc, java.util.List<Project> projects) {
        if (projects.isEmpty()) return;
        createSectionHeader(doc, "Projects");
        for (Project proj : projects) {
            // Determine the primary URL for the project link
            String projectUrl = null;
            if (proj.getLiveUrl() != null && !proj.getLiveUrl().isBlank()) {
                projectUrl = proj.getLiveUrl();
            } else if (proj.getRepoUrl() != null && !proj.getRepoUrl().isBlank()) {
                projectUrl = proj.getRepoUrl();
            }

            Text titleText = new Text(proj.getTitle()).setBold();
            if (projectUrl != null) {
                titleText.setFontColor(ACCENT_COLOR)
                        .setUnderline()
                        .setAction(PdfAction.createURI(projectUrl));
            }

            Paragraph titleLine = new Paragraph()
                    .add(titleText)
                    .add(new Text("  |  " + proj.getSkills().stream().map(Skill::getName).collect(Collectors.joining(", "))).setFontColor(SUBTLE_COLOR).setFontSize(9))
                    .setMarginBottom(0);

            doc.add(titleLine);
            doc.add(new Paragraph(proj.getDescription()).setMarginLeft(15).setMarginTop(5).setMarginBottom(15).setMultipliedLeading(1.2f));
        }
    }

    private void createQualificationsSection(Document doc, java.util.List<Qualification> qualifications) {
        if (qualifications.isEmpty()) return;
        createSectionHeader(doc, "Education");
        qualifications.sort(Comparator.comparing(Qualification::getCompletionYear).reversed());
        for (Qualification qual : qualifications) {
            doc.add(new Paragraph()
                    .add(new Text(qual.getQualificationName()).setBold())
                    .add(new Text("  |  " + qual.getCompletionYear()).setFontColor(SUBTLE_COLOR).setFontSize(9))
                    .setMarginBottom(0));
            doc.add(new Paragraph(qual.getInstitutionName()).setItalic().setFontSize(10).setMarginLeft(15).setMarginTop(0).setMarginBottom(10));
        }
    }

    private void initializeTemplateResources() throws IOException {
        this.headingFont = loadFontFromResource("fonts/Montserrat-Bold.ttf");
        this.bodyFont = loadFontFromResource("fonts/Lato-Regular.ttf");
        this.bodyItalicFont = loadFontFromResource("fonts/Lato-Italic.ttf");
    }

    private PdfFont loadFontFromResource(String resourcePath) throws IOException {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) throw new IOException("Font resource not found: " + resourcePath);
            return PdfFontFactory.createFont(is.readAllBytes(), PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
        }
    }
}