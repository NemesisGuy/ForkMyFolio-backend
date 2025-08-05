package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.response.UserSettingDto;
import com.forkmyfolio.dto.update.UpdateUserSettingRequest;
import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.model.Setting;
import com.forkmyfolio.model.User;
import com.forkmyfolio.model.UserSetting;
import com.forkmyfolio.repository.SettingRepository;
import com.forkmyfolio.repository.UserSettingRepository;
import com.forkmyfolio.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service layer for managing user-specific settings.
 * This service handles the logic of overriding global settings with user preferences.
 */
@Service
public class UserSettingService {

    private final UserSettingRepository userSettingRepository;
    private final SettingRepository settingRepository;
    private final UserService userService;

    @Autowired
    public UserSettingService(UserSettingRepository userSettingRepository, SettingRepository settingRepository, UserService userService) {
        this.userSettingRepository = userSettingRepository;
        this.settingRepository = settingRepository;
        this.userService = userService;
    }

    /**
     * Gets the effective settings for a given user.
     * It starts with all global settings and then applies the user's specific overrides.
     *
     * @param user The user for whom to get the settings.
     * @return A map of setting names to their effective values.
     */
    public Map<String, String> getEffectiveSettingsMapForUser(User user) {
        // 1. Start with a base map of all global settings (name -> value).
        Map<String, String> effectiveSettings = settingRepository.findAll().stream()
                .collect(Collectors.toMap(Setting::getName, Setting::getValue));

        // 2. Fetch the user's specific settings.
        List<UserSetting> userOverrides = userSettingRepository.findByUser(user);

        // 3. Apply user overrides to the map.
        userOverrides.forEach(override -> effectiveSettings.put(override.getName(), override.getValue()));

        return effectiveSettings;
    }

    /**
     * Gets the effective settings for a given user as a list of DTOs.
     * It starts with all global settings and then applies the user's specific overrides,
     * providing a complete view for the frontend.
     *
     * @param user The user for whom to get the settings.
     * @return A list of DTOs representing the final, effective settings for the given user.
     */
    public List<UserSettingDto> getEffectiveSettingsForUser(User user) {
        // 1. Get all global settings as a map for easy lookup of descriptions.
        Map<String, Setting> globalSettingsMap = settingRepository.findAll().stream()
                .collect(Collectors.toMap(Setting::getName, Function.identity()));

        // 2. Get the user's overrides as a map.
        Map<String, String> userOverridesMap = userSettingRepository.findByUser(user).stream()
                .collect(Collectors.toMap(UserSetting::getName, UserSetting::getValue));

        // 3. Build the final DTO list, combining global info with user values.
        return globalSettingsMap.values().stream()
                .map(globalSetting -> {
                    String effectiveValue = userOverridesMap.getOrDefault(globalSetting.getName(), globalSetting.getValue());
                    return new UserSettingDto(
                            globalSetting.getUuid(),
                            globalSetting.getName(),
                            effectiveValue,
                            globalSetting.getDescription()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Gets the effective settings for the currently authenticated user.
     * This is a convenience method for the /me endpoints.
     *
     * @return A list of DTOs representing the final, effective settings for the current user.
     */
    public List<UserSettingDto> getMyEffectiveSettings() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        return getEffectiveSettingsForUser(currentUser);
    }

    /**
     * Updates a list of settings for the currently authenticated user.
     * This method will either update an existing setting or create a new one if it doesn't exist.
     *
     * @param requests A list of setting updates.
     * @return The complete, updated list of effective settings for the user.
     */
    @Transactional
    public List<UserSettingDto> updateMySettings(List<UpdateUserSettingRequest> requests) {
        User currentUser = userService.getCurrentAuthenticatedUser();

        // 1. Get all global settings to validate UUIDs and get names.
        Map<UUID, Setting> globalSettingsMap = settingRepository.findAll().stream()
                .collect(Collectors.toMap(Setting::getUuid, Function.identity()));

        // 2. Fetch existing user-specific settings for this user.
        Map<String, UserSetting> existingUserSettingsMap = userSettingRepository.findByUser(currentUser).stream()
                .collect(Collectors.toMap(UserSetting::getName, Function.identity()));

        List<UserSetting> settingsToSave = requests.stream().map(request -> {
            // 3. Validate that the UUID from the request corresponds to a real global setting.
            Setting globalSetting = globalSettingsMap.get(request.getUuid());
            if (globalSetting == null) {
                throw new ResourceNotFoundException("Setting with UUID " + request.getUuid() + " not found.");
            }
            String settingName = globalSetting.getName();

            // 4. Check if a user-specific setting already exists.
            UserSetting userSetting = existingUserSettingsMap.get(settingName);
            if (userSetting != null) {
                // If it exists, update its value.
                userSetting.setValue(request.getValue());
            } else {
                // If not, create a new UserSetting.
                userSetting = new UserSetting(currentUser, settingName, request.getValue());
            }
            return userSetting;
        }).collect(Collectors.toList());

        userSettingRepository.saveAll(settingsToSave);

        // After saving, return the full, effective list of settings to the client.
        return getEffectiveSettingsForUser(currentUser);
    }
}