package com.forkmyfolio.model;

import com.forkmyfolio.model.enums.AuthProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a user in the ForkMyFolio system.
 * Implements Spring Security's UserDetails for authentication.
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "slug") // Added unique constraint for slug
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "uuid", nullable = false, updatable = false, unique = true)
    private UUID uuid;

    /**
     * The unique, URL-friendly identifier for the user's public portfolio.
     */
    @Column(name = "slug", unique = true, nullable = false, length = 50)
    private String slug;

    /**
     * User's email address. Must be unique and a valid email format.
     */
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * User's first name.
     */
    @NotBlank(message = "First name cannot be blank")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    /**
     * User's last name.
     */
    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    /**
     * Hashed password for the user. Can be null for OAuth2 users.
     */
    @Size(min = 8, message = "Password must be at least 8 characters long") // Validation for length before hashing
    private String password;

    /**
     * URL to the user's portfolioProfile image.
     */
    private String profileImageUrl;

    /**
     * Flag indicating if the user's account is active. Can be used for soft deletes or email verification.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Flag indicating if the user's email has been verified.
     * OAuth2 providers are assumed to have verified emails.
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
     * This will be null for users with LOCAL provider.
     */
    private String providerId;


    // 'mappedBy = "user"' tells Hibernate that the other entity owns the relationship
    // (i.e., the `user_id` foreign key is in the other table).
    // 'cascade = CascadeType.ALL' means if you delete a User, their associated entities are also deleted.
    // 'orphanRemoval = true' handles cases where the link is severed.
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PortfolioProfile portfolioProfile;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Project> projects = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Skill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Experience> experiences = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Qualification> qualifications = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Testimonial> testimonials = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContactMessage> contactMessages = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserSetting> userSettings = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    /**
     * Roles assigned to the user (e.g., ADMIN, USER).
     * Stored as a set of Role enums.
     */
    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Set<Role> roles = new HashSet<>();

    /**
     * Timestamp of when the user account was created.
     * Automatically set by Hibernate.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of when the user account was last updated.
     * Automatically set by Hibernate on update.
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // UserDetails methods

    /**
     * Returns the authorities granted to the user.
     * Converts roles to Spring Security's GrantedAuthority.
     *
     * @return A collection of granted authorities.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());
    }

    /**
     * Returns the username used to authenticate the user. In this case, it's the email.
     *
     * @return The user's email.
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Indicates whether the user's account has expired.
     *
     * @return true if the account is valid (not expired), false otherwise.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true; // Account expiration not implemented
    }

    /**
     * Indicates whether the user is locked or unlocked.
     *
     * @return true if the user is not locked, false otherwise.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true; // Account locking not implemented
    }

    /**
     * Indicates whether the user's credentials (password) has expired.
     *
     * @return true if the credentials are valid (not expired), false otherwise.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Credentials expiration not implemented
    }

    /**
     * Indicates whether the user is enabled or disabled.
     *
     * @return true if the user is enabled, false otherwise.
     */
    @Override
    public boolean isEnabled() {
        return this.active; // Use the active flag to enable/disable users
    }
}
