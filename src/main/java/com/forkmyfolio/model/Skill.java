package com.forkmyfolio.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.forkmyfolio.model.enums.SkillLevel;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "skills")
@Data
@EqualsAndHashCode(of = "uuid") // Important for collections
@ToString(exclude = {"projects", "experiences"}) // Avoid recursion
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, unique = true)
    private UUID uuid = UUID.randomUUID();

    @Column(nullable = false, unique = true) // The skill name must be unique across the entire platform
    private String name;

    // FIX: Switched to the top-level SkillLevel enum
    @Enumerated(EnumType.STRING)
    private SkillLevel level; // This can now represent a "suggested" or "default" level

    private boolean visible = true; // Can be used by admins to hide a skill from suggestions

    private String category;

    private String icon;

    @Lob
    private String description; // A global description of the skill

    @ManyToMany(mappedBy = "skills")
    @JsonBackReference("project-skills") // FIX: Added to break the serialization loop.
    private Set<Project> projects = new HashSet<>();

    @ManyToMany(mappedBy = "skills")
    @JsonBackReference("experience-skills") // FIX: Added to break the serialization loop.
    private Set<Experience> experiences = new HashSet<>();

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

}