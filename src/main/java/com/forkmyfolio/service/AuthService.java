package com.forkmyfolio.service;

import com.forkmyfolio.dto.request.RegisterRequest;
import com.forkmyfolio.model.User;

public interface AuthService {
    /**
     * Registers a new user in the system.
     */
    User registerUser(RegisterRequest registerRequest);
}