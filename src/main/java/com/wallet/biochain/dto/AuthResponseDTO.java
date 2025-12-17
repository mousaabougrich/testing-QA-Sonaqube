package com.wallet.biochain.dto;

import java.util.Set;

public record AuthResponseDTO(
        String token,
        String type,
        Long id,
        String username,
        String email,
        String fullName,
        Set<String> roles
) {}

