package com.forkmyfolio.controller.guest;

import com.forkmyfolio.dto.response.QualificationDto;
import com.forkmyfolio.mapper.QualificationMapper;
import com.forkmyfolio.model.Qualification;
import com.forkmyfolio.service.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/portfolios/{slug}/qualifications")
public class PortfolioQualificationsController {

    private final PortfolioService portfolioService;
    private final QualificationMapper qualificationMapper;

    public PortfolioQualificationsController(PortfolioService portfolioService, QualificationMapper qualificationMapper) {
        this.portfolioService = portfolioService;
        this.qualificationMapper = qualificationMapper;
    }

    @GetMapping
    public ResponseEntity<List<QualificationDto>> getPortfolioQualifications(@PathVariable String slug) {
        Set<Qualification> qualifications = portfolioService.getPublicPortfolioUserBySlug(slug).getQualifications();
        List<QualificationDto> qualificationDtos = qualifications.stream()
                .map(qualificationMapper::toDto)
                .toList();

        return ResponseEntity.ok(qualificationDtos);
    }
}


