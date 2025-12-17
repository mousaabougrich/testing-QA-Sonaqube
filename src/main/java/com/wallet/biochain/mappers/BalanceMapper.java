package com.wallet.biochain.mappers;

import com.wallet.biochain.dto.BalanceDTO;
import com.wallet.biochain.entities.Wallet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BalanceMapper {

    public BalanceDTO toDTO(Wallet wallet, BigDecimal pendingBalance) {
        if (wallet == null) {
            return null;
        }

        BigDecimal pending = pendingBalance != null ? pendingBalance : BigDecimal.ZERO;
        BigDecimal total = wallet.getBalance().add(pending);

        return new BalanceDTO(
                wallet.getAddress(),
                wallet.getBalance(),
                pending,
                total,
                "BTC" // or make this configurable
        );
    }

    public BalanceDTO toDTO(String walletAddress, BigDecimal balance, BigDecimal pendingBalance) {
        if (walletAddress == null) {
            return null;
        }

        BigDecimal pending = pendingBalance != null ? pendingBalance : BigDecimal.ZERO;
        BigDecimal confirmedBalance = balance != null ? balance : BigDecimal.ZERO;
        BigDecimal total = confirmedBalance.add(pending);

        return new BalanceDTO(
                walletAddress,
                confirmedBalance,
                pending,
                total,
                "BTC"
        );
    }
}