package com.wallet.biochain.services;

import com.wallet.biochain.dto.TransactionRequestDTO;
import com.wallet.biochain.dto.TransactionResponseDTO;
import com.wallet.biochain.dto.TransactionHistoryDTO;
import com.wallet.biochain.entities.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TransactionService {

    /**
     * Create and sign a new transaction
     */
    TransactionResponseDTO createTransaction(TransactionRequestDTO request);

    /**
     * Get transaction by hash
     */
    Optional<TransactionResponseDTO> getTransactionByHash(String transactionHash);

    /**
     * Get transaction history for a wallet
     */
    List<TransactionHistoryDTO> getTransactionHistory(String walletAddress);

    /**
     * Get sent transactions for a wallet
     */
    List<TransactionResponseDTO> getSentTransactions(String walletAddress);

    /**
     * Get received transactions for a wallet
     */
    List<TransactionResponseDTO> getReceivedTransactions(String walletAddress);

    /**
     * Get pending transactions
     */
    List<TransactionResponseDTO> getPendingTransactions();

    /**
     * Get confirmed transactions
     */
    List<TransactionResponseDTO> getConfirmedTransactions();

    /**
     * Validate transaction
     */
    boolean validateTransaction(Transaction transaction);

    /**
     * Sign transaction
     */
    String signTransaction(Transaction transaction, String privateKey);

    /**
     * Verify transaction signature
     */
    boolean verifyTransactionSignature(Transaction transaction);

    /**
     * Broadcast transaction to network
     */
    void broadcastTransaction(Transaction transaction);

    /**
     * Confirm transaction (add to block)
     */
    void confirmTransaction(String transactionHash, Long blockId);

    /**
     * Get transaction count for wallet
     */
    Long getTransactionCount(String walletAddress);

    /**
     * Get total transaction volume
     */
    BigDecimal getTotalTransactionVolume();

    /**
     * Calculate transaction fee
     */
    BigDecimal calculateTransactionFee(BigDecimal amount);
}