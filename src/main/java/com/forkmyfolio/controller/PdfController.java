package com.forkmyfolio.controller;

import com.forkmyfolio.aop.TrackVisitor;
import com.forkmyfolio.model.enums.VisitorStatType;
import com.forkmyfolio.service.PdfGenerationService;
import com.forkmyfolio.service.VisitorStatsService;
// THIS IS THE FIX: The unused SecurityUtils import is removed.
// import com.forkmyfolio.util.SecurityUtils;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pdf")
@RequiredArgsConstructor
public class PdfController {

    private static final Logger log = LoggerFactory.getLogger(PdfController.class);

    private final PdfGenerationService pdfGenerationService;
    // VisitorStatsService is kept as it's likely used by the @TrackVisitor aspect.
    private final VisitorStatsService visitorStatsService;
    // THIS IS THE FIX: The unused dependency is removed for better code clarity.
    // private final SecurityUtils securityUtils;

    /**
     * GET : /api/v1/pdf/download
     * Generates and returns the user's portfolio as a downloadable PDF file.
     *
     * @return A ResponseEntity containing the PDF file as a byte array.
     */
    @GetMapping("/download")
    @TrackVisitor(VisitorStatType.PDF_DOWNLOAD)
    public ResponseEntity<byte[]> downloadPortfolioAsPdf() {
        log.info("GET /api/v1/pdf/download - Received request to generate and download portfolio PDF.");
        try {
            PdfGenerationService.PdfFile pdfFile = pdfGenerationService.generatePortfolioPdf();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            // Use the suggested filename from the service
            headers.setContentDispositionFormData("attachment", pdfFile.suggestedFilename());
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            log.info("Successfully generated PDF. Filename: {}, Size: {} bytes.", pdfFile.suggestedFilename(), pdfFile.content().length);
            return new ResponseEntity<>(pdfFile.content(), headers, HttpStatus.OK);
        } catch (Exception e) {
            // THIS IS THE FIX: The log message is now more generic and accurate for iText 7.
            log.error("Failed to generate portfolio PDF.", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}