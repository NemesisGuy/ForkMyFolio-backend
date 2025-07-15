package com.forkmyfolio.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@Schema(name = "QualificationDto", description = "Publicly available information about a single qualification (e.g., degree, certification).")
public class QualificationDto {

    @Schema(description = "The public, unique identifier for the qualification.")
    private UUID uuid;

    @Schema(description = "The name of the qualification.", example = "Bachelor of Science in Computer Science")
    private String qualificationName;

    @Schema(description = "The name of the institution that awarded the qualification.", example = "University of California, Berkeley")
    private String institutionName;

    @Schema(description = "The year the qualification was completed.", example = "2020")
    private Integer completionYear;

    @Schema(description = "The grade or honors received, if applicable.", example = "First Class Honours")
    private String grade;
}