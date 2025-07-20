package com.forkmyfolio.service;

import com.forkmyfolio.model.Experience;
import com.forkmyfolio.model.PortfolioProfile;
import com.forkmyfolio.model.Project;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.ExperienceRepository;
import com.forkmyfolio.repository.ProjectRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service responsible for generating a portfolio PDF from user data.
 */
@Service
@RequiredArgsConstructor
public class PdfGenerationService {

    private static final Logger log = LoggerFactory.getLogger(PdfGenerationService.class);
    // --- PDF Font and Style Definitions ---
    private static final Font NAME_FONT = new Font(Font.HELVETICA, 24, Font.BOLD);
    private static final Font CONTACT_FONT = new Font(Font.HELVETICA, 10, Font.ITALIC, Color.GRAY);
    private static final Font SECTION_HEADER_FONT = new Font(Font.HELVETICA, 16, Font.BOLD, new Color(0, 51, 102));
    private static final Font JOB_TITLE_FONT = new Font(Font.HELVETICA, 12, Font.BOLD);
    // NOTE: You would also inject SkillRepository, TestimonialRepository, etc. here
    private static final Font COMPANY_FONT = new Font(Font.HELVETICA, 11, Font.ITALIC);
    private static final Font BODY_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");
    // --- Repositories to fetch data ---
    private final PortfolioProfileService portfolioProfileService;
    private final ExperienceRepository experienceRepository;
    private final ProjectRepository projectRepository;

    public PdfFile generatePortfolioPdf() throws DocumentException {
        log.info("Starting PDF generation process...");
        // Use the service layer to get the complete, public-facing profile.
        PortfolioProfile profile = portfolioProfileService.getPublicProfile();
        User user = profile.getUser();
        log.info("Generating PDF for user: {}", user.getEmail());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        log.debug("Initialized PDF document and writer.");
        PdfWriter.getInstance(document, baos);

        document.open();

        addHeader(document, user, profile);
        addSection(document, "Summary", doc -> addSummarySection(doc, profile));
        addSection(document, "Professional Experience", this::addExperienceSection);
        addSection(document, "Projects", this::addProjectsSection);
        // NOTE: You would add more sections here for Skills, Education, etc.

        document.close();
        log.info("PDF generation complete. Final document size: {} bytes.", baos.size());

        String filename = String.format("%s%s-Resume-%s.pdf",
                user.getFirstName(),
                user.getLastName(),
                LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        );

        return new PdfFile(baos.toByteArray(), filename);
    }

    private void addHeader(Document document, User user, PortfolioProfile profile) throws DocumentException {
        try {
            Image profileImage = null;
            if (profile.getResumeImageUrl() != null && !profile.getResumeImageUrl().isBlank()) {
                log.debug("Attempting to fetch profile image from URL: {}", profile.getResumeImageUrl());
                try {
                    profileImage = Image.getInstance(profile.getResumeImageUrl());
                    profileImage.scaleToFit(80, 80); // Scale image to a reasonable size
                } catch (BadElementException | IOException e) {
                    log.warn("Could not fetch or process profile image. Falling back to text-only header.", e);
                    profileImage = null; // Ensure it's null on failure
                }
            }

            // Use a table for layout to place image next to text
            PdfPTable headerTable = (profileImage != null) ? new PdfPTable(2) : new PdfPTable(1);
            headerTable.setWidthPercentage(100);
            // Note: Table borders are controlled by the cells' borders, not the table itself.

            if (profileImage != null) {
                headerTable.setWidths(new float[]{1, 4}); // Image column is 1/5th of the width
                PdfPCell imageCell = new PdfPCell(profileImage, false);
                imageCell.setBorder(Rectangle.NO_BORDER);
                imageCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                headerTable.addCell(imageCell);
            }

            // Cell for Name and Contact Info
            PdfPCell textCell = new PdfPCell();
            textCell.setBorder(Rectangle.NO_BORDER);
            textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            Paragraph name = new Paragraph(user.getFirstName() + " " + user.getLastName(), NAME_FONT);
            String contactInfo = List.of(profile.getPublicEmail(), profile.getWebsiteUrl(), profile.getLinkedinUrl(), profile.getGithubUrl())
                    .stream()
                    .filter(s -> Objects.nonNull(s) && !s.isBlank())
                    .collect(Collectors.joining(" | "));
            Paragraph contact = new Paragraph(contactInfo, CONTACT_FONT);
            textCell.addElement(name);
            textCell.addElement(contact);
            headerTable.addCell(textCell);

            document.add(headerTable);
        } catch (Exception e) {
            log.error("An unexpected error occurred while creating the PDF header.", e);
        }

        // Add a line separator
        document.add(new Paragraph(" ")); // Spacer
        document.add(new Chunk(new com.lowagie.text.pdf.draw.LineSeparator(0.5f, 100, null, Element.ALIGN_CENTER, -2)));
    }

