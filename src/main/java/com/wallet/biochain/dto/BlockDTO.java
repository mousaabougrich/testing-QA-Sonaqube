package com.wallet.biochain.dto;

import java.time.LocalDateTime;
import java.util.List;

public record BlockDTO(
        Long id,
        Integer blockIndex,
        String hash,
        String previousHash,
        Long timestamp,
        Integer nonce,
        Integer difficulty,
        String merkleRoot,
        String minerAddress,
        Integer transactionCount,
        List<TransactionResponseDTO> transactions,
        LocalDateTime createdAt
) {}