package com.wallet.biochain.dto;

import com.wallet.biochain.enums.ConsensusType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BlockchainStatusDTO(
        String chainId,
        String name,
        Integer currentHeight,
        String latestBlockHash,
        Integer difficulty,
        BigDecimal blockReward,
        ConsensusType consensusType,
        Integer totalTransactions,
        Integer pendingTransactions,
        Boolean isValid,
        Integer averageBlockTime,
        LocalDateTime lastBlockTime
) {}