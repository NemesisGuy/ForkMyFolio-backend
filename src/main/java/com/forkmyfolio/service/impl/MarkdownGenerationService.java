package com.forkmyfolio.service.impl;

import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.model.*;
import com.forkmyfolio.repository.*;
import com.forkmyfolio.service.pdf.PortfolioData;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarkdownGenerationService {

    private static final Logger log = LoggerFactory.getLogger(MarkdownGenerationService.class);

    //<editor-fold desc="Repositories">
    private final UserRepository userRepository;
    private final PortfolioProfileRepository portfolioProfileRepository;
    private final ExperienceRepository experienceRepository;
    private final ProjectRepository projectRepository;
    private final QualificationRepository qualificationRepository;
    //</editor-fold>

    /**
     * Generates a portfolio in Markdown format for a specific user.
     *
     * @param user The User entity for whom the portfolio is being generated.
     * @return A MarkdownFile record containing the byte content and suggested filename.
     */
    @Transactional(readOnly = true)
    public MarkdownFile generatePortfolioMarkdown(User user) {
        log.info("Starting Markdown generation process for user '{}'", user.getSlug());

        PortfolioProfile profile = portfolioProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio profile not found for user: " + user.getEmail()));

        // Fetch all items for the user, using sorted methods where appropriate.
        List<Experience> experiences = experienceRepository.findByUserOrderByDisplayOrderAsc(user);
        List<Qualification> qualifications = qualificationRepository.findByUserOrderByCompletionYearDescStartYearDesc(user);
        List<Project> projects = projectRepository.findByUserOrderByDisplayOrderAsc(user);

        // Fetch skills from the User entity's relationships, combining global and user-specific data.
        List<Skill> skills = user.getUserSkills().stream()
                .filter(UserSkill::isVisible) // Only include skills the user wants to show
                .map(userSkill -> {
                    Skill globalSkill = userSkill.getSkill();
                    // Create a transient Skill object for the Markdown, populated with the correct data
                    Skill mdSkill = new Skill();
                    mdSkill.setName(globalSkill.getName());
                    mdSkill.setCategory(globalSkill.getCategory());
                    mdSkill.setIcon(globalSkill.getIcon());
                    // Use user-specific data where it exists
                    mdSkill.setLevel(userSkill.getLevel());
                    mdSkill.setDescription(userSkill.getDescription());
                    mdSkill.setVisible(userSkill.isVisible());
                    return mdSkill;
                })
                .collect(Collectors.toList());

        PortfolioData portfolioData = new PortfolioData(profile, experiences, qualifications, projects, skills);

        log.info("Generating Markdown for user: {}", user.getEmail());

        // 2. Build the Markdown string
        String markdownContent = buildMarkdown(portfolioData);

        // 3. Create the file record
        String filename = String.format("%s%s-Portfolio-%s.md",
                user.getFirstName(),
                user.getLastName(),
                LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        );

        log.info("Markdown generation complete. Final content length: {} characters.", markdownContent.length());
        return new MarkdownFile(markdownContent.getBytes(StandardCharsets.UTF_8), filename);
    }

    private String buildMarkdown(PortfolioData data) {
        StringBuilder md = new StringBuilder();
        PortfolioProfile profile = data.profile();
        User user = profile.getUser();

        // --- Header ---
        md.append("# ").append(user.getFirstName()).append(" ").append(user.getLastName()).append("\n");
        if (profile.getHeadline() != null && !profile.getHeadline().isBlank()) {
            md.append("> ").append(profile.getHeadline()).append("\n\n");
        }
        appendContactInfo(md, user, profile);
        md.append("\n---\n\n");

        // --- About Me ---
        if (profile.getSummary() != null && !profile.getSummary().isBlank()) {
            md.append("## 📖 About Me\n\n");
            md.append(profile.getSummary()).append("\n\n---\n\n");
        }

        // --- Experience ---
        if (!data.experiences().isEmpty()) {
            md.append("## 💼 Experience\n\n");
            data.experiences().forEach(exp -> appendExperience(md, exp));
            md.append("---\n\n");
        }

        // --- Projects ---
        if (!data.projects().isEmpty()) {
            md.append("## 🚀 Projects\n\n");
            data.projects().forEach(proj -> appendProject(md, proj));
            md.append("---\n\n");
        }

        // --- Skills ---
        if (!data.skills().isEmpty()) {
            md.append("## 🛠️ Skills & Technologies\n\n");
            appendSkills(md, data.skills());
            md.append("---\n\n");
        }

        // --- Qualifications ---
        if (!data.qualifications().isEmpty()) {
            md.append("## 🎓 Qualifications & Education\n\n");
            data.qualifications().forEach(qual -> appendQualification(md, qual));
            md.append("---\n\n");
        }

        return md.toString();
    }

    private void appendContactInfo(StringBuilder md, User user, PortfolioProfile profile) {
        md.append("📧 ").append(user.getEmail());
      /*  if (profile.getPhoneNumber() != null && !profile.getPhoneNumber().isBlank()) {
            md.append(" | 📞 ").append(profile.getPhoneNumber());
        }*/
        if (profile.getWebsiteUrl() != null && !profile.getWebsiteUrl().isBlank()) {
            md.append(" | 🌐 [Website](").append(profile.getWebsiteUrl()).append(")");
        }
        if (profile.getLinkedinUrl() != null && !profile.getLinkedinUrl().isBlank()) {
            md.append(" | 💼 [LinkedIn](").append(profile.getLinkedinUrl()).append(")");
        }
        if (profile.getGithubUrl() != null && !profile.getGithubUrl().isBlank()) {
            md.append(" | 💻 [GitHub](").append(profile.getGithubUrl()).append(")");
        }
        md.append("\n");
    }

    private void appendExperience(StringBuilder md, Experience exp) {
        md.append("### ").append(exp.getJobTitle()).append("\n");
        md.append("**").append(exp.getCompanyName()).append("**");
        if (exp.getLocation() != null && !exp.getLocation().isBlank()) {
            md.append(" | ").append(exp.getLocation());
        }
        md.append("\n\n");

        String startDate = exp.getStartDate().format(DateTimeFormatter.ofPattern("MMM yyyy"));
        String endDate = exp.getEndDate() != null ? exp.getEndDate().format(DateTimeFormatter.ofPattern("MMM yyyy")) : "Present";
        md.append("*").append(startDate).append(" - ").append(endDate).append("*\n\n");

        if (exp.getDescription() != null && !exp.getDescription().isBlank()) {
            md.append(exp.getDescription().replaceAll("\\r\\n|\\r|\\n", "\n> ")).append("\n\n");
        }

        if (exp.getAchievements() != null && !exp.getAchievements().isBlank()) {
            md.append("**Key Achievements:**\n");
            for (String achievement : exp.getAchievements().split("\\r?\\n")) {
                if (!achievement.isBlank()) {
                    md.append("- ").append(achievement.trim()).append("\n");
                }
            }
            md.append("\n");
        }

        if (exp.getSkills() != null && !exp.getSkills().isEmpty()) {
            String skills = exp.getSkills().stream().map(Skill::getName).collect(Collectors.joining(", "));
            md.append("**Skills:** ").append(skills).append("\n\n");
        }
    }

    private void appendProject(StringBuilder md, Project proj) {
        md.append("### ").append(proj.getTitle()).append("\n\n");
        if (proj.getDescription() != null && !proj.getDescription().isBlank()) {
            md.append(proj.getDescription()).append("\n\n");
        }

        if (proj.getRepoUrl() != null && !proj.getRepoUrl().isBlank()) {
            md.append("- **Repository:** [").append(proj.getRepoUrl()).append("](").append(proj.getRepoUrl()).append(")\n");
        }
        if (proj.getLiveUrl() != null && !proj.getLiveUrl().isBlank()) {
            md.append("- **Live Demo:** [").append(proj.getLiveUrl()).append("](").append(proj.getLiveUrl()).append(")\n");
        }
        md.append("\n");

        if (proj.getSkills() != null && !proj.getSkills().isEmpty()) {
            String skills = proj.getSkills().stream().map(Skill::getName).collect(Collectors.joining(" | "));
            md.append("**Technologies:** ").append(skills).append("\n\n");
        }
    }

    private void appendSkills(StringBuilder md, List<Skill> skills) {
        // Group skills by category for better organization
        Map<String, List<Skill>> groupedSkills = skills.stream()
                .filter(s -> s.getCategory() != null && !s.getCategory().isBlank())
                .collect(Collectors.groupingBy(Skill::getCategory));

        for (Map.Entry<String, List<Skill>> entry : groupedSkills.entrySet()) {
            md.append("#### ").append(entry.getKey()).append("\n");
            String skillNames = entry.getValue().stream()
                    .map(Skill::getName)
                    .collect(Collectors.joining(", "));
            md.append(skillNames).append("\n\n");
        }

        // Add skills without a category
        List<Skill> uncategorizedSkills = skills.stream()
                .filter(s -> s.getCategory() == null || s.getCategory().isBlank())
                .toList();
        if (!uncategorizedSkills.isEmpty()) {
            md.append("#### Other Skills\n");
            String skillNames = uncategorizedSkills.stream()
                    .map(Skill::getName)
                    .collect(Collectors.joining(", "));
            md.append(skillNames).append("\n\n");
        }
    }

    private void appendQualification(StringBuilder md, Qualification qual) {
        md.append("### ").append(qual.getQualificationName()).append("\n");

        String institution = qual.getInstitutionName();
        if (StringUtils.hasText(qual.getInstitutionWebsite())) {
            institution = String.format("[%s](%s)", institution, qual.getInstitutionWebsite());
        }
        md.append("**").append(institution).append("**\n\n");

        // Date String
        String dateString;
        if (Boolean.TRUE.equals(qual.getStillStudying())) {
            dateString = qual.getStartYear() != null ? qual.getStartYear() + " - Present" : "Present";
        } else {
            dateString = qual.getCompletionYear() != null ? String.valueOf(qual.getCompletionYear()) : "N/A";
        }
        md.append("- **Date:** ").append(dateString).append("\n");

        if (StringUtils.hasText(qual.getFieldOfStudy())) {
            md.append("- **Field:** ").append(qual.getFieldOfStudy()).append("\n");
        }
        if (qual.getLevel() != null) {
            String level = qual.getLevel().name().charAt(0) + qual.getLevel().name().substring(1).toLowerCase();
            md.append("- **Level:** ").append(level).append("\n");
        }
        if (StringUtils.hasText(qual.getGrade())) {
            md.append("- **Grade:** ").append(qual.getGrade()).append("\n");
        }
        if (StringUtils.hasText(qual.getCredentialUrl())) {
            md.append("- **Credential:** [View Certificate](").append(qual.getCredentialUrl()).append(")\n");
        }
        md.append("\n");
    }

    public record MarkdownFile(byte[] content, String suggestedFilename) {
    }
}