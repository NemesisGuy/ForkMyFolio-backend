package com.forkmyfolio.service;

import com.forkmyfolio.model.*;
import com.forkmyfolio.repository.ExperienceRepository;
import com.forkmyfolio.repository.ProjectRepository;
import com.forkmyfolio.repository.QualificationRepository;
import com.forkmyfolio.repository.SkillRepository;
import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.image.ImageDataFactory;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfGenerationService {

    private static final Logger log = LoggerFactory.getLogger(PdfGenerationService.class);

    private final PortfolioProfileService portfolioProfileService;
    private final ExperienceRepository experienceRepository;
    private final ProjectRepository projectRepository;
    private final SkillRepository skillRepository;
    private final QualificationRepository qualificationRepository;

    // Theme Colors
    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(34, 49, 63);
    private static final DeviceRgb SECONDARY_COLOR = new DeviceRgb(128, 128, 128);
    private static final DeviceRgb ACCENT_COLOR = new DeviceRgb(52, 152, 219);
    private static final DeviceRgb SIDEBAR_BG = new DeviceRgb(245, 245, 245);
    private static final DeviceRgb SIDEBAR_TEXT = new DeviceRgb(51, 51, 51);

    // --- NEW: Skill Level Colors ---
    private static final DeviceRgb SKILL_EXPERT_COLOR = new DeviceRgb(40, 167, 69);      // #28A745 (Green)
    private static final DeviceRgb SKILL_INTERMEDIATE_COLOR = new DeviceRgb(0, 123, 255); // #007BFF (Blue)
    private static final DeviceRgb SKILL_BEGINNER_COLOR = new DeviceRgb(255, 193, 7);     // #FFC107 (Amber)

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");

    private static class PdfContext {
        final PdfDocument pdfDocument;
        final PdfFont nameFont, headlineFont, sectionHeaderFont, itemTitleFont, itemSubtitleFont, bodyFont, dateFont, solidIconFont, brandsIconFont;

        PdfContext(PdfDocument pdfDocument) throws IOException {
            this.pdfDocument = pdfDocument;

            log.info("Initializing fonts for PDF generation");
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
        log.info("Attempting to load font from resource: {}", resourcePath);
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                log.error("Font resource not found on classpath: '{}'", resourcePath);
                return null;
            }
            byte[] fontBytes = is.readAllBytes();
            return PdfFontFactory.createFont(fontBytes, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
        } catch (IOException e) {
            log.error("Failed to load font resource '{}': {}", resourcePath, e.getMessage(), e);
            return null;
        }
    }

    public PdfFile generatePortfolioPdf() {
        log.info("Starting stylish PDF generation process...");
        PortfolioProfile profile = portfolioProfileService.getPublicProfile();
        User user = profile.getUser();
        log.info("Generating PDF for user: {}", user.getEmail());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(0, 0, 0, 0);

        try {
            PdfContext ctx = new PdfContext(pdf);

            Table bannerTable = new Table(UnitValue.createPercentArray(new float[]{1})).useAllAvailableWidth();
            Cell bannerCell = new Cell()
                    .add(new Paragraph(user.getFirstName() + " " + user.getLastName())
                            .setFont(ctx.nameFont).setFontSize(24).setFontColor(ColorConstants.WHITE))
                    .add(new Paragraph(profile.getHeadline())
                            .setFont(ctx.headlineFont).setFontSize(14).setFontColor(ColorConstants.WHITE))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(PRIMARY_COLOR)
                    .setPaddingTop(20).setPaddingBottom(20)
                    .setBorder(Border.NO_BORDER);
            bannerTable.addCell(bannerCell);
            document.add(bannerTable);

            Table mainTable = new Table(UnitValue.createPercentArray(new float[]{1, 2.5f})).useAllAvailableWidth();
            mainTable.addCell(createLeftColumn(profile, ctx));
            mainTable.addCell(createRightColumn(profile, ctx));
            document.add(mainTable);

            // A div with a top border to act as a separator line
            Div footerLine = new Div()
                    .setBorderTop(new SolidBorder(SECONDARY_COLOR, 0.5f))
                    .setMarginTop(15);
            document.add(footerLine);

            // The footer text itself
            Paragraph footerText = new Paragraph("Generated with ForkMyFolio • https://forkmyfolio.nemesisnet.co.za")
                    .setFont(ctx.bodyFont).setFontSize(8)
                    .setFontColor(SECONDARY_COLOR)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPaddingTop(5)
                    .setPaddingBottom(5);
            document.add(footerText);

        } catch (IOException e) {
            log.error("Failed to load fonts for PDF generation. Aborting.", e);
            throw new RuntimeException("Failed to generate PDF due to a font loading issue.", e);
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

    private Cell createLeftColumn(PortfolioProfile profile, PdfContext ctx) {
        Cell cell = new Cell().setBorder(Border.NO_BORDER).setBackgroundColor(SIDEBAR_BG).setPadding(20);

        if (profile.getResumeImageUrl() != null && !profile.getResumeImageUrl().isBlank()) {
            try {
                Image profileImage = new Image(ImageDataFactory.create(profile.getResumeImageUrl()));
                profileImage.setWidth(100).setHeight(100).setAutoScale(false);
                cell.add(new Paragraph().add(profileImage).setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));
            } catch (Exception e) {
                log.warn("Could not load profile image for PDF: {}", e.getMessage());
            }
        }

        addSectionToCell(cell, "Contact", () -> {
            Table contactTable = new Table(UnitValue.createPercentArray(new float[]{1, 10})).useAllAvailableWidth().setBorder(Border.NO_BORDER);

            if (profile.getPublicEmail() != null && !profile.getPublicEmail().isBlank()) {
                addContactRow(contactTable, "email", profile.getPublicEmail(), "mailto:" + profile.getPublicEmail(), ctx);
            }
            if (profile.getWebsiteUrl() != null && !profile.getWebsiteUrl().isBlank()) {
                addContactRow(contactTable, "website", profile.getWebsiteUrl(), profile.getWebsiteUrl(), ctx);
            }
            if (profile.getLinkedinUrl() != null && !profile.getLinkedinUrl().isBlank()) {
                addContactRow(contactTable, "linkedin", profile.getLinkedinUrl(), profile.getLinkedinUrl(), ctx);
            }
            if (profile.getGithubUrl() != null && !profile.getGithubUrl().isBlank()) {
                addContactRow(contactTable, "github", profile.getGithubUrl(), profile.getGithubUrl(), ctx);
            }

            if (profile.getWebsiteUrl() != null && !profile.getWebsiteUrl().isBlank()) {
                BarcodeQRCode qrCode = new BarcodeQRCode(profile.getWebsiteUrl());
                Image qrImage = new Image(qrCode.createFormXObject(SIDEBAR_TEXT, ctx.pdfDocument))
                        .setWidth(80).setHeight(80).setMarginTop(15);
                Cell qrCell = new Cell(1, 2).add(new Paragraph().add(qrImage).setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER);
                contactTable.addCell(qrCell);
            }

            return contactTable;
        }, ctx);

        addSectionToCell(cell, "Skills", () -> {
            Table skillsTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
            List<Skill> skills = skillRepository.findAll();
            // Sort by level (EXPERT first), then alphabetically
            skills.sort(Comparator.comparing(Skill::getLevel).reversed()
                    .thenComparing(Skill::getName, String.CASE_INSENSITIVE_ORDER));

            for (Skill skill : skills) {
                // Skill name has a consistent color now
                Cell nameCell = new Cell()
                        .add(new Paragraph(skill.getName())
                                .setFont(ctx.bodyFont).setFontSize(9)
                                .setFontColor(SIDEBAR_TEXT).setMargin(0)) // Consistent color
                        .setBorder(Border.NO_BORDER)
                        .setPadding(0).setPaddingBottom(5);

                Paragraph ratingParagraph = new Paragraph()
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setMargin(0).setFontSize(10);

                int filledDots = 0;
                // Determine the color for the FILLED dots based on skill level
                DeviceRgb dotColor = SKILL_BEGINNER_COLOR; // Default to beginner color
                if (skill.getLevel() != null) {
                    switch (skill.getLevel()) {
                        case BEGINNER:
                            filledDots = 1;
                            dotColor = SKILL_BEGINNER_COLOR;
                            break;
                        case INTERMEDIATE:
                            filledDots = 3;
                            dotColor = SKILL_INTERMEDIATE_COLOR;
                            break;
                        case EXPERT:
                            filledDots = 5;
                            dotColor = SKILL_EXPERT_COLOR;
                            break;
                    }
                }

                if (ctx.solidIconFont != null) {
                    String circleIcon = "\u25CF"; // Solid Circle Unicode
                    for (int i = 0; i < 5; i++) {
                        Text dot = new Text(circleIcon).setFont(ctx.solidIconFont);
                        if (i < filledDots) {
                            dot.setFontColor(dotColor); // Apply level-specific color to filled dots
                        } else {
                            dot.setFontColor(new DeviceRgb(224, 224, 224)); // A light gray for empty
                        }
                        ratingParagraph.add(dot);
                    }
                }

                Cell ratingCell = new Cell().add(ratingParagraph)
                        .setBorder(Border.NO_BORDER)
                        .setPadding(0).setPaddingBottom(5);

                skillsTable.addCell(nameCell);
                skillsTable.addCell(ratingCell);
            }
            return skillsTable;
        }, ctx);

        return cell;
    }

    private void addContactRow(Table table, String type, String text, String reference, PdfContext ctx) {
        String iconChar;
        PdfFont iconFont;

        switch (type.toLowerCase()) {
            case "email":
                iconChar = "\uf0e0"; iconFont = ctx.solidIconFont; break;
            case "website":
                iconChar = "\uf0c1"; iconFont = ctx.solidIconFont; break;
            case "linkedin":
                iconChar = "\uf08c"; iconFont = ctx.brandsIconFont; break;
            case "github":
                iconChar = "\uf09b"; iconFont = ctx.brandsIconFont; break;
            default:
                iconChar = "[?]"; iconFont = ctx.bodyFont; break;
        }

        Cell iconCell = new Cell().setBorder(Border.NO_BORDER).setPadding(0).setWidth(UnitValue.createPercentValue(5));
        if (iconFont != null && iconFont.containsGlyph(iconChar.codePointAt(0))) {
            iconCell.add(new Paragraph(iconChar + " ").setFont(iconFont).setFontSize(12).setFontColor(SIDEBAR_TEXT));
        }

        Cell textCell = new Cell().setBorder(Border.NO_BORDER).setPadding(0).setWidth(UnitValue.createPercentValue(95));
        Text link = new Text(text).setFont(ctx.bodyFont).setFontSize(9).setFontColor(ACCENT_COLOR);
        if (reference != null) {
            link.setAction(PdfAction.createURI(reference)).setUnderline();
        }
        Paragraph p = new Paragraph().add(link).setMultipliedLeading(1.2f);
        textCell.add(p);

        table.addCell(iconCell);
        table.addCell(textCell);
    }

    private Cell createRightColumn(PortfolioProfile profile, PdfContext ctx) {
        Cell cell = new Cell().setBorder(Border.NO_BORDER).setPadding(20);

        if (profile.getSummary() != null && !profile.getSummary().isBlank()) {
            addSectionToCell(cell, "Summary", () -> createFormattedParagraph(profile.getSummary(), ctx), ctx);
        }

        addSectionToCell(cell, "Qualifications", () -> {
            Table table = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
            List<Qualification> qualifications = qualificationRepository.findAll();
            qualifications.sort(Comparator.comparing(Qualification::getCompletionYear).reversed());
            qualifications.forEach(qual -> table.addCell(createQualificationCell(qual, ctx)));
            return table;
        }, ctx);

        addSectionToCell(cell, "Professional Experience", () -> {
            Table table = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
            experienceRepository.findAllByOrderByStartDateDesc()
                    .forEach(exp -> table.addCell(createExperienceCell(exp, ctx)));
            return table;
        }, ctx);

        addSectionToCell(cell, "Projects", () -> {
            Table table = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
            projectRepository.findAll().forEach(proj -> table.addCell(createProjectCell(proj, ctx)));
            return table;
        }, ctx);

        return cell;
    }

    private void addSectionToCell(Cell cell, String title, SectionContentProvider provider, PdfContext ctx) {
        Paragraph heading = new Paragraph(title.toUpperCase())
                .setFont(ctx.sectionHeaderFont)
                .setFontSize(14)
                .setFontColor(ACCENT_COLOR)
                .setMarginBottom(5)
                .setBorderBottom(new SolidBorder(ACCENT_COLOR, 1));

        if (!cell.getChildren().isEmpty()) {
            heading.setMarginTop(20);
        }
        cell.add(heading);

        BlockElement content = provider.getContent();
        content.setMarginTop(10);
        cell.add(content);
    }

    private Paragraph createFormattedParagraph(String text, PdfContext ctx) {
        if (text == null || text.isBlank()) return new Paragraph();
        Paragraph container = new Paragraph().setMarginBottom(0).setPaddingBottom(0);
        com.itextpdf.layout.element.List currentList = null;

        for (String line : text.split("\n")) {
            if (line.trim().startsWith("*") || line.trim().startsWith("-")) {
                if (currentList == null) {
                    currentList = new com.itextpdf.layout.element.List()
                            .setListSymbol("• ")
                            .setFont(ctx.bodyFont).setFontSize(10).setFontColor(PRIMARY_COLOR).setMarginLeft(10);
                    container.add(currentList);
                }
                ListItem listItem = new ListItem(line.trim().substring(1).trim());
                listItem.setFont(ctx.bodyFont).setFontSize(10).setFontColor(PRIMARY_COLOR);
                currentList.add(listItem);
            } else {
                currentList = null;
                container.add(new Paragraph(line).setFont(ctx.bodyFont).setFontSize(10).setFontColor(PRIMARY_COLOR));
            }
        }
        return container;
    }

    private Cell createQualificationCell(Qualification qual, PdfContext ctx) {
        Cell cell = new Cell().setBorder(Border.NO_BORDER).setPaddingBottom(15);
        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1})).useAllAvailableWidth();

        table.addCell(new Cell().setBorder(Border.NO_BORDER)
                .add(new Paragraph(qual.getQualificationName())
                        .setFont(ctx.itemTitleFont).setFontSize(12).setFontColor(PRIMARY_COLOR)));

        table.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph(String.valueOf(qual.getCompletionYear()))
                        .setFont(ctx.dateFont).setFontSize(9).setFontColor(SECONDARY_COLOR)));

        cell.add(table);

        cell.add(new Paragraph(qual.getInstitutionName())
                .setFont(ctx.itemSubtitleFont).setFontSize(10).setFontColor(SECONDARY_COLOR).setMarginBottom(2));

        if (qual.getGrade() != null && !qual.getGrade().isBlank()) {
            cell.add(new Paragraph(qual.getGrade())
                    .setFont(ctx.bodyFont).setFontSize(9).setFontColor(SECONDARY_COLOR));
        }

        return cell;
    }

    private Cell createExperienceCell(Experience exp, PdfContext ctx) {
        Cell cell = new Cell().setBorder(Border.NO_BORDER).setPaddingBottom(15);
        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1})).useAllAvailableWidth();

        table.addCell(new Cell().setBorder(Border.NO_BORDER)
                .add(new Paragraph(exp.getJobTitle()).setFont(ctx.itemTitleFont).setFontSize(12).setFontColor(PRIMARY_COLOR)));

        String dateText = exp.getStartDate().format(DATE_FORMATTER) + " - " +
                (exp.getEndDate() == null ? "Present" : exp.getEndDate().format(DATE_FORMATTER));

        table.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph(dateText).setFont(ctx.dateFont).setFontSize(9).setFontColor(SECONDARY_COLOR)));

        cell.add(table);

        String company = exp.getCompanyName() + (exp.getLocation() != null ? ", " + exp.getLocation() : "");
        cell.add(new Paragraph(company).setFont(ctx.itemSubtitleFont).setFontSize(10).setFontColor(SECONDARY_COLOR).setMarginBottom(5));

        cell.add(createFormattedParagraph(exp.getDescription(), ctx));
        return cell;
    }

    private Cell createProjectCell(Project proj, PdfContext ctx) {
        Cell cell = new Cell().setBorder(Border.NO_BORDER).setPaddingBottom(15);
        cell.add(new Paragraph(proj.getTitle()).setFont(ctx.itemTitleFont).setFontSize(12).setFontColor(PRIMARY_COLOR));
        cell.add(createFormattedParagraph(proj.getDescription(), ctx));
        return cell;
    }

    @FunctionalInterface
    private interface SectionContentProvider {
        BlockElement getContent();
    }

    public record PdfFile(byte[] content, String suggestedFilename) {}
}