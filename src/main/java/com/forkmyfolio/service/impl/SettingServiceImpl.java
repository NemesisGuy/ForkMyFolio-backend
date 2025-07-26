package com.forkmyfolio.service.impl;

import com.forkmyfolio.model.Setting;
import com.forkmyfolio.repository.SettingRepository;
import com.forkmyfolio.service.SettingService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
// Hidden Lines
public class SettingServiceImpl implements SettingService {

    private final SettingRepository settingRepository;

    @Override
    @Transactional(readOnly = true) // Switched to Spring's Transactional
    public List<Setting> getAllSettings() {
        return settingRepository.findAll();
    }

    @Override
    @Transactional
    public List<Setting> updateSettings(Map<UUID, String> settingsToUpdate) {
        List<UUID> uuids = settingsToUpdate.keySet().stream().toList();
        List<Setting> settingsToModify = settingRepository.findByUuidIn(uuids);

        if (settingsToModify.size() != uuids.size()) {
            throw new EntityNotFoundException("One or more settings could not be found for the given UUIDs.");
        }

        settingsToModify.forEach(setting ->
                setting.setValue(settingsToUpdate.get(setting.getUuid()).toString())
        );

        return settingRepository.saveAll(settingsToModify);
    }

    /**
     * Gets all settings intended for public consumption.
     * For backward compatibility, this converts settings with "true" or "false" values
     * into a Map<String, Boolean> for the public visibility toggles.
     */
    @Transactional
    public Map<String, Boolean> getPublicSettings() {
        return settingRepository.findAll().stream()
                .filter(s -> "true".equalsIgnoreCase(s.getValue()) || "false".equalsIgnoreCase(s.getValue()))
                .collect(Collectors.toMap(Setting::getName, s -> Boolean.parseBoolean(s.getValue())));
    }
}