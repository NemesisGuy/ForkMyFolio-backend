package com.forkmyfolio.service;

import com.forkmyfolio.dto.AdminStatsDto;
import com.forkmyfolio.model.VisitorStats;
import com.forkmyfolio.model.enums.VisitorStatType;
import com.forkmyfolio.repository.VisitorStatsRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VisitorStatsService {

    private static final Logger log = LoggerFactory.getLogger(VisitorStatsService.class);
    private static final String TOTAL_VISITS_REF_ID = "site_total";
    private static final String PROJECTS_SECTION_REF_ID = "projects_section";
    private static final String SKILLS_SECTION_REF_ID = "skills_section";
    private static final String EXPERIENCE_SECTION_REF_ID = "experience_section";
    private static final String QUALIFICATIONS_SECTION_REF_ID = "qualifications_section";
    private static final String CONTACT_MESSAGE_SUBMISSION_REF_ID = "contact_submission";
    private static final String PDF_DOWNLOAD_REF_ID = "pdf_download";
    private static final String TESTIMONIALS_SECTION_REF_ID = "testimonials_section";
    private static final String LOGIN_SUCCESS_REF_ID = "auth_login_success";
    private static final String LOGIN_FAILURE_REF_ID = "auth_login_failure";
    private static final String LOGOUT_SUCCESS_REF_ID = "auth_logout_success";

    private final VisitorStatsRepository visitorStatsRepository;

    @Async // Run this in a separate thread to not slow down the user's page load
    @Transactional
    public void incrementTotalVisits() {
        log.debug("Incrementing total site visits.");
        incrementStat(VisitorStatType.TOTAL_VISITS, TOTAL_VISITS_REF_ID);
    }

    @Async // Also run this asynchronously
    @Transactional
    public void incrementProjectView(String projectId) {
        log.debug("Incrementing project view for ID: {}", projectId);
        incrementStat(VisitorStatType.PROJECT_VIEW, projectId);
    }

    @Async
    @Transactional
    public void incrementProjectsSectionView() {
        log.debug("Incrementing projects section views.");
        incrementStat(VisitorStatType.PROJECTS_SECTION_VIEW, PROJECTS_SECTION_REF_ID);
    }

    @Async
    @Transactional
    public void incrementSkillsSectionView() {
        log.debug("Incrementing skills section views.");
        incrementStat(VisitorStatType.SKILLS_SECTION_VIEW, SKILLS_SECTION_REF_ID);
    }

    @Async
    @Transactional
    public void incrementExperienceSectionView() {
        log.debug("Incrementing experience section views.");
        incrementStat(VisitorStatType.EXPERIENCE_SECTION_VIEW, EXPERIENCE_SECTION_REF_ID);
    }

    @Async
    @Transactional
    public void incrementQualificationsSectionView() {
        log.debug("Incrementing qualifications section views.");
        incrementStat(VisitorStatType.QUALIFICATIONS_SECTION_VIEW, QUALIFICATIONS_SECTION_REF_ID);
    }

    @Async
    @Transactional
    public void incrementContactMessageSubmission() {
        log.debug("Incrementing contact message submissions.");
        incrementStat(VisitorStatType.CONTACT_MESSAGE_SUBMISSION, CONTACT_MESSAGE_SUBMISSION_REF_ID);
    }

    @Async
    @Transactional
    public void incrementPdfDownload() {
        log.debug("Incrementing PDF download count.");
        incrementStat(VisitorStatType.PDF_DOWNLOAD, PDF_DOWNLOAD_REF_ID);
    }

    @Async
    @Transactional
    public void incrementTestimonialsSectionView() {
        log.debug("Incrementing testimonials section views.");
        incrementStat(VisitorStatType.TESTIMONIALS_SECTION_VIEW, TESTIMONIALS_SECTION_REF_ID);
    }

    @Async
    @Transactional
    public void incrementLoginSuccess() {
        log.debug("Incrementing successful login count.");
        incrementStat(VisitorStatType.LOGIN_SUCCESS, LOGIN_SUCCESS_REF_ID);
    }

    @Async
    @Transactional
    public void incrementLoginFailure() {
        log.debug("Incrementing failed login attempt count.");
        incrementStat(VisitorStatType.LOGIN_FAILURE, LOGIN_FAILURE_REF_ID);
    }

    @Async
    @Transactional
    public void incrementLogoutSuccess() {
        log.debug("Incrementing successful logout count.");
        incrementStat(VisitorStatType.LOGOUT_SUCCESS, LOGOUT_SUCCESS_REF_ID);
    }

    private void incrementStat(VisitorStatType type, String refId) {
        VisitorStats stats = visitorStatsRepository.findByTypeAndRefId(type, refId)
            .orElseGet(() -> {
                log.info("No stats found for type={}, refId={}. Creating new record.", type, refId);
                VisitorStats newStats = new VisitorStats();
                newStats.setType(type);
                newStats.setRefId(refId);
                return newStats;
            });

        stats.setCount(stats.getCount() + 1);
        visitorStatsRepository.save(stats);
    }

    @Transactional(readOnly = true)
    public AdminStatsDto getStats() {
        log.debug("Fetching all visitor stats for admin dashboard.");
        List<VisitorStats> allStats = visitorStatsRepository.findAll();
        
        // Group stats by type for efficient processing
        Map<VisitorStatType, List<VisitorStats>> groupedStats = allStats.stream()
                .collect(Collectors.groupingBy(VisitorStats::getType));

        long totalVisits = getSumForStatType(groupedStats, VisitorStatType.TOTAL_VISITS);
        long projectsSectionViews = getSumForStatType(groupedStats, VisitorStatType.PROJECTS_SECTION_VIEW);
        long skillsSectionViews = getSumForStatType(groupedStats, VisitorStatType.SKILLS_SECTION_VIEW);
        long experienceSectionViews = getSumForStatType(groupedStats, VisitorStatType.EXPERIENCE_SECTION_VIEW);
        long qualificationsSectionViews = getSumForStatType(groupedStats, VisitorStatType.QUALIFICATIONS_SECTION_VIEW);
        long contactMessageSubmissions = getSumForStatType(groupedStats, VisitorStatType.CONTACT_MESSAGE_SUBMISSION);
        long pdfDownloads = getSumForStatType(groupedStats, VisitorStatType.PDF_DOWNLOAD);
        long testimonialsSectionViews = getSumForStatType(groupedStats, VisitorStatType.TESTIMONIALS_SECTION_VIEW);
        long loginSuccesses = getSumForStatType(groupedStats, VisitorStatType.LOGIN_SUCCESS);
        long loginFailures = getSumForStatType(groupedStats, VisitorStatType.LOGIN_FAILURE);
        long logoutSuccesses = getSumForStatType(groupedStats, VisitorStatType.LOGOUT_SUCCESS);

        Map<String, Long> projectViews = groupedStats.getOrDefault(VisitorStatType.PROJECT_VIEW, List.of()).stream()
                .collect(Collectors.toMap(VisitorStats::getRefId, VisitorStats::getCount));

        // Using setters is more flexible than relying on a specific constructor order
        AdminStatsDto dto = new AdminStatsDto();
        dto.setTotalVisits(totalVisits);
        dto.setProjects(projectViews);
        dto.setProjectsSectionViews(projectsSectionViews);
        dto.setSkillsSectionViews(skillsSectionViews);
        dto.setExperienceSectionViews(experienceSectionViews);
        dto.setQualificationsSectionViews(qualificationsSectionViews);
        dto.setContactMessageSubmissions(contactMessageSubmissions);
        dto.setPdfDownloads(pdfDownloads);
        dto.setTestimonialsSectionViews(testimonialsSectionViews);
        dto.setLoginSuccesses(loginSuccesses);
        dto.setLoginFailures(loginFailures);
        dto.setLogoutSuccesses(logoutSuccesses);

        return dto;
    }

    /**
     * Helper method to safely calculate the sum of counts for a given stat type from a pre-grouped map.
     */
    private long getSumForStatType(Map<VisitorStatType, List<VisitorStats>> groupedStats, VisitorStatType type) {
        return groupedStats.getOrDefault(type, List.of()).stream()
                .mapToLong(VisitorStats::getCount)
                .sum();
    }
}