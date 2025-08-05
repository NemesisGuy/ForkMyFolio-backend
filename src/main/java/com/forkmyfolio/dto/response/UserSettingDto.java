package com.forkmyfolio.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UserSettingDto", description = "Represents a single, effective setting for a user, combining global defaults with user-specific overrides.")
public class UserSettingDto {

    @Schema(description = "The unique identifier of the setting.", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    private UUID uuid;

    @Schema(description = "The unique name of the setting.", example = "portfolio.theme")
    private String name;

    @Schema(description = "The effective value of the setting.", example = "modern")
    private String value;

    @Schema(description = "The description of what the setting controls.", example = "The visual theme for the public portfolio.")
    private String description;
}