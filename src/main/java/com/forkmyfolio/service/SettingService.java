package com.forkmyfolio.service;

import com.forkmyfolio.model.Setting;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface SettingService {
    List<Setting> getAllSettings();
    List<Setting> updateSettings(Map<UUID, Boolean> settingsToUpdate);
    Map<String, Boolean> getPublicSettings();
}