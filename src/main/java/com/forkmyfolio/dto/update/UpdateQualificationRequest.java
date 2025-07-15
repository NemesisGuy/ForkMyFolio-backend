package com.forkmyfolio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Optional;

@Data
@Schema(name = "UpdateQualificationRequest", description = "Request body for updating an existing qualification. All fields are optional.")
public class UpdateQualificationRequest {

    private Optional<@Size(max = 255) String> qualificationName;
    private Optional<@Size(max = 255) String> institutionName;
    private Optional<@Min(1900) @Max(2100) Integer> completionYear;
    private Optional<@Size(max = 255) String> grade;
}