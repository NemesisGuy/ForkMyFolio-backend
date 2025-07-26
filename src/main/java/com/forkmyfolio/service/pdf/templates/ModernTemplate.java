package com.forkmyfolio.service.pdf.templates;

import com.forkmyfolio.model.*;
import com.forkmyfolio.service.PdfGenerationService;
import com.forkmyfolio.service.pdf.PortfolioData;
import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

/**
 * The "Modern" PDF template implementation. Contains all layout logic for this specific design.
 */
public class ModernTemplate implements PortfolioPdfTemplate {

    // Pull in color constants from the service
    private static final DeviceRgb PRIMARY_COLOR = PdfGenerationService.PRIMARY_COLOR;
    private static final DeviceRgb SECONDARY_COLOR = PdfGenerationService.SECONDARY_COLOR;
    private static final DeviceRgb ACCENT_COLOR = PdfGenerationService.ACCENT_COLOR;
    private static final DeviceRgb SIDEBAR_BG = PdfGenerationService.SIDEBAR_BG;
    private static final DeviceRgb SIDEBAR_TEXT = PdfGenerationService.SIDEBAR_TEXT;
    private static final DeviceRgb SKILL_EXPERT_COLOR = PdfGenerationService.SKILL_EXPERT_COLOR;
    private static final DeviceRgb SKILL_INTERMEDIATE_COLOR = PdfGenerationService.SKILL_INTERMEDIATE_COLOR;
    private static final DeviceRgb SKILL_BEGINNER_COLOR = PdfGenerationService.SKILL_BEGINNER_COLOR;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");

    @Override
    public void generate(Document document, PdfGenerationService.PdfContext ctx, PortfolioData data) throws IOException {
        User user = data.profile().getUser();

        // Banner
        Table bannerTable = new Table(UnitValue.createPercentArray(new float[]{1})).useAllAvailableWidth();
        Cell bannerCell = new Cell()
                .add(new Paragraph(user.getFirstName() + " " + user.getLastName())
                        .setFont(ctx.nameFont).setFontSize(24).setFontColor(ColorConstants.WHITE))
                .add(new Paragraph(data.profile().getHeadline())
                        .setFont(ctx.headlineFont).setFontSize(14).setFontColor(ColorConstants.WHITE))
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(PRIMARY_COLOR)
                .setPaddingTop(20).setPaddingBottom(20)
                .setBorder(Border.NO_BORDER);
        bannerTable.addCell(bannerCell);
        document.add(bannerTable);

        // Main content table
        Table mainTable = new Table(UnitValue.createPercentArray(new float[]{1, 2.5f})).useAllAvailableWidth();
        mainTable.addCell(createLeftColumn(data, ctx));
        mainTable.addCell(createRightColumn(data, ctx));
        document.add(mainTable);

        // Footer
        Div footerLine = new Div().setBorderTop(new SolidBorder(SECONDARY_COLOR, 0.5f)).setMarginTop(15);
        document.add(footerLine);
        Paragraph footerText = new Paragraph("Generated with ForkMyFolio • https://forkmyfolio.nemesisnet.co.za")
                .setFont(ctx.bodyFont).setFontSize(8)
                .setFontColor(SECONDARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setPaddingTop(5)
                .setPaddingBottom(5);
        document.add(footerText);
    }

    private Cell createLeftColumn(PortfolioData data, PdfGenerationService.PdfContext ctx) {
        Cell cell = new Cell().setBorder(Border.NO_BORDER).setBackgroundColor(SIDEBAR_BG).setPadding(20);
        PortfolioProfile profile = data.profile();

        if (profile.getResumeImageUrl() != null && !profile.getResumeImageUrl().isBlank()) {
            try {
                Image profileImage = new Image(ImageDataFactory.create(profile.getResumeImageUrl()));
                profileImage.setWidth(100).setHeight(100).setAutoScale(false);
                cell.add(new Paragraph().add(profileImage).setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));
            } catch (Exception e) {
                // In a real app, you might pass a logger into the template
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
            data.skills().sort(Comparator.comparing(Skill::getLevel).reversed().thenComparing(Skill::getName, String.CASE_INSENSITIVE_ORDER));
            for (Skill skill : data.skills()) {
                Cell nameCell = new Cell().add(new Paragraph(skill.getName()).setFont(ctx.bodyFont).setFontSize(9).setFontColor(SIDEBAR_TEXT).setMargin(0)).setBorder(Border.NO_BORDER).setPadding(0).setPaddingBottom(5);
                Paragraph ratingParagraph = new Paragraph().setTextAlignment(TextAlignment.RIGHT).setMargin(0).setFontSize(10);
                int filledDots = 0;
                DeviceRgb dotColor = SKILL_BEGINNER_COLOR;
                if (skill.getLevel() != null) {
                    switch (skill.getLevel()) {
                        case BEGINNER -> { filledDots = 1; dotColor = SKILL_BEGINNER_COLOR; }
                        case INTERMEDIATE -> { filledDots = 3; dotColor = SKILL_INTERMEDIATE_COLOR; }
                        case EXPERT -> { filledDots = 5; dotColor = SKILL_EXPERT_COLOR; }
                    }
                }
                if (ctx.solidIconFont != null) {
                    String circleIcon = "\u25CF";
                    for (int i = 0; i < 5; i++) {
                        Text dot = new Text(circleIcon).setFont(ctx.solidIconFont);
                        if (i < filledDots) {
                            dot.setFontColor(dotColor);
                        } else {
                            dot.setFontColor(new DeviceRgb(224, 224, 224));
                        }
                        ratingParagraph.add(dot);
                    }
                }
                Cell ratingCell = new Cell().add(ratingParagraph).setBorder(Border.NO_BORDER).setPadding(0).setPaddingBottom(5);
                skillsTable.addCell(nameCell);
                skillsTable.addCell(ratingCell);
            }
            return skillsTable;
        }, ctx);

        return cell;
    }

