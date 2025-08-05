package com.forkmyfolio.config.startup;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forkmyfolio.model.Skill;
import com.forkmyfolio.repository.SkillRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Responsible for seeding the global 'skills' table from all JSON files
 * found in the 'data/skills/' directory at application startup.
 * This is the single source of truth for initial skill data.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SkillDataLoader {

    private final SkillRepository skillRepository;
    private final ObjectMapper objectMapper;
    private static final String SKILLS_PATTERN = "classpath:data/skills/*.json";

    @PostConstruct
    @Transactional
    public void loadSkills() {
        if (skillRepository.count() > 0) {
            log.info("Global skills table is not empty. Skipping initial data load from skills JSON files.");
            return;
        }

        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(SKILLS_PATTERN);

            if (resources.length == 0) {
                log.warn("Could not find any skill files matching pattern '{}'. No global skills will be seeded.", SKILLS_PATTERN);
                return;
            }

            log.info("Found {} skill files to process from '{}'.", resources.length, SKILLS_PATTERN);

            List<Skill> allSkillsFromJson = new ArrayList<>();
            for (Resource resource : resources) {
                log.info("Reading skills from: {}", resource.getFilename());
                try (InputStream inputStream = resource.getInputStream()) {
                    List<Skill> skillsFromFile = objectMapper.readValue(inputStream, new TypeReference<>() {});
                    allSkillsFromJson.addAll(skillsFromFile);
                } catch (IOException e) {
                    log.error("Failed to read or parse skill file: {}", resource.getFilename(), e);
                }
            }

            if (allSkillsFromJson.isEmpty()) {
                log.warn("No skills were loaded from the JSON files. Aborting seed process.");
                return;
            }

            // De-duplicate skills by name (case-insensitive) across all files.
            // Using a LinkedHashMap preserves the insertion order of the first occurrence.
            Map<String, Skill> uniqueSkillsMap = new LinkedHashMap<>();
            allSkillsFromJson.forEach(skill -> {
                if (skill != null && skill.getName() != null && !skill.getName().isBlank()) {
                    uniqueSkillsMap.putIfAbsent(skill.getName().toLowerCase(), skill);
                }
            });

            List<Skill> skillsToSeed = new ArrayList<>(uniqueSkillsMap.values());

            // Ensure all skills have a UUID before saving
            skillsToSeed.forEach(skill -> {
                if (skill.getUuid() == null) {
                    skill.setUuid(UUID.randomUUID());
                }
            });

            skillRepository.saveAll(skillsToSeed);
            log.info("Successfully loaded and saved {} unique global skills from {} files to the database.", skillsToSeed.size(), resources.length);

        } catch (Exception e) {
            log.error("An unexpected error occurred during the skill loading process.", e);
        }
    }
}