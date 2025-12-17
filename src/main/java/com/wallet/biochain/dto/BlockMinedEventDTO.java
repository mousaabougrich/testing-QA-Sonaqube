package com.wallet.biochain.dto;

import java.math.BigDecimal;

public record BlockMinedEventDTO(
        Integer blockIndex,
        String blockHash,
        String minerAddress,
        BigDecimal reward,
        Integer transactionCount,
        Long timestamp
) {}