package com.wallet.biochain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MiningResultDTO(
        Boolean success,
        String blockHash,
        Integer blockIndex,
        Integer nonce,
        Long miningDuration,
        Integer difficulty,
        String minerAddress,
        BigDecimal reward,
        Integer transactionCount,
        LocalDateTime timestamp,
        String message
) {}