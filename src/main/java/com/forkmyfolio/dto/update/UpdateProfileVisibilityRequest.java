package com.forkmyfolio.dto.update;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UpdateProfileVisibilityRequest {
    @Schema(description = "Set to true to make the portfolio public, false to make it private.", example = "true")
    @JsonProperty("isPublic")
    private boolean isPublic;
}