package com.forkmyfolio.dto.update;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;
import java.util.Optional;

// Using Optional for partial updates (PATCH-like behavior with PUT)
@Data
public class UpdateExperienceRequest {
    private Optional<@Size(max=100) String> jobTitle;
    private Optional<@Size(max=100) String> companyName;
    private Optional<@Size(max=100) String> location;
    private Optional<LocalDate> startDate;
    private Optional<LocalDate> endDate;
    private Optional<String> description;
}