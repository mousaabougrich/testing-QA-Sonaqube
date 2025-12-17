package com.wallet.biochain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequestDTO(
        @NotBlank(message = "Username is required") String username,
        @NotBlank(message = "Email is required") @Email(message = "Email should be valid") String email,
        @NotBlank(message = "Password is required") String password,
        String fullName,
        String phoneNumber
) {}

