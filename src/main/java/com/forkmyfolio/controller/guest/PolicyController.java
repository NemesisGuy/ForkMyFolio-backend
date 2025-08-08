package com.forkmyfolio.controller.guest;

import com.forkmyfolio.dto.response.PolicyDto;
import com.forkmyfolio.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/policies")
@Tag(name = "Policies", description = "Endpoints for retrieving legal documents like Terms of Service and Privacy Policy.")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @GetMapping("/terms-of-service")
    @Operation(summary = "Get the current Terms of Service", description = "Retrieves the latest version of the Terms of Service.")
    public ResponseEntity<PolicyDto> getTermsOfService() {
        return ResponseEntity.ok(policyService.getTermsOfService());
    }

    @GetMapping("/privacy-policy")
    @Operation(summary = "Get the current Privacy Policy", description = "Retrieves the latest version of the Privacy Policy.")
    public ResponseEntity<PolicyDto> getPrivacyPolicy() {
        return ResponseEntity.ok(policyService.getPrivacyPolicy());
    }
}