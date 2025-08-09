package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.request.RegisterRequest;
import com.forkmyfolio.exception.EmailAlreadyExistsException;
import com.forkmyfolio.model.ContactMessage;
import com.forkmyfolio.model.PortfolioProfile;
import com.forkmyfolio.model.User;
import com.forkmyfolio.model.enums.AuthProvider;
import com.forkmyfolio.model.enums.MessagePriority;
import com.forkmyfolio.model.enums.Role;
import com.forkmyfolio.repository.ContactMessageRepository;
import com.forkmyfolio.repository.PortfolioProfileRepository;
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
    private final PortfolioProfileRepository portfolioProfileRepository;
    private final ContactMessageRepository contactMessageRepository;

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

        User savedUser = userRepository.save(user);

        // After saving the user, create their default profile and send a welcome message.
        createDefaultProfileForUser(savedUser);
        sendWelcomeMessage(savedUser);

        return savedUser;
    }

    /**
     * Creates a default, public-facing portfolio profile for a new user.
     * This ensures that new users have a profile to edit immediately and that it's visible by default.
     *
     * @param user The newly registered user.
     */
    private void createDefaultProfileForUser(User user) {
        PortfolioProfile profile = new PortfolioProfile();
        profile.setUser(user);
        profile.setPublic(true); // Make the portfolio public by default.
        profile.setVisible(true); // Ensure the profile section itself is visible.
        profile.setHeadline("Welcome to Your New Portfolio!");
        profile.setSummary("This is your new portfolio summary. You can edit this text to tell visitors about yourself, your skills, and your professional goals. Make it engaging and unique!");
        portfolioProfileRepository.save(profile);
    }

    /**
     * Creates and saves a welcome message for a new user.
     * This message guides them on the next steps and informs them about the default public visibility.
     *
     * @param user The newly registered user.
     */
    private void sendWelcomeMessage(User user) {
        ContactMessage welcomeMessage = new ContactMessage();
        welcomeMessage.setUser(user);
        welcomeMessage.setName("The ForkMyFolio Team");
        welcomeMessage.setEmail("welcome@forkmyfolio.com");
        welcomeMessage.setMessage("Welcome to ForkMyFolio! We're excited to have you on board.\n\nYour account has been created successfully. Here are a few next steps to get your portfolio looking great:\n\n1.  **Complete Your Profile:** Navigate to the 'My Portfolio' sections in the dashboard to add your work experience, projects, skills, and more.\n2.  **Customize Your Look:** Check out the settings to choose a theme and personalize your public page.\n\n**Important Note:** Your portfolio is set to **public** by default so you can share it right away. If you're not ready for the world to see it yet, you can easily make it private. Just go to **Display Settings** and toggle the **'Portfolio is Public'** switch to the OFF position.\n\nWe can't wait to see what you create!\n\nBest,\nThe ForkMyFolio Team");
        welcomeMessage.setRead(false); // Mark as unread
        welcomeMessage.setArchived(false);
        welcomeMessage.setReplied(false);
        welcomeMessage.setPriority(MessagePriority.HIGH);
        contactMessageRepository.save(welcomeMessage);
    }
}