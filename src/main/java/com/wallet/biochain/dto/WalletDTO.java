package com.wallet.biochain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletDTO(
        Long id,
        String address,
        String publicKey,
        BigDecimal balance,
        Boolean isActive,
        LocalDateTime createdAt,
        String username
) {}