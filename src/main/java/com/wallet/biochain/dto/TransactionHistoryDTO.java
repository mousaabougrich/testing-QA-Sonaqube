package com.wallet.biochain.dto;

import com.wallet.biochain.enums.TransactionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionHistoryDTO(
        String transactionHash,
        String senderAddress,
        String recipientAddress,
        BigDecimal amount,
        BigDecimal fee,
        TransactionStatus status,
        Integer confirmationCount,
        String type, // "SENT" or "RECEIVED"
        LocalDateTime timestamp,
        Integer blockIndex,
        String memo
) {}