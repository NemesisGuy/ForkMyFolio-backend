package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.request.RegisterRequest;
import com.forkmyfolio.exception.EmailAlreadyExistsException;
import com.forkmyfolio.model.User;
import com.forkmyfolio.model.enums.AuthProvider;
import com.forkmyfolio.model.enums.Role;
import com.forkmyfolio.repository.UserRepository;
import com.forkmyfolio.service.AuthService;
import com.forkmyfolio.service.PolicyService;
import com.forkmyfolio.service.SlugService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SlugService slugService;
    private final PolicyService policyService;

    @Override
    @Transactional
    public User registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new EmailAlreadyExistsException("An account with this email address already exists.");
        }

        // Validate that the user is accepting the current terms version
        String currentTermsVersion = policyService.getTermsOfService().getVersion();
        if (!currentTermsVersion.equals(registerRequest.getTermsVersion())) {
            throw new IllegalArgumentException("You must accept the latest Terms of Service version: " + currentTermsVersion);
        }

        User user = new User();
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setProvider(AuthProvider.LOCAL);
        user.setRoles(Set.of(Role.USER));
        user.setActive(true);
        user.setEmailVerified(false); // Assuming an email verification step will follow

        // Generate a unique slug for the user's public portfolio
        // FIX: Concatenate first and last name to match the service method signature.
        String fullName = registerRequest.getFirstName() + " " + registerRequest.getLastName();
        user.setSlug(slugService.generateUniqueSlug(fullName));

        // Set POPIA compliance fields
        user.setTermsAcceptedAt(Instant.now());
        user.setTermsVersion(registerRequest.getTermsVersion());

        return userRepository.save(user);
    }
}