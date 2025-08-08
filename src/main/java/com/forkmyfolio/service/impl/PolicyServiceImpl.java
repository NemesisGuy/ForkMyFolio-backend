package com.forkmyfolio.service.impl;

import com.forkmyfolio.dto.response.PolicyDto;
import com.forkmyfolio.service.PolicyService;
import org.springframework.stereotype.Service;

@Service
public class PolicyServiceImpl implements PolicyService {

    // In a real application, this would come from a database, a file, or a CMS.
    // For now, we define it here. The version should be updated when the policy changes.
    public static final String CURRENT_TERMS_VERSION = "2025-08-08";

    @Override
    public PolicyDto getTermsOfService() {
        String content = """
                <h1>Terms of Service</h1>
                <p>Welcome to ForkMyFolio!</p>
                <p><strong>1. Acceptance of Terms</strong></p>
                <p>By creating an account, you agree to be bound by these Terms of Service...</p>
                <p><strong>2. User Accounts</strong></p>
                <p>You are responsible for maintaining the confidentiality of your account and password...</p>
                """;
        return new PolicyDto(CURRENT_TERMS_VERSION, "Terms of Service", content);
    }

    @Override
    public PolicyDto getPrivacyPolicy() {
        String content = """
                <h1>Privacy Policy</h1>
                <p>Your privacy is important to us.</p>
                <p><strong>1. Information We Collect</strong></p>
                <p>We collect information you provide directly to us, such as when you create an account...</p>
                <p>This includes your name, email address, and any portfolio content you upload.</p>
                <p><strong>2. How We Use Your Information</strong></p>
                <p>We use the information we collect to operate, maintain, and provide you with the features and functionality of the service...</p>
                """;
        // The privacy policy can share the same version date as the T&S if they are updated together.
        return new PolicyDto(CURRENT_TERMS_VERSION, "Privacy Policy", content);
    }
}