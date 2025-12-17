package com.wallet.biochain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletCreateResponseDTO(
        Long id,
        String address,
        String publicKey,
        String privateKey, // Return only once during creation
        BigDecimal balance,
        LocalDateTime createdAt,
        String message
) {}