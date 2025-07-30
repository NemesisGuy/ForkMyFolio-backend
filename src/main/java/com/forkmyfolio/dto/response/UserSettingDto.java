package com.forkmyfolio.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UserSettingDto", description = "Represents a single, effective setting for a user, combining global defaults with user-specific overrides.")
public class UserSettingDto {

    @Schema(description = "The unique name of the setting.", example = "SHOW_SKILLS")
    private String name;

    @Schema(description = "The effective value of the setting.", example = "true")
    private String value;

    @Schema(description = "The description of what the setting controls.", example = "Display the \"Skills\" section on the public site.")
    private String description;
}