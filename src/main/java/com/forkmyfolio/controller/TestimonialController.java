package com.forkmyfolio.controller;

import com.forkmyfolio.aop.TrackVisitor;
import com.forkmyfolio.dto.response.TestimonialDto;
import com.forkmyfolio.mapper.TestimonialMapper;
import com.forkmyfolio.model.enums.VisitorStatType;
import com.forkmyfolio.service.TestimonialService;
import com.forkmyfolio.service.VisitorStatsService;
import com.forkmyfolio.util.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/testimonials")
@Tag(name = "Testimonials", description = "Public endpoint for viewing testimonials.")
public class TestimonialController {

    private static final Logger logger = LoggerFactory.getLogger(TestimonialController.class);
    private final TestimonialService testimonialService;
    private final VisitorStatsService visitorStatsService;
    private final TestimonialMapper testimonialMapper;
    private final SecurityUtils securityUtils;

    @Autowired
    public TestimonialController(TestimonialService testimonialService, VisitorStatsService visitorStatsService, TestimonialMapper testimonialMapper, SecurityUtils securityUtils) {
        this.testimonialService = testimonialService;
        this.visitorStatsService = visitorStatsService;
        this.testimonialMapper = testimonialMapper;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    @TrackVisitor(VisitorStatType.TESTIMONIALS_SECTION_VIEW)
    public List<TestimonialDto> getPublicTestimonials() {
        logger.info("Request received for public testimonials.");

        List<TestimonialDto> testimonials = testimonialService.getPublicTestimonials().stream()
                .map(testimonialMapper::toDto)
                .collect(Collectors.toList());
        logger.info("Successfully retrieved {} testimonials.", testimonials.size());
        return testimonials;
    }
}