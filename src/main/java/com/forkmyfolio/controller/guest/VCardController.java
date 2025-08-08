package com.forkmyfolio.controller.guest;

import com.forkmyfolio.aop.SkipApiResponseWrapper;
import com.forkmyfolio.aop.TrackVisitor;
import com.forkmyfolio.model.PortfolioProfile;
import com.forkmyfolio.model.User;
import com.forkmyfolio.model.enums.VisitorStatType;
import com.forkmyfolio.service.PortfolioService;
import com.forkmyfolio.service.impl.VCardService;
import com.forkmyfolio.service.impl.VisitorStatsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/portfolios/{slug}/vcard")
@RequiredArgsConstructor
public class VCardController {

    private static final Logger log = LoggerFactory.getLogger(VCardController.class);

    private final VCardService vCardService;
    private final PortfolioService portfolioService;
    private final VisitorStatsService visitorStatsService;

    @GetMapping()
    @TrackVisitor(VisitorStatType.VCARD_DOWNLOAD)
    @SkipApiResponseWrapper
    public ResponseEntity<byte[]> downloadVCard(@PathVariable String slug) {
        log.info("GET /api/v1/portfolios/{}/vcard/download - Received request for vCard", slug);
        try {

            User user = portfolioService.getPublicPortfolioUserBySlug(slug);
            PortfolioProfile profile = user.getPortfolioProfile();

            VCardService.VCardFile vCardFile = vCardService.generateVCard(profile);

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_TYPE, "text/vcard; charset=utf-8");
            headers.setContentDispositionFormData("attachment", vCardFile.suggestedFilename());

            log.info("Successfully generated vCard for {}. Filename: {}", slug, vCardFile.suggestedFilename());
            return new ResponseEntity<>(vCardFile.content(), headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to generate vCard for slug: {}", slug, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}