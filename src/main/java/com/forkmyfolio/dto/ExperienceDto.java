package com.forkmyfolio.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ExperienceDto {
    private Long id;
    private String jobTitle;
    private String companyName;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
}