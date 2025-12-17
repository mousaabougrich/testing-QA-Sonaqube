package com.wallet.biochain.services;

import com.wallet.biochain.dto.WalletDTO;
import com.wallet.biochain.dto.WalletCreateRequestDTO;
import com.wallet.biochain.dto.WalletCreateResponseDTO;
import com.wallet.biochain.dto.BalanceDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface WalletService {

    /**
     * Create a new wallet for a user
     */
    WalletCreateResponseDTO createWallet(WalletCreateRequestDTO request);

    /**
     * Get wallet by ID
     */
    Optional<WalletDTO> getWalletById(Long id);

    /**
     * Get wallet by address
     */
    Optional<WalletDTO> getWalletByAddress(String address);

    /**
     * Get all wallets for a user
     */
    List<WalletDTO> getWalletsByUserId(Long userId);

    /**
     * Get active wallets for a user
     */
    List<WalletDTO> getActiveWalletsByUserId(Long userId);

    /**
     * Get wallet balance
     */
    BalanceDTO getBalance(String walletAddress);

    /**
     * Update wallet balance
     */
    void updateBalance(String walletAddress, BigDecimal newBalance);

    /**
     * Add amount to wallet balance
     */
    void addToBalance(String walletAddress, BigDecimal amount);

    /**
     * Subtract amount from wallet balance
     */
    void subtractFromBalance(String walletAddress, BigDecimal amount);

    /**
     * Deactivate wallet
     */
    void deactivateWallet(String walletAddress);

    /**
     * Activate wallet
     */
    void activateWallet(String walletAddress);

    /**
     * Get total balance across all active wallets
     */
    BigDecimal getTotalBalance();

    /**
     * Export wallet (backup private key)
     */
    String exportWallet(String walletAddress, String userPassword);

    /**
     * Import wallet (restore from private key)
     */
    WalletDTO importWallet(Long userId, String privateKey, String userPassword);

    /**
     * Validate wallet address format
     */
    boolean isValidAddress(String address);

    /**
     * Check if wallet exists
     */
    boolean walletExists(String address);
}