package com.forkmyfolio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SettingDto {
    private UUID uuid;
    private String name;
    private boolean enabled;
    private String description;
}