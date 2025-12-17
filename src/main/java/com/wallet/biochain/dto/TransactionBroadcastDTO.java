package com.wallet.biochain.dto;

import java.math.BigDecimal;

public record TransactionBroadcastDTO(
        String transactionHash,
        String senderAddress,
        String recipientAddress,
        BigDecimal amount,
        String signature,
        Long timestamp
) {}