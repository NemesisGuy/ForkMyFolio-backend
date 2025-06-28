package com.forkmyfolio.service.impl;

import com.forkmyfolio.exception.EmailAlreadyExistsException;
import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.model.Role;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.UserRepository;
import com.forkmyfolio.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of the {@link UserService} interface.
 * Handles business logic related to users, including registration and retrieval.
 * This service operates solely on domain models (e.g., User) and is DTO-agnostic.
 */
@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user. The request data should be mapped to primitive types
     * or a domain model before calling this method.
     *
     * @param email The user's email.
     * @param password The user's raw password.
     * @param firstName The user's first name.
     * @param lastName The user's last name.
     * @param profileImageUrl The user's profile image URL.
     * @param roles The set of roles for the user.
     * @return The saved {@link User} entity.
     * @throws EmailAlreadyExistsException if the email is already in use.
     */
    @Transactional
    @Override
    public User registerUser(String email, String password, String firstName, String lastName, String profileImageUrl, Set<Role> roles) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Error: Email is already taken!");
        }

        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(passwordEncoder.encode(password));
        user.setProfileImageUrl(profileImageUrl);

        if (roles == null || roles.isEmpty()) {
            user.setRoles(Set.of(Role.USER)); // Default role
        } else {
            user.setRoles(new HashSet<>(roles));
        }

        return userRepository.save(user);
    }

    /**
     * Finds a user by their email address.
     *
     * @param email The email address to search for.
     * @return The {@link User} entity.
     * @throws UsernameNotFoundException if no user is found with the given email.
     */
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
                .orElseThrow(() -> new ResourceNotFoundException("User"));
    }

    /**
     * Checks if a user exists with the given email address.
     *
     * @param email The email to check.
     * @return True if a user exists, false otherwise.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Retrieves the portfolio owner's profile.
     *
     * @return The {@link User} entity of the portfolio owner.
     */
    @Override
    @Transactional(readOnly = true)
    public User getPublicProfile() {
        return userRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new ResourceNotFoundException("User"));
    }

    /**
     * Updates an existing user's profile information.
     *
     * @param userId The ID of the user to update.
     * @param firstName The new first name.
     * @param lastName The new last name.
     * @param profileImageUrl The new profile image URL.
     * @return The updated {@link User} entity.
     */
    @Transactional
    @Override
    public User updateUser(Long userId, String firstName, String lastName, String profileImageUrl) {
        User userToUpdate = getUserById(userId);

        userToUpdate.setFirstName(firstName);
        userToUpdate.setLastName(lastName);
        userToUpdate.setProfileImageUrl(profileImageUrl);

        return userRepository.save(userToUpdate);
    }

    /**
     * Loads a user by their username (email for this application).
     * This method is part of the {@link UserDetailsService} interface.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    /**
     * Retrieves the currently authenticated user from the Spring Security context.
     *
     * @return The authenticated {@link User} object.
     * @throws UsernameNotFoundException if no user is authenticated or the principal cannot be resolved to a User.
     */
    @Override
    @Transactional(readOnly = true)
    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UsernameNotFoundException("No authenticated user found in security context.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }

        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        return findByEmail(username);
    }
}