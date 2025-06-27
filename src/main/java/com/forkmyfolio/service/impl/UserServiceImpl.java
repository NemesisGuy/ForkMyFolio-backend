package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.RegisterRequest;
import com.forkmyfolio.dto.UserDto;
import com.forkmyfolio.exception.EmailAlreadyExistsException;
import com.forkmyfolio.model.Role;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.UserRepository;
import com.forkmyfolio.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of the {@link UserService} interface.
 * Handles business logic related to users, including registration and retrieval.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs a {@code UserServiceImpl} with necessary dependencies.
     *
     * @param userRepository  The repository for accessing user data.
     * @param passwordEncoder The encoder for hashing passwords.
     */
    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user in the system.
     * Hashes the password and assigns default roles if none are provided.
     *
     * @param registerRequest DTO containing registration details.
     * @return The saved {@link User} entity.
     * @throws EmailAlreadyExistsException if the email is already in use.
     */
    @Override
    @Transactional
    public User registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Error: Email is already taken!");
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setProfileImageUrl(registerRequest.getProfileImageUrl());

        Set<Role> roles = new HashSet<>();
        if (registerRequest.getRoles() == null || registerRequest.getRoles().isEmpty()) {
            roles.add(Role.USER); // Default role
        } else {
            roles.addAll(registerRequest.getRoles());
        }
        user.setRoles(roles);

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
     * Converts a {@link User} entity to a {@link UserDto}.
     *
     * @param user The user entity to convert.
     * @return The corresponding DTO.
     */
    @Override
    public UserDto convertToDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserDto(
                user.getUuid(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getProfileImageUrl(),
                user.getRoles(),
                user.getCreatedAt()
        );
    }

    /**
     * Loads a user by their username (email for this application).
     * This method is part of the {@link org.springframework.security.core.userdetails.UserDetailsService} interface.
     *
     * @param email The email (username) of the user to load.
     * @return UserDetails object.
     * @throws UsernameNotFoundException if the user is not found.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email : " + email));
    }

    /**
     * Retrieves the currently authenticated user from the Spring Security context.
     *
     * @return The authenticated {@link User} object.
     * @throws UsernameNotFoundException if no user is authenticated or the principal is not a User instance.
     */
    @Override
    @Transactional(readOnly = true)
    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new UsernameNotFoundException("No authenticated user found in security context.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            // If principal is already our User object (e.g. after our JwtAuthenticationFilter)
            return (User) principal;
        } else if (principal instanceof UserDetails) {
            // If principal is a UserDetails object, try to fetch our User entity by username
            String username = ((UserDetails) principal).getUsername();
            return findByEmail(username);
        } else {
            // If principal is just a String (e.g. username), try to fetch our User entity
            return findByEmail(principal.toString());
        }
    }
}
