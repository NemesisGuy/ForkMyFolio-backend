package com.forkmyfolio.dto.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(name = "CreateQualificationRequest", description = "Request body for creating a new qualification.")
public class CreateQualificationRequest {

    @NotBlank
    @Size(max = 255)
    private String qualificationName;

    @NotBlank
    @Size(max = 255)
    private String institutionName;

    @NotNull
    @Min(1900)
    @Max(2100)
    private Integer completionYear;

    @Size(max = 255)
    private String grade;
}