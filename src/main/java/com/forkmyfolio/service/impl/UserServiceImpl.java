package com.forkmyfolio.service.impl;

import com.forkmyfolio.exception.DuplicateResourceException;
import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.model.PortfolioProfile;
import com.forkmyfolio.model.Role;
import com.forkmyfolio.model.User;
import com.forkmyfolio.model.enums.AuthProvider;
import com.forkmyfolio.repository.PortfolioProfileRepository;
import com.forkmyfolio.repository.UserRepository;
import com.forkmyfolio.service.UserService;
import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final PortfolioProfileRepository portfolioProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final Slugify slugify;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Override
    public User getCurrentAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public User getUserByUuid(UUID uuid) {
        return userRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + uuid));
    }

    @Override
    @Transactional
    public User registerUser(String email, String password, String firstName, String lastName, String profileImageUrl, Set<Role> roles, Boolean active) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email address already in use.");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setProfileImageUrl(profileImageUrl);
        user.setProvider(AuthProvider.LOCAL);
        user.setSlug(generateUniqueSlug(firstName, lastName));
        user.setEmailVerified(true); // Or implement an email verification flow

        if (roles == null || roles.isEmpty()) {
            user.setRoles(Set.of(Role.USER));
        } else {
            user.setRoles(roles);
        }

        if (active != null) {
            user.setActive(active);
        } else {
            user.setActive(true);
        }

        User savedUser = userRepository.save(user);
        createPortfolioProfileForUser(savedUser);
        return savedUser;
    }

    @Override
    @Transactional
    public User updateUserByAdmin(UUID uuid, String firstName, String lastName, String slug, Set<Role> roles, Boolean active) {
        User user = getUserByUuid(uuid);

        if (firstName != null) user.setFirstName(firstName);
        if (lastName != null) user.setLastName(lastName);
        if (slug != null) {
            if (!slug.equals(user.getSlug()) && userRepository.existsBySlug(slug)) {
                throw new DuplicateResourceException("Slug already in use: " + slug);
            }
            user.setSlug(slug);
        }
        if (roles != null) user.setRoles(roles);
        if (active != null) user.setActive(active);

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deactivateUser(UUID uuid) {
        User user = getUserByUuid(uuid);
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public Optional<User> findBySlug(String slug) {
        return userRepository.findBySlug(slug);
    }

    @Override
    @Transactional
    public User updateUserProfile(String firstName, String lastName, String profileImageUrl) {
        User user = getCurrentAuthenticatedUser();

        if (firstName != null) user.setFirstName(firstName);
        if (lastName != null) user.setLastName(lastName);
        if (profileImageUrl != null) user.setProfileImageUrl(profileImageUrl);

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void createOrUpdateAdminUser(String email, String password, String firstName, String lastName) {
        Optional<User> existingAdminOpt = userRepository.findByEmail(email);

        User adminUser;
        if (existingAdminOpt.isEmpty()) {
            // Create new admin user
            adminUser = new User();
            adminUser.setEmail(email);
            adminUser.setSlug(generateUniqueSlug(firstName, lastName));
            adminUser.setProvider(AuthProvider.LOCAL);
            adminUser.setEmailVerified(true); // Admins are verified by default
        } else {
            // Admin user already exists, just update it
            adminUser = existingAdminOpt.get();
        }

        adminUser.setFirstName(firstName);
        adminUser.setLastName(lastName);
        adminUser.setPassword(passwordEncoder.encode(password)); // Always update password from config
        adminUser.setActive(true); // Ensure admin is always active

        Set<Role> roles = new HashSet<>();
        roles.add(Role.ADMIN);
        roles.add(Role.USER);
        adminUser.setRoles(roles);

        User savedAdmin = userRepository.save(adminUser);

        // Ensure the portfolio profile exists
        if (savedAdmin.getPortfolioProfile() == null) {
            createPortfolioProfileForUser(savedAdmin);
        }
    }

    private String generateUniqueSlug(String firstName, String lastName) {
        String baseSlug = slugify.slugify(firstName + " " + lastName);
        String finalSlug = baseSlug;
        int counter = 1;
        while (userRepository.existsBySlug(finalSlug)) {
            finalSlug = baseSlug + "-" + counter++;
        }
        return finalSlug;
    }

    private void createPortfolioProfileForUser(User user) {
        if (portfolioProfileRepository.findByUser(user).isEmpty()) {
            PortfolioProfile profile = new PortfolioProfile();
            profile.setUser(user);
            profile.setHeadline("Welcome to my portfolio!");
            portfolioProfileRepository.save(profile);
        }
    }
}