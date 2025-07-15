package com.forkmyfolio.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class TestimonialDto {
    private UUID uuid;
    private String quote;
    private String authorName;
    private String authorTitle;
}