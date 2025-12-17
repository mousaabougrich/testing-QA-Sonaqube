package com.wallet.biochain.mappers;

import com.wallet.biochain.dto.WalletDTO;
import com.wallet.biochain.dto.WalletCreateResponseDTO;
import com.wallet.biochain.entities.Wallet;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class WalletMapper {

    public WalletDTO toDTO(Wallet wallet) {
        if (wallet == null) {
            return null;
        }

        return new WalletDTO(
                wallet.getId(),
                wallet.getAddress(),
                wallet.getPublicKey(),
                wallet.getBalance(),
                wallet.getIsActive(),
                wallet.getCreatedAt(),
                wallet.getUser() != null ? wallet.getUser().getUsername() : null
        );
    }

    public List<WalletDTO> toDTOList(List<Wallet> wallets) {
        if (wallets == null) {
            return null;
        }

        return wallets.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public WalletCreateResponseDTO toCreateResponseDTO(Wallet wallet, String privateKey) {
        if (wallet == null) {
            return null;
        }

        return new WalletCreateResponseDTO(
                wallet.getId(),
                wallet.getAddress(),
                wallet.getPublicKey(),
                privateKey,
                wallet.getBalance(),
                wallet.getCreatedAt(),
                "Wallet created successfully"
        );
    }
}