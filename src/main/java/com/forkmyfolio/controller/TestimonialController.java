package com.forkmyfolio.controller;

import com.forkmyfolio.dto.TestimonialDto;
import com.forkmyfolio.mapper.TestimonialMapper;
import com.forkmyfolio.service.TestimonialService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/testimonials")
@Tag(name = "Testimonials", description = "Public endpoint for viewing testimonials.")
public class TestimonialController {

    private static final Logger logger = LoggerFactory.getLogger(TestimonialController.class);
    private final TestimonialService testimonialService;
    private final TestimonialMapper testimonialMapper;

    @Autowired
    public TestimonialController(TestimonialService testimonialService, TestimonialMapper testimonialMapper) {
        this.testimonialService = testimonialService;
        this.testimonialMapper = testimonialMapper;
    }

    @GetMapping
    public List<TestimonialDto> getPublicTestimonials() {
        logger.info("Request received for public testimonials.");
        List<TestimonialDto> testimonials = testimonialService.getPublicTestimonials().stream()
                .map(testimonialMapper::toDto)
                .collect(Collectors.toList());
        logger.info("Successfully retrieved {} testimonials.", testimonials.size());
        return testimonials;
    }
}