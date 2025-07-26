package com.forkmyfolio.service.pdf;

import com.forkmyfolio.model.*;

import java.util.List;

/**
 * A data transfer object to hold all the necessary information for building a PDF.
 * This simplifies method signatures for PDF templates.
 */
public record PortfolioData(
        PortfolioProfile profile,
        List<Experience> experiences,
        List<Qualification> qualifications,
        List<Project> projects,
        List<Skill> skills
) {
}