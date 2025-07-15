package com.forkmyfolio.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class ExperienceDto {
    private UUID uuid;
    private String jobTitle;
    private String companyName;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
}