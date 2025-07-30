package com.forkmyfolio.dto.update;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
@Schema(name = "UpdateUserSettingRequest", description = "A single key-value pair for updating a user's setting.")
public class UpdateUserSettingRequest {

    @NotBlank
    @Schema(description = "The name of the setting to update.", example = "SHOW_SKILLS")
    private String name;

    @NotBlank
    @Schema(description = "The new value for the setting.", example = "false")
    private String value;
}