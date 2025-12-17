package com.wallet.biochain.dto;

import java.math.BigDecimal;

public record BalanceDTO(
        String walletAddress,
        BigDecimal balance,
        BigDecimal pendingBalance,
        BigDecimal totalBalance,
        String currency
) {}