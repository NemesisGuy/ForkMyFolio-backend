package com.forkmyfolio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdateSettingRequest {
    @NotBlank
    private String name;
    @NotNull
    private UUID uuid;
    @NotNull
    private Boolean enabled;
}