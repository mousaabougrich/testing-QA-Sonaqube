package com.wallet.biochain.services.impl;

import com.wallet.biochain.dto.TransactionPoolStatusDTO;
import com.wallet.biochain.entities.Transaction;
import com.wallet.biochain.entities.TransactionPool;
import com.wallet.biochain.enums.TransactionStatus;
import com.wallet.biochain.mappers.TransactionPoolMapper;
import com.wallet.biochain.repositories.TransactionPoolRepository;
import com.wallet.biochain.repositories.TransactionRepository;
import com.wallet.biochain.services.TransactionPoolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionPoolServiceImpl implements TransactionPoolService {

    private final TransactionPoolRepository poolRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionPoolMapper poolMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public TransactionPool createPool(String poolName, Integer maxSize) {
        log.info("Creating transaction pool: {} with max size: {}", poolName, maxSize);

        // Check if pool already exists
        if (poolRepository.existsByPoolName(poolName)) {
            throw new IllegalArgumentException("Transaction pool already exists: " + poolName);
        }

        // Validate max size
        if (maxSize == null || maxSize <= 0) {
            throw new IllegalArgumentException("Max size must be greater than 0");
        }

        // Create pool
        TransactionPool pool = new TransactionPool(poolName);
        pool.setMaxSize(maxSize);
        pool.setIsActive(true);

        TransactionPool savedPool = poolRepository.save(pool);
        log.info("Transaction pool created successfully: {}", poolName);

        // Broadcast pool creation event
        try {
            messagingTemplate.convertAndSend("/topic/pools/created",
                    poolMapper.toStatusDTO(savedPool));
        } catch (Exception e) {
            log.error("Failed to broadcast pool creation event", e);
        }

        return savedPool;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TransactionPool> getPoolById(Long id) {
        log.debug("Fetching transaction pool by ID: {}", id);
        return poolRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TransactionPool> getPoolByName(String poolName) {
        log.debug("Fetching transaction pool by name: {}", poolName);
        return poolRepository.findByPoolName(poolName);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionPoolStatusDTO getPoolStatus(String poolName) {
        log.debug("Fetching pool status for: {}", poolName);

        TransactionPool pool = poolRepository.findByPoolName(poolName)
                .orElseThrow(() -> new IllegalArgumentException("Pool not found: " + poolName));

        return poolMapper.toStatusDTO(pool);
    }

    @Override
    @Transactional
    public void addTransaction(String poolName, Transaction transaction) {
        // Validate transaction
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }
        log.debug("Adding transaction {} to pool: {}", transaction.getTransactionHash(), poolName);

        if (transaction.getTransactionHash() == null || transaction.getTransactionHash().isEmpty()) {
            throw new IllegalArgumentException("Transaction hash is required");
        }

        // Get pool
        TransactionPool pool = poolRepository.findByPoolName(poolName)
                .orElseThrow(() -> new IllegalArgumentException("Pool not found: " + poolName));

        // Check if pool is active
        if (!pool.getIsActive()) {
            throw new IllegalStateException("Pool is not active: " + poolName);
        }

        // Check if pool is full
        if (pool.isFull()) {
            throw new IllegalStateException("Transaction pool is full. Current size: " +
                    pool.getCurrentSize() + ", Max size: " + pool.getMaxSize());
        }

        // Check if transaction already exists in pool
        boolean alreadyInPool = pool.getPendingTransactions().stream()
                .anyMatch(tx -> tx.getTransactionHash().equals(transaction.getTransactionHash()));

        if (alreadyInPool) {
            log.warn("Transaction {} already in pool", transaction.getTransactionHash());
            return;
        }

        // Set transaction status to PENDING if not already
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            transaction.setStatus(TransactionStatus.PENDING);
        }

        // Link transaction to pool
        transaction.setTransactionPool(pool);
        transactionRepository.save(transaction);

        // Update pool size
        pool.setCurrentSize(pool.getCurrentSize() + 1);
        poolRepository.save(pool);

        log.info("Transaction {} added to pool: {}", transaction.getTransactionHash(), poolName);

        // Broadcast pool update
        try {
            messagingTemplate.convertAndSend("/topic/pools/updated",
                    poolMapper.toStatusDTO(pool));
        } catch (Exception e) {
            log.error("Failed to broadcast pool update", e);
        }
    }

    @Override
    @Transactional
    public void removeTransaction(String poolName, String transactionHash) {
        log.debug("Removing transaction {} from pool: {}", transactionHash, poolName);

        // Validate inputs
        if (transactionHash == null || transactionHash.isEmpty()) {
            throw new IllegalArgumentException("Transaction hash is required");
        }

        // Get pool
        TransactionPool pool = poolRepository.findByPoolName(poolName)
                .orElseThrow(() -> new IllegalArgumentException("Pool not found: " + poolName));

        // Find transaction
        Transaction transaction = transactionRepository.findByTransactionHash(transactionHash)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionHash));

        // Check if transaction is in this pool
        if (transaction.getTransactionPool() == null ||
                !transaction.getTransactionPool().getId().equals(pool.getId())) {
            log.warn("Transaction {} is not in pool: {}", transactionHash, poolName);
            return;
        }

        // Remove transaction from pool
        transaction.setTransactionPool(null);
        transactionRepository.save(transaction);

        // Update pool size
        pool.setCurrentSize(Math.max(0, pool.getCurrentSize() - 1));
        poolRepository.save(pool);

        log.info("Transaction {} removed from pool: {}", transactionHash, poolName);

        // Broadcast pool update
        try {
            messagingTemplate.convertAndSend("/topic/pools/updated",
                    poolMapper.toStatusDTO(pool));
        } catch (Exception e) {
            log.error("Failed to broadcast pool update", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getPendingTransactions(String poolName) {
        log.debug("Fetching pending transactions from pool: {}", poolName);

        TransactionPool pool = poolRepository.findByPoolName(poolName)
                .orElseThrow(() -> new IllegalArgumentException("Pool not found: " + poolName));

        List<Transaction> pending = pool.getPendingTransactions();
        log.debug("Found {} pending transactions in pool: {}", pending.size(), poolName);

        return pending;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getTopTransactionsByFee(String poolName, Integer limit) {
        log.debug("Fetching top {} transactions by fee from pool: {}", limit, poolName);

        if (limit == null || limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than 0");
        }

        List<Transaction> pending = getPendingTransactions(poolName);

        // Sort by fee (descending) and timestamp (ascending for same fee)
        List<Transaction> topTransactions = pending.stream()
                .filter(tx -> tx.getFee() != null)
                .sorted(Comparator.comparing(Transaction::getFee).reversed()
                        .thenComparing(Transaction::getTimestamp))
                .limit(limit)
                .collect(Collectors.toList());

        log.debug("Returning {} top transactions by fee", topTransactions.size());
        return topTransactions;
    }

    @Override
    @Transactional
    public void clearPool(String poolName) {
        log.info("Clearing transaction pool: {}", poolName);

        TransactionPool pool = poolRepository.findByPoolName(poolName)
                .orElseThrow(() -> new IllegalArgumentException("Pool not found: " + poolName));

        List<Transaction> transactions = pool.getPendingTransactions();
        int clearedCount = 0;

        // Remove all transactions from pool
        for (Transaction tx : transactions) {
            tx.setTransactionPool(null);
            transactionRepository.save(tx);
            clearedCount++;
        }

        // Reset pool size
        pool.setCurrentSize(0);
        poolRepository.save(pool);

        log.info("Cleared {} transactions from pool: {}", clearedCount, poolName);

        // Broadcast pool cleared event
        try {
            messagingTemplate.convertAndSend("/topic/pools/cleared",
                    poolMapper.toStatusDTO(pool));
        } catch (Exception e) {
            log.error("Failed to broadcast pool cleared event", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPoolFull(String poolName) {
        TransactionPool pool = poolRepository.findByPoolName(poolName)
                .orElseThrow(() -> new IllegalArgumentException("Pool not found: " + poolName));

        boolean isFull = pool.isFull();
        log.debug("Pool {} is full: {}", poolName, isFull);

        return isFull;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionPool> getAvailablePools() {
        log.debug("Fetching available transaction pools");

        List<TransactionPool> availablePools = poolRepository.findAvailablePools();
        log.debug("Found {} available pools", availablePools.size());

        return availablePools;
    }

    /**
     * Get all active pools
     */
    @Transactional(readOnly = true)
    public List<TransactionPool> getAllActivePools() {
        log.debug("Fetching all active transaction pools");
        return poolRepository.findByIsActive(true);
    }

    /**
     * Activate a pool
     */
    @Transactional
    public void activatePool(String poolName) {
        log.info("Activating pool: {}", poolName);

        TransactionPool pool = poolRepository.findByPoolName(poolName)
                .orElseThrow(() -> new IllegalArgumentException("Pool not found: " + poolName));

        pool.setIsActive(true);
        poolRepository.save(pool);

        log.info("Pool activated: {}", poolName);
    }

    /**
     * Deactivate a pool
     */
    @Transactional
    public void deactivatePool(String poolName) {
        log.info("Deactivating pool: {}", poolName);

        TransactionPool pool = poolRepository.findByPoolName(poolName)
                .orElseThrow(() -> new IllegalArgumentException("Pool not found: " + poolName));

        pool.setIsActive(false);
        poolRepository.save(pool);

        log.info("Pool deactivated: {}", poolName);
    }

    /**
     * Get pool capacity (percentage used)
     */
    @Transactional(readOnly = true)
    public Double getPoolCapacity(String poolName) {
        TransactionPool pool = poolRepository.findByPoolName(poolName)
                .orElseThrow(() -> new IllegalArgumentException("Pool not found: " + poolName));

        if (pool.getMaxSize() == 0) {
            return 0.0;
        }

        double capacity = (pool.getCurrentSize().doubleValue() / pool.getMaxSize().doubleValue()) * 100;
        log.debug("Pool {} capacity: {:.2f}%", poolName, capacity);

        return capacity;
    }

    /**
     * Remove confirmed transactions from pool
     */
    @Transactional
    public int removeConfirmedTransactions(String poolName) {
        log.info("Removing confirmed transactions from pool: {}", poolName);

        TransactionPool pool = poolRepository.findByPoolName(poolName)
                .orElseThrow(() -> new IllegalArgumentException("Pool not found: " + poolName));

        List<Transaction> transactions = pool.getPendingTransactions();
        int removedCount = 0;

        for (Transaction tx : transactions) {
            if (tx.getStatus() == TransactionStatus.CONFIRMED) {
                tx.setTransactionPool(null);
                transactionRepository.save(tx);
                removedCount++;
            }
        }

        // Update pool size
        pool.setCurrentSize(Math.max(0, pool.getCurrentSize() - removedCount));
        poolRepository.save(pool);

        log.info("Removed {} confirmed transactions from pool: {}", removedCount, poolName);
        return removedCount;
    }

    /**
     * Get pool statistics
     */
    @Transactional(readOnly = true)
    public PoolStatistics getPoolStatistics(String poolName) {
        TransactionPool pool = poolRepository.findByPoolName(poolName)
                .orElseThrow(() -> new IllegalArgumentException("Pool not found: " + poolName));

        List<Transaction> transactions = pool.getPendingTransactions();

        int totalTransactions = transactions.size();
        double avgFee = transactions.stream()
                .filter(tx -> tx.getFee() != null)
                .mapToDouble(tx -> tx.getFee().doubleValue())
                .average()
                .orElse(0.0);

        double totalValue = transactions.stream()
                .filter(tx -> tx.getAmount() != null)
                .mapToDouble(tx -> tx.getAmount().doubleValue())
                .sum();

        return new PoolStatistics(
                totalTransactions,
                pool.getMaxSize(),
                avgFee,
                totalValue,
                pool.getIsActive()
        );
    }

    /**
     * Inner class for pool statistics
     */
    public record PoolStatistics(
            int totalTransactions,
            int maxCapacity,
            double averageFee,
            double totalValue,
            boolean isActive
    ) {}
}
