package com.forkmyfolio.controller;

import com.forkmyfolio.aop.SkipApiResponseWrapper; // <-- IMPORT
import com.forkmyfolio.aop.TrackVisitor;
import com.forkmyfolio.model.enums.VisitorStatType;
import com.forkmyfolio.service.PdfGenerationService;
import com.forkmyfolio.service.VisitorStatsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pdf")
@RequiredArgsConstructor
public class PdfController {

    private static final Logger log = LoggerFactory.getLogger(PdfController.class);

    private final PdfGenerationService pdfGenerationService;
    private final VisitorStatsService visitorStatsService;

    /**
     * GET /api/v1/pdf/templates
     * Returns a list of available PDF template names for the frontend.
     */
    @GetMapping("/templates")
    public ResponseEntity<List<String>> getAvailableTemplates() {
        return ResponseEntity.ok(pdfGenerationService.getAvailableTemplateNames());
    }


    @GetMapping("/download")
    @TrackVisitor(VisitorStatType.PDF_DOWNLOAD)
    @SkipApiResponseWrapper // <-- ADD THIS ANNOTATION
    public ResponseEntity<byte[]> downloadPortfolioAsPdf(
            @RequestParam(value = "template", defaultValue = "modern") String templateName) {

        log.info("GET /api/v1/pdf/download - Received request for PDF with template: {}", templateName);
        try {
            PdfGenerationService.PdfFile pdfFile = pdfGenerationService.generatePortfolioPdf(templateName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", pdfFile.suggestedFilename());
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            log.info("Successfully generated PDF. Template: {}, Filename: {}, Size: {} bytes.",
                    templateName, pdfFile.suggestedFilename(), pdfFile.content().length);
            return new ResponseEntity<>(pdfFile.content(), headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to generate portfolio PDF with template '{}'.", templateName, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}