    private Cell createRightColumn(PortfolioData data, PdfGenerationService.PdfContext ctx) {
        Cell cell = new Cell().setBorder(Border.NO_BORDER).setPadding(20);
        if (data.profile().getSummary() != null && !data.profile().getSummary().isBlank()) {
            addSectionToCell(cell, "Summary", () -> createFormattedParagraph(data.profile().getSummary(), ctx), ctx);
        }
        addSectionToCell(cell, "Qualifications", () -> {
            Table table = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
            data.qualifications().sort(Comparator.comparing(Qualification::getCompletionYear).reversed());
            data.qualifications().forEach(qual -> table.addCell(createQualificationCell(qual, ctx)));
            return table;
        }, ctx);
        addSectionToCell(cell, "Professional Experience", () -> {
            Table table = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
            data.experiences().sort(Comparator.comparing(Experience::getStartDate).reversed());
            data.experiences().forEach(exp -> table.addCell(createExperienceCell(exp, ctx)));
            return table;
        }, ctx);
        addSectionToCell(cell, "Projects", () -> {
            Table table = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
            data.projects().forEach(proj -> table.addCell(createProjectCell(proj, ctx)));
            return table;
        }, ctx);
        return cell;
    }

    private void addSectionToCell(Cell cell, String title, SectionContentProvider provider, PdfGenerationService.PdfContext ctx) {
        Paragraph heading = new Paragraph(title.toUpperCase()).setFont(ctx.sectionHeaderFont).setFontSize(14).setFontColor(ACCENT_COLOR).setMarginBottom(5).setBorderBottom(new SolidBorder(ACCENT_COLOR, 1));
        if (!cell.getChildren().isEmpty()) {
            heading.setMarginTop(20);
        }
        cell.add(heading);
        BlockElement<?> content = provider.getContent();
        content.setMarginTop(10);
        cell.add(content);
    }

