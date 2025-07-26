package com.forkmyfolio.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "settings", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
})
@Getter
@Setter
@NoArgsConstructor
public class Setting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @UuidGenerator
    @Column(name = "uuid", nullable = false, updatable = false, unique = true)
    private UUID uuid;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, length = 255)
    private String value;

    @Column(length = 512)
    private String description;

    public Setting(String name, String value, String description) {
        this.name = name;
        this.value = value;
        this.description = description;
    }
}