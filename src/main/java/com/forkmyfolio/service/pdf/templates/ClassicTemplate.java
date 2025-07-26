package com.forkmyfolio.service.pdf.templates;

import com.forkmyfolio.model.Experience;
import com.forkmyfolio.model.Project;
import com.forkmyfolio.model.Qualification;
import com.forkmyfolio.model.User;
import com.forkmyfolio.service.PdfGenerationService;
import com.forkmyfolio.service.pdf.PortfolioData;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * A classic, professional, single-column resume template using serif fonts.
 */
public class ClassicTemplate implements PortfolioPdfTemplate {

    private PdfFont headingFont;
    private PdfFont bodyFont;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy");

    @Override
    public void generate(Document document, PdfGenerationService.PdfContext ctx, PortfolioData data) throws IOException {
        this.headingFont = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
        this.bodyFont = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);

        document.setFont(bodyFont).setFontColor(ColorConstants.BLACK);
        document.setMargins(50, 50, 50, 50);

        // 1. Header
        User user = data.profile().getUser();
        document.add(new Paragraph(user.getFirstName() + " " + user.getLastName())
                .setFont(headingFont).setFontSize(24).setTextAlignment(TextAlignment.CENTER).setMarginBottom(2));

        String contactInfo = String.join(" | ",
                data.profile().getPublicEmail(),
                data.profile().getLinkedinUrl(),
                data.profile().getGithubUrl()
        );
        document.add(new Paragraph(contactInfo)
                .setFontSize(10).setTextAlignment(TextAlignment.CENTER));

        // 2. Summary
        createSection(document, "Summary");
        document.add(new Paragraph(data.profile().getSummary()).setMarginBottom(10));

        // 3. Experience
        createSection(document, "Experience");
        data.experiences().sort(Comparator.comparing(Experience::getStartDate).reversed());
        for (Experience exp : data.experiences()) {
            String dateStr = exp.getStartDate().format(DATE_FORMATTER) + " – " +
                    (exp.getEndDate() == null ? "Present" : exp.getEndDate().format(DATE_FORMATTER));
            createEntry(document, exp.getJobTitle(), dateStr, exp.getCompanyName() + ", " + exp.getLocation(), exp.getDescription());
        }

        // 4. Projects
        createSection(document, "Projects");
        for (Project proj : data.projects()) {
            createEntry(document, proj.getTitle(), proj.getTechStack().stream().collect(Collectors.joining(", ")), null, proj.getDescription());
        }

        // 5. Education
        createSection(document, "Education");
        data.qualifications().sort(Comparator.comparing(Qualification::getCompletionYear).reversed());
        for (Qualification qual : data.qualifications()) {
            createEntry(document, qual.getQualificationName(), String.valueOf(qual.getCompletionYear()), qual.getInstitutionName(), qual.getGrade());
        }
    }

    private void createSection(Document doc, String title) {
        doc.add(new Div()
                .setBorderTop(new SolidBorder(ColorConstants.BLACK, 0.5f))
                .setMarginTop(10).setMarginBottom(5));
        doc.add(new Paragraph(title.toUpperCase())
                .setFont(headingFont).setFontSize(12));
    }



    private void createEntry(Document doc, String title, String date, String subtitle, String description) {
        com.itextpdf.layout.element.Table table = new com.itextpdf.layout.element.Table(new float[]{70, 30});
        table.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));
        table.addCell(new Paragraph(title).setBold().setMargin(0).setPadding(0).setBorder(Border.NO_BORDER));
        table.addCell(new Paragraph(date).setTextAlignment(TextAlignment.RIGHT).setMargin(0).setPadding(0).setBorder(Border.NO_BORDER));

        doc.add(table.setMarginBottom(0));

        if (subtitle != null && !subtitle.isBlank()) {
            doc.add(new Paragraph(subtitle).setItalic().setFontSize(10).setMarginTop(0).setMarginBottom(5));
        }

        if (description != null && !description.isBlank()) {
            doc.add(new Paragraph(description).setFontSize(10).setMarginLeft(15).setMarginBottom(10));
        }
    }
}