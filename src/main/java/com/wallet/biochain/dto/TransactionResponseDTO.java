package com.wallet.biochain.dto;

import com.wallet.biochain.enums.TransactionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponseDTO(
        Long id,
        String transactionHash,
        String senderAddress,
        String recipientAddress,
        BigDecimal amount,
        BigDecimal fee,
        TransactionStatus status,
        Integer confirmationCount,
        Long timestamp,
        LocalDateTime createdAt,
        String memo
) {}