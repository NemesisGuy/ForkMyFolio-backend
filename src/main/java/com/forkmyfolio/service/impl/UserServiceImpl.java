package com.forkmyfolio.service.impl;

import com.forkmyfolio.exception.DuplicateResourceException;
import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.model.enums.Role;
import com.forkmyfolio.model.User;
import com.forkmyfolio.model.enums.AuthProvider;
import com.forkmyfolio.repository.UserRepository;
import com.forkmyfolio.service.UserService;
import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Slugify slugify;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional
    public User registerUser(String email, String password, String firstName, String lastName, String profileImageUrl, Set<Role> roles, Boolean active) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email is already in use: " + email);
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setProfileImageUrl(profileImageUrl);
        user.setSlug(slugify.slugify(firstName + " " + lastName));
        user.setProvider(AuthProvider.LOCAL); // Set default provider for local registration

        if (roles == null || roles.isEmpty()) {
            user.setRoles(Set.of(Role.USER));
        } else {
            user.setRoles(roles);
        }

        user.setActive(active != null ? active : true);

        log.info("Registering new user with email: {}", email);
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentAuthenticatedUserWithAllPortfolioData() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }
        return userRepository.findByEmailWithAllPortfolioData(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional
    public User updateUserProfile(String firstName, String lastName, String profileImageUrl) {
        User currentUser = getCurrentAuthenticatedUser();
        currentUser.setFirstName(firstName);
        currentUser.setLastName(lastName);
        currentUser.setProfileImageUrl(profileImageUrl);
        log.info("Updating profile for user: {}", currentUser.getEmail());
        return userRepository.save(currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsersWithPortfolioData() {
        return userRepository.findAllWithPortfolioData();
    }

    @Override
    @Transactional
    public User updateUserByAdmin(UUID uuid, String firstName, String lastName, String slug, Set<Role> roles, Boolean active) {
        User user = getUserByUuid(uuid);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setSlug(slug);
        user.setRoles(roles);
        user.setActive(active);
        log.info("Admin updated user with UUID: {}", uuid);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deactivateUser(UUID uuid) {
        User user = getUserByUuid(uuid);
        user.setActive(false);
        log.warn("Deactivating user with UUID: {}", uuid);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findBySlug(String slug) {
        return userRepository.findBySlugAndActiveTrue(slug);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findBySlugWithAllPortfolioData(String slug) {
        return userRepository.findBySlugWithAllPortfolioData(slug);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByUuid(UUID uuid) {
        return userRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User with UUID: " + uuid));
    }

    @Override
    @Transactional
    public void createOrUpdateAdminUser(String email, String password, String firstName, String lastName) {
        Optional<User> existingAdmin = userRepository.findByEmail(email);

        if (existingAdmin.isPresent()) {
            User admin = existingAdmin.get();
            log.info("Admin user '{}' already exists. Ensuring roles are correct.", email);
            admin.getRoles().add(Role.ADMIN);
            admin.getRoles().add(Role.USER);
            userRepository.save(admin);
        } else {
            log.info("Admin user '{}' not found. Creating new admin user.", email);
            User admin = new User();
            admin.setEmail(email);
            admin.setPassword(passwordEncoder.encode(password));
            admin.setFirstName(firstName);
            admin.setLastName(lastName);
            admin.setSlug(slugify.slugify(firstName + " " + lastName));
            admin.setRoles(new HashSet<>(Set.of(Role.ADMIN, Role.USER)));
            admin.setActive(true);
            admin.setProvider(AuthProvider.LOCAL); // Set provider for admin creation
            userRepository.save(admin);
        }
    }
}