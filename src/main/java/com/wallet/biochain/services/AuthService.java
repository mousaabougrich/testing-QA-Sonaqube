package com.wallet.biochain.services;

import com.wallet.biochain.dto.AuthResponseDTO;
import com.wallet.biochain.dto.LoginRequestDTO;
import com.wallet.biochain.dto.RegisterRequestDTO;

public interface AuthService {

    /**
     * Register a new user
     */
    AuthResponseDTO register(RegisterRequestDTO registerRequest);

    /**
     * Authenticate user and return JWT token
     */
    AuthResponseDTO login(LoginRequestDTO loginRequest);
}

