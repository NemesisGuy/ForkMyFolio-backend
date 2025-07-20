package com.forkmyfolio.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTestimonialRequest {
    @NotBlank
    @Size(max = 2000)
    private String quote;
    @NotBlank
    @Size(max = 100)
    private String authorName;
    @Size(max = 100)
    private String authorTitle;
}