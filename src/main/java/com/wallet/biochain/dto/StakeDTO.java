package com.wallet.biochain.dto;

import com.wallet.biochain.enums.StakeStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StakeDTO(
        Long id,
        String walletAddress,
        BigDecimal stakedAmount,
        BigDecimal rewardsEarned,
        StakeStatus status,
        LocalDateTime lockedUntil,
        Double stakeWeight,
        LocalDateTime createdAt,
        Boolean isLocked
) {}