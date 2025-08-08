package com.forkmyfolio.service;

import com.forkmyfolio.dto.response.PolicyDto;

/**
 * Service for retrieving legal policy documents.
 */
public interface PolicyService {

    PolicyDto getTermsOfService();

    PolicyDto getPrivacyPolicy();
}