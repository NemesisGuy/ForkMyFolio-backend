package com.forkmyfolio.dto.response;

import com.forkmyfolio.model.Skill;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@Schema(name = "SkillDto", description = "Public representation of a single skill and its proficiency level.")
public class SkillDto {

    private UUID uuid;
    private String name;
    private Skill.SkillLevel level;
}