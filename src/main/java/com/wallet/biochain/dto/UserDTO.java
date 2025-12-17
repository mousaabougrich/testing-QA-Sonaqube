package com.wallet.biochain.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record UserDTO(
        Long id,
        String username,
        String email,
        String fullName,
        String phoneNumber,
        Boolean isActive,
        Integer walletCount,
        LocalDateTime createdAt,
        Set<String> roles
) {}