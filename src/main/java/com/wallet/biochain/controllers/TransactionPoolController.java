package com.wallet.biochain.controllers;

import com.wallet.biochain.dto.TransactionPoolStatusDTO;
import com.wallet.biochain.entities.Transaction;
import com.wallet.biochain.entities.TransactionPool;
import com.wallet.biochain.services.TransactionPoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/transaction-pool")
@RequiredArgsConstructor
@Tag(name = "Transaction Pool", description = "Transaction pool management endpoints")
public class TransactionPoolController {

    private final TransactionPoolService poolService;

    @PostMapping
    @Operation(summary = "Create pool", description = "Creates a new transaction pool")
    public ResponseEntity<TransactionPool> createPool(
            @RequestParam String poolName,
            @RequestParam Integer maxSize) {
        log.info("REST request to create transaction pool: {}", poolName);

        try {
            TransactionPool pool = poolService.createPool(poolName, maxSize);
            return ResponseEntity.status(HttpStatus.CREATED).body(pool);
        } catch (IllegalArgumentException e) {
            log.error("Failed to create pool", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get pool by ID", description = "Retrieves pool by ID")
    public ResponseEntity<TransactionPool> getPoolById(@PathVariable Long id) {
        log.info("REST request to get pool: {}", id);

        return poolService.getPoolById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{poolName}")
    @Operation(summary = "Get pool by name", description = "Retrieves pool by name")
    public ResponseEntity<TransactionPool> getPoolByName(@PathVariable String poolName) {
        log.info("REST request to get pool by name: {}", poolName);

        return poolService.getPoolByName(poolName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{poolName}/status")
    @Operation(summary = "Get pool status", description = "Gets transaction pool status and statistics")
    public ResponseEntity<TransactionPoolStatusDTO> getPoolStatus(@PathVariable String poolName) {
        log.info("REST request to get pool status: {}", poolName);

        try {
            TransactionPoolStatusDTO status = poolService.getPoolStatus(poolName);
            return ResponseEntity.ok(status);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{poolName}/transactions")
    @Operation(summary = "Add transaction to pool", description = "Adds a transaction to the pool")
    public ResponseEntity<Void> addTransaction(
            @PathVariable String poolName,
            @RequestBody Transaction transaction) {
        log.info("REST request to add transaction to pool: {}", poolName);

        try {
            poolService.addTransaction(poolName, transaction);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Failed to add transaction to pool", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{poolName}/transactions/{transactionHash}")
    @Operation(summary = "Remove transaction", description = "Removes a transaction from pool")
    public ResponseEntity<Void> removeTransaction(
            @PathVariable String poolName,
            @PathVariable String transactionHash) {
        log.info("REST request to remove transaction {} from pool: {}", transactionHash, poolName);

        try {
            poolService.removeTransaction(poolName, transactionHash);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Failed to remove transaction", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{poolName}/transactions")
    @Operation(summary = "Get pending transactions", description = "Gets all pending transactions in pool")
    public ResponseEntity<List<Transaction>> getPendingTransactions(@PathVariable String poolName) {
        log.info("REST request to get pending transactions from pool: {}", poolName);

        try {
            List<Transaction> transactions = poolService.getPendingTransactions(poolName);
            return ResponseEntity.ok(transactions);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{poolName}/top-by-fee")
    @Operation(summary = "Get top transactions by fee", description = "Gets top transactions sorted by fee")
    public ResponseEntity<List<Transaction>> getTopTransactionsByFee(
            @PathVariable String poolName,
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("REST request to get top {} transactions by fee from pool: {}", limit, poolName);

        try {
            List<Transaction> transactions = poolService.getTopTransactionsByFee(poolName, limit);
            return ResponseEntity.ok(transactions);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{poolName}/clear")
    @Operation(summary = "Clear pool", description = "Removes all transactions from pool")
    public ResponseEntity<Void> clearPool(@PathVariable String poolName) {
        log.info("REST request to clear pool: {}", poolName);

        try {
            poolService.clearPool(poolName);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{poolName}/is-full")
    @Operation(summary = "Check if pool is full", description = "Checks if pool has reached max capacity")
    public ResponseEntity<Boolean> isPoolFull(@PathVariable String poolName) {
        log.debug("REST request to check if pool is full: {}", poolName);

        try {
            boolean isFull = poolService.isPoolFull(poolName);
            return ResponseEntity.ok(isFull);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/available")
    @Operation(summary = "Get available pools", description = "Gets all available (not full) pools")
    public ResponseEntity<List<TransactionPool>> getAvailablePools() {
        log.info("REST request to get available pools");

        List<TransactionPool> pools = poolService.getAvailablePools();
        return ResponseEntity.ok(pools);
    }
}