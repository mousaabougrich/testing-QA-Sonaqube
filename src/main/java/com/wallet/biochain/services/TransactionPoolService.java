package com.wallet.biochain.services;

import com.wallet.biochain.dto.TransactionPoolStatusDTO;
import com.wallet.biochain.entities.Transaction;
import com.wallet.biochain.entities.TransactionPool;

import java.util.List;
import java.util.Optional;

public interface TransactionPoolService {

    /**
     * Create transaction pool
     */
    TransactionPool createPool(String poolName, Integer maxSize);

    /**
     * Get pool by ID
     */
    Optional<TransactionPool> getPoolById(Long id);

    /**
     * Get pool by name
     */
    Optional<TransactionPool> getPoolByName(String poolName);

    /**
     * Get pool status
     */
    TransactionPoolStatusDTO getPoolStatus(String poolName);

    /**
     * Add transaction to pool
     */
    void addTransaction(String poolName, Transaction transaction);

    /**
     * Remove transaction from pool
     */
    void removeTransaction(String poolName, String transactionHash);

    /**
     * Get pending transactions from pool
     */
    List<Transaction> getPendingTransactions(String poolName);

    /**
     * Get top transactions by fee
     */
    List<Transaction> getTopTransactionsByFee(String poolName, Integer limit);

    /**
     * Clear pool
     */
    void clearPool(String poolName);

    /**
     * Check if pool is full
     */
    boolean isPoolFull(String poolName);

    /**
     * Get available pools
     */
    List<TransactionPool> getAvailablePools();
}