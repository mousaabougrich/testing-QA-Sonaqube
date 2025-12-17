package com.wallet.biochain.dto;

public record WalletCreateRequestDTO(
        Long userId,
        String walletName
) {}