    private void addContactRow(Table table, String type, String text, String reference, PdfGenerationService.PdfContext ctx) {
        String iconChar;
        PdfFont iconFont;
        switch (type.toLowerCase()) {
            case "email" -> { iconChar = "\uf0e0"; iconFont = ctx.solidIconFont; }
            case "website" -> { iconChar = "\uf0c1"; iconFont = ctx.solidIconFont; }
            case "linkedin" -> { iconChar = "\uf08c"; iconFont = ctx.brandsIconFont; }
            case "github" -> { iconChar = "\uf09b"; iconFont = ctx.brandsIconFont; }
            default -> { iconChar = "[?]"; iconFont = ctx.bodyFont; }
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

    private Paragraph createFormattedParagraph(String text, PdfGenerationService.PdfContext ctx) {
        if (text == null || text.isBlank()) return new Paragraph();
        Paragraph container = new Paragraph().setMarginBottom(0).setPaddingBottom(0);
        com.itextpdf.layout.element.List currentList = null;
        for (String line : text.split("\n")) {
            if (line.trim().startsWith("*") || line.trim().startsWith("-")) {
                if (currentList == null) {
                    currentList = new com.itextpdf.layout.element.List().setListSymbol("• ").setFont(ctx.bodyFont).setFontSize(10).setFontColor(PRIMARY_COLOR).setMarginLeft(10);
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

    private Cell createQualificationCell(Qualification qual, PdfGenerationService.PdfContext ctx) {
        Cell cell = new Cell().setBorder(Border.NO_BORDER).setPaddingBottom(15);
        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1})).useAllAvailableWidth();
        table.addCell(new Cell().setBorder(Border.NO_BORDER).add(new Paragraph(qual.getQualificationName()).setFont(ctx.itemTitleFont).setFontSize(12).setFontColor(PRIMARY_COLOR)));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT).add(new Paragraph(String.valueOf(qual.getCompletionYear())).setFont(ctx.dateFont).setFontSize(9).setFontColor(SECONDARY_COLOR)));
        cell.add(table);
        cell.add(new Paragraph(qual.getInstitutionName()).setFont(ctx.itemSubtitleFont).setFontSize(10).setFontColor(SECONDARY_COLOR).setMarginBottom(2));
        if (qual.getGrade() != null && !qual.getGrade().isBlank()) {
            cell.add(new Paragraph(qual.getGrade()).setFont(ctx.bodyFont).setFontSize(9).setFontColor(SECONDARY_COLOR));
        }
        return cell;
    }

    private Cell createExperienceCell(Experience exp, PdfGenerationService.PdfContext ctx) {
        Cell cell = new Cell().setBorder(Border.NO_BORDER).setPaddingBottom(15);
        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1})).useAllAvailableWidth();
        table.addCell(new Cell().setBorder(Border.NO_BORDER).add(new Paragraph(exp.getJobTitle()).setFont(ctx.itemTitleFont).setFontSize(12).setFontColor(PRIMARY_COLOR)));
        String dateText = exp.getStartDate().format(DATE_FORMATTER) + " - " + (exp.getEndDate() == null ? "Present" : exp.getEndDate().format(DATE_FORMATTER));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT).add(new Paragraph(dateText).setFont(ctx.dateFont).setFontSize(9).setFontColor(SECONDARY_COLOR)));
        cell.add(table);
        String company = exp.getCompanyName() + (exp.getLocation() != null ? ", " + exp.getLocation() : "");
        cell.add(new Paragraph(company).setFont(ctx.itemSubtitleFont).setFontSize(10).setFontColor(SECONDARY_COLOR).setMarginBottom(5));
        cell.add(createFormattedParagraph(exp.getDescription(), ctx));
        return cell;
    }

    private Cell createProjectCell(Project proj, PdfGenerationService.PdfContext ctx) {
        Cell cell = new Cell().setBorder(Border.NO_BORDER).setPaddingBottom(15);
        String projectUrl = null;
        if (proj.getLiveUrl() != null && !proj.getLiveUrl().isBlank()) {
            projectUrl = proj.getLiveUrl();
        } else if (proj.getRepoUrl() != null && !proj.getRepoUrl().isBlank()) {
            projectUrl = proj.getRepoUrl();
        }
        Text titleText = new Text(proj.getTitle()).setFont(ctx.itemTitleFont).setFontSize(12);
        if (projectUrl != null) {
            titleText.setFontColor(ACCENT_COLOR).setUnderline().setAction(PdfAction.createURI(projectUrl));
        } else {
            titleText.setFontColor(PRIMARY_COLOR);
        }
        Paragraph titleParagraph = new Paragraph(titleText);
        cell.add(titleParagraph);
        if (proj.getTechStack() != null && !proj.getTechStack().isEmpty()) {
            titleParagraph.setMarginBottom(2);
            String techStackString = String.join(" • ", proj.getTechStack());
            Paragraph techStackParagraph = new Paragraph(techStackString).setFont(ctx.itemSubtitleFont).setFontSize(9).setFontColor(SECONDARY_COLOR).setMarginBottom(5);
            cell.add(techStackParagraph);
        } else {
            titleParagraph.setMarginBottom(5);
        }
        cell.add(createFormattedParagraph(proj.getDescription(), ctx));
        return cell;
    }

    // This functional interface is specific to how the ModernTemplate builds sections.
    @FunctionalInterface
    private interface SectionContentProvider {
        BlockElement<?> getContent();
    }
}