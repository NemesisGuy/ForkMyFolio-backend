package com.forkmyfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateExperienceRequest {
    @NotBlank @Size(max = 100) private String jobTitle;
    @NotBlank @Size(max = 100) private String companyName;
    @Size(max = 100) private String location;
    @NotNull private LocalDate startDate;
    private LocalDate endDate;
    private String description;
}