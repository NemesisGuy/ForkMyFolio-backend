package com.forkmyfolio.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.forkmyfolio.model.enums.AuthProvider;
import com.forkmyfolio.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Represents a user in the ForkMyFolio system.
 * Implements Spring Security's UserDetails for authentication.
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "slug")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"portfolioProfile", "projects", "userSkills", "experiences", "qualifications", "testimonials", "contactMessages", "userSettings", "refreshTokens"})
@EqualsAndHashCode(of = "id")
public class User implements UserDetails {

    /**
     * Unique identifier for the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Universally unique identifier for the user.
     */
    @Column(name = "uuid", nullable = false, updatable = false, unique = true)
    private UUID uuid = UUID.randomUUID();

    /**
     * The unique, URL-friendly identifier for the user's public portfolio.
     */
    @Column(name = "slug", unique = true, nullable = false, length = 50)
    private String slug;

    /**
     * User's email address. Must be unique.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * User's first name.
     */
    @Column(nullable = false)
    private String firstName;

    /**
     * User's last name.
     */
    @Column(nullable = false)
    private String lastName;

    /**
     * Hashed password for the user. Can be null for OAuth2 users.
     */
    private String password;

    /**
     * URL to the user's profile image.
     */
    private String profileImageUrl;

    /**
     * Flag indicating if the user's account is active.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Flag indicating if the user's email has been verified.
     */
    @Column(nullable = false)
    private boolean emailVerified = false;

    /**
     * The authentication provider used to register this user (e.g., LOCAL, GOOGLE).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    /**
     * The unique identifier for the user from the external provider.
     */
    private String providerId;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference("user-profile")
    private PortfolioProfile portfolioProfile;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-projects")
    private Set<Project> projects = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-userskills")
    private Set<UserSkill> userSkills = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-experiences")
    private Set<Experience> experiences = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-qualifications")
    private Set<Qualification> qualifications = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-testimonials")
    private Set<Testimonial> testimonials = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-contactmessages")
    private Set<ContactMessage> contactMessages = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-usersettings")
    private Set<UserSetting> userSettings = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-refreshtokens")
    private Set<RefreshToken> refreshTokens = new HashSet<>();

    /**
     * Roles assigned to the user (e.g., ADMIN, USER).
     */
    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Set<Role> roles = new HashSet<>();

    /**
     * Timestamp of when the user account was created.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Timestamp of when the user account was last updated.
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    // UserDetails methods

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.active;
    }
}