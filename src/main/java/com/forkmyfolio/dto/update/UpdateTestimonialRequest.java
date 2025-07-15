package com.forkmyfolio.dto.update;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Optional;

@Data
public class UpdateTestimonialRequest {
    private Optional<@Size(max = 2000) String> quote;
    private Optional<@Size(max = 100) String> authorName;
    private Optional<@Size(max = 100) String> authorTitle;
}