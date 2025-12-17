package com.wallet.biochain.dto;

import java.time.LocalDateTime;

public record TransactionPoolStatusDTO(
        Long id,
        String poolName,
        Integer currentSize,
        Integer maxSize,
        Double fillPercentage,
        Boolean isActive,
        Boolean isFull,
        LocalDateTime lastUpdated
) {}