    private void addSection(Document document, String title, SectionContentAdder contentAdder) throws DocumentException {
        document.add(new Paragraph(" ")); // Spacer before section
        Paragraph sectionHeader = new Paragraph(title, SECTION_HEADER_FONT);
        sectionHeader.setSpacingBefore(10);
        sectionHeader.setSpacingAfter(5);
        document.add(sectionHeader);
        contentAdder.addContent(document);
    }

    private void addSummarySection(Document document, PortfolioProfile profile) throws DocumentException {
        if (profile.getSummary() != null && !profile.getSummary().isBlank()) {
            Paragraph summary = new Paragraph(profile.getSummary(), BODY_FONT);
            summary.setFirstLineIndent(15);
            summary.setAlignment(Element.ALIGN_JUSTIFIED);
            document.add(summary);
        }
    }

    private void addExperienceSection(Document document) throws DocumentException {
        List<Experience> experiences = experienceRepository.findAllByOrderByStartDateDesc();
        for (Experience exp : experiences) {
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 1}); // Job title takes 3/4, date takes 1/4
            table.setSpacingAfter(10);

            // Column 1: Job Title and Company
            Paragraph jobTitle = new Paragraph(exp.getJobTitle(), JOB_TITLE_FONT);
            Paragraph company = new Paragraph(exp.getCompanyName() + (exp.getLocation() != null ? ", " + exp.getLocation() : ""), COMPANY_FONT);
            company.setSpacingBefore(2);

            PdfPCell leftCell = new PdfPCell();
            leftCell.addElement(jobTitle);
            leftCell.addElement(company);
            leftCell.setBorder(Rectangle.NO_BORDER);
            table.addCell(leftCell);

            // Column 2: Dates
            String endDate = exp.getEndDate() == null ? "Present" : exp.getEndDate().format(DATE_FORMATTER);
            String dateRange = exp.getStartDate().format(DATE_FORMATTER) + " - " + endDate;
            Paragraph dates = new Paragraph(dateRange, BODY_FONT);
            dates.setAlignment(Element.ALIGN_RIGHT);

            PdfPCell rightCell = new PdfPCell(dates);
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setVerticalAlignment(Element.ALIGN_TOP);
            table.addCell(rightCell);

            document.add(table);

            // Description
            if (exp.getDescription() != null && !exp.getDescription().isBlank()) {
                Paragraph description = new Paragraph(exp.getDescription(), BODY_FONT);
                description.setFirstLineIndent(15);
                description.setAlignment(Element.ALIGN_JUSTIFIED);
                document.add(description);
            }
        }
    }

    private void addProjectsSection(Document document) throws DocumentException {
        List<Project> projects = projectRepository.findAll();
        for (Project proj : projects) {
            Paragraph projectTitle = new Paragraph(proj.getTitle(), JOB_TITLE_FONT);
            projectTitle.setSpacingBefore(5);
            document.add(projectTitle);

            if (proj.getDescription() != null && !proj.getDescription().isBlank()) {
                Paragraph description = new Paragraph(proj.getDescription(), BODY_FONT);
                description.setFirstLineIndent(15);
                description.setAlignment(Element.ALIGN_JUSTIFIED);
                document.add(description);
            }
        }
    }

    /**
     * A functional interface to allow passing different section-building methods.
     */
    @FunctionalInterface
    private interface SectionContentAdder {
        void addContent(Document document) throws DocumentException;
    }

    /**
     * A simple record to hold the generated PDF content and a suggested filename.
     *
     * @param content           The PDF content as a byte array.
     * @param suggestedFilename The dynamically generated filename (e.g., "JohnDoe-Resume-2023-10-27.pdf").
     */
    public record PdfFile(byte[] content, String suggestedFilename) {
    }
}