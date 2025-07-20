package com.forkmyfolio.config;

import com.forkmyfolio.model.Setting;
import com.forkmyfolio.repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SettingRepository settingRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeSettings();
    }

    private void initializeSettings() {
        List<Setting> defaultSettings = List.of(
                new Setting("SHOW_PROJECTS", true, "Display the \"Projects\" section on the public site."),
                new Setting("SHOW_SKILLS", true, "Display the \"Skills\" section on the public site."),
                new Setting("SHOW_EXPERIENCE", true, "Display the \"Experience\" section on the public site."),
                new Setting("SHOW_TESTIMONIALS", true, "Display the \"Testimonials\" section on the public site."),
                new Setting("SHOW_QUALIFICATIONS", true, "Display the \"Qualifications\" section on the public site."),
                new Setting("SHOW_CONTACT_FORM", true, "Display the \"Contact Me\" section on the public site.")
        );

        for (Setting defaultSetting : defaultSettings) {
            settingRepository.findByName(defaultSetting.getName())
                    .ifPresentOrElse(s -> {
                    }, () -> settingRepository.save(defaultSetting));
        }
    }
}