package com.forkmyfolio.service.impl;

import com.forkmyfolio.exception.EmailAlreadyExistsException;
import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.model.PortfolioProfile;
import com.forkmyfolio.model.Role;
import com.forkmyfolio.model.User;
import com.forkmyfolio.model.enums.AuthProvider;
import com.forkmyfolio.repository.UserRepository;
import com.forkmyfolio.service.SlugService;
import com.forkmyfolio.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
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

/**
 * Implementation of the {@link UserService} interface.
 * Handles business logic related to users, including registration and retrieval.
 * This service operates solely on domain models (e.g., User) and is DTO-agnostic.
 */
@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SlugService slugService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, SlugService slugService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.slugService = slugService;
    }

    @Transactional
    @Override
    public User registerUser(String email, String password, String firstName, String lastName, String profileImageUrl, Set<Role> roles, Boolean active) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Error: Email is already taken!");
        }

        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(passwordEncoder.encode(password));
        user.setProfileImageUrl(profileImageUrl);
        user.setProvider(AuthProvider.LOCAL);
        user.setActive(active != null ? active : true); // Default to active if not specified

        String baseNameForSlug = firstName + " " + lastName;
        String uniqueSlug = slugService.generateUniqueSlug(baseNameForSlug);
        user.setSlug(uniqueSlug);

        if (roles == null || roles.isEmpty()) {
            user.setRoles(Set.of(Role.USER));
        } else {
            user.setRoles(new HashSet<>(roles));
        }

        PortfolioProfile profile = new PortfolioProfile();
        profile.setUser(user);
        profile.setHeadline("Welcome to my portfolio!");
        profile.setSummary("I'm excited to share my work with you.");
        user.setPortfolioProfile(profile);

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + userId + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public User getPublicProfile() {
        return userRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new ResourceNotFoundException("User"));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UsernameNotFoundException("No authenticated user found in security context.");
        }

        Object principal = authentication.getPrincipal();
        String username;

        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        return findByEmail(username);
    }

    @Override
    @Transactional
    public User updateUserProfile(Long userId, String firstName, String lastName, String profileImageUrl) {
        User userToUpdate = getUserById(userId);
        userToUpdate.setFirstName(firstName);
        userToUpdate.setLastName(lastName);
        userToUpdate.setProfileImageUrl(profileImageUrl);
        return userRepository.save(userToUpdate);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public User updateUserByAdmin(UUID userId, String firstName, String lastName, String slug, Set<Role> roles, Boolean active) {
        User userToUpdate = getUserByUuid(userId);
        userToUpdate.setFirstName(firstName);
        userToUpdate.setLastName(lastName);
        userToUpdate.setSlug(slug); // Admins can edit slugs
        userToUpdate.setRoles(roles);
        userToUpdate.setActive(active);
        return userRepository.save(userToUpdate);
    }

    @Override
    @Transactional
    public void deactivateUser(UUID userId) {
        User userToDeactivate = getUserByUuid(userId);
        userToDeactivate.setActive(false);
        userRepository.save(userToDeactivate);
    }

    @Override
    public Optional<User> findBySlug(String slug) {
        return userRepository.findBySlugAndActiveTrue(slug);
    }

    @Override
    public User getUserByUuid(UUID userId) {
        return userRepository.findByUuid(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with UUID " + userId + " not found"));
    }
}