package com.forkmyfolio.model;

// This would be in a new file: com.forkmyfolio.validator.ValidDateRange
// For now, we just reference it. The next step will be to create it.
// import com.forkmyfolio.validator.ValidDateRange;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@NamedEntityGraph(
        name = "Experience.withSkills",
        attributeNodes = @NamedAttributeNode("skills")
)
@Entity
@Table(name = "experiences")
@Getter
@Setter
@NoArgsConstructor
// @ValidDateRange(startDate = "startDate", endDate = "endDate", message = "End date must be after or equal to start date.")
public class Experience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @UuidGenerator
    @Column(name = "uuid", nullable = false, updatable = false, unique = true)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private boolean visible = true;

    @NotBlank
    @Size(max = 100)
    private String jobTitle;

    @NotBlank
    @Size(max = 100)
    private String companyName;

    @Size(max = 255)
    @Column(name = "company_url")
    private String companyUrl;

    @Size(max = 255)
    @Column(name = "company_logo_url")
    private String companyLogoUrl;

    @Size(max = 100)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type")
    private LocationType locationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type")
    private EmploymentType employmentType;

    @NotNull
    private LocalDate startDate;

    /**
     * If null, this signifies that this is the user's current position.
     */
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String achievements;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "experience_skills",
            joinColumns = @JoinColumn(name = "experience_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills = new HashSet<>();

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public enum EmploymentType {
        FULL_TIME,
        PART_TIME,
        CONTRACT,
        FREELANCE,
        INTERNSHIP,
        APPRENTICESHIP
    }

    public enum LocationType {
        ON_SITE,
        HYBRID,
        REMOTE
    }
}