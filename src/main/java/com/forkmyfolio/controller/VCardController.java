package com.forkmyfolio.controller;

import com.forkmyfolio.aop.SkipApiResponseWrapper; // <-- IMPORT
import com.forkmyfolio.aop.TrackVisitor;
import com.forkmyfolio.model.PortfolioProfile;
import com.forkmyfolio.model.enums.VisitorStatType;
import com.forkmyfolio.service.PortfolioProfileService;
import com.forkmyfolio.service.VCardService;
import com.forkmyfolio.service.VisitorStatsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vcard")
@RequiredArgsConstructor
public class VCardController {

    private static final Logger log = LoggerFactory.getLogger(VCardController.class);

    private final VCardService vCardService;
    private final PortfolioProfileService portfolioProfileService;
    private final VisitorStatsService visitorStatsService;

    @GetMapping("/download")
    @TrackVisitor(VisitorStatType.VCARD_DOWNLOAD)
    @SkipApiResponseWrapper // <-- ADD THIS ANNOTATION
    public ResponseEntity<byte[]> downloadVCard() {
        log.info("GET /api/v1/vcard/download - Received request for vCard");
        try {
            PortfolioProfile profile = portfolioProfileService.getPublicProfile();
            VCardService.VCardFile vCardFile = vCardService.generateVCard(profile);

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_TYPE, "text/vcard; charset=utf-8");
            headers.setContentDispositionFormData("attachment", vCardFile.suggestedFilename());

            log.info("Successfully generated vCard. Filename: {}", vCardFile.suggestedFilename());
            return new ResponseEntity<>(vCardFile.content(), headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to generate vCard.", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}