package com.forkmyfolio.config;

import com.forkmyfolio.model.Setting;
import com.forkmyfolio.repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final SettingRepository settingRepository;

    @Override
    public void run(String... args) {
        initializeSettings();
    }

    /**
     * Initializes default application settings if they do not already exist in the database.
     * This process is idempotent and safe to run on every application startup.
     */
    private void initializeSettings() {
        log.info("Checking for and initializing default settings...");

        // --- A single, unified list of all default settings ---
        List<Setting> defaultSettings = List.of(
                new Setting("DEFAULT_PDF_TEMPLATE", "modern", "The default template used for the public PDF download button."),
                new Setting("SHOW_PROJECTS", "true", "Display the \"Projects\" section on the public site."),
                new Setting("SHOW_SKILLS", "true", "Display the \"Skills\" section on the public site."),
                new Setting("SHOW_EXPERIENCE", "true", "Display the \"Experience\" section on the public site."),
                new Setting("SHOW_TESTIMONIALS", "true", "Display the \"Testimonials\" section on the public site."),
                new Setting("SHOW_QUALIFICATIONS", "true", "Display the \"Qualifications\" section on the public site."),
                new Setting("SHOW_CONTACT_FORM", "true", "Display the \"Contact Me\" section on the public site.")
        );

        // --- One consistent loop to safely initialize all settings ---
        for (Setting defaultSetting : defaultSettings) {
            // Check if a setting with this name already exists
            settingRepository.findByName(defaultSetting.getName())
                    .ifPresentOrElse(
                            // If it exists, do nothing.
                            existingSetting -> log.trace("Setting '{}' already exists. Skipping.", existingSetting.getName()),
                            // If it does not exist, save the new default setting.
                            () -> {
                                log.info("Initializing new setting: '{}' with value '{}'", defaultSetting.getName(), defaultSetting.getValue());
                                settingRepository.save(defaultSetting);
                            }
                    );
        }
        log.info("Default settings check complete.");
    }
}