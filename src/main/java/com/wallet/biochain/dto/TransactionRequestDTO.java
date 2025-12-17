package com.wallet.biochain.dto;

import java.math.BigDecimal;

public record TransactionRequestDTO(
        String senderAddress,
        String recipientAddress,
        BigDecimal amount,
        BigDecimal fee,
        String memo,
        String privateKey
) {}