package com.wallet.biochain.controllers;

import com.wallet.biochain.dto.TransactionHistoryDTO;
import com.wallet.biochain.dto.TransactionRequestDTO;
import com.wallet.biochain.dto.TransactionResponseDTO;
import com.wallet.biochain.services.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction", description = "Transaction management endpoints")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Create transaction", description = "Creates and signs a new transaction")
    public ResponseEntity<TransactionResponseDTO> createTransaction(@RequestBody TransactionRequestDTO request) {
        log.info("REST request to create transaction from {} to {}",
                request.senderAddress(), request.recipientAddress());

        try {
            TransactionResponseDTO transaction = transactionService.createTransaction(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Failed to create transaction", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{hash}")
    @Operation(summary = "Get transaction by hash", description = "Retrieves transaction details by hash")
    public ResponseEntity<TransactionResponseDTO> getTransactionByHash(@PathVariable String hash) {
        log.info("REST request to get transaction: {}", hash);

        return transactionService.getTransactionByHash(hash)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/wallet/{address}/history")
    @Operation(summary = "Get transaction history", description = "Gets all transactions for a wallet")
    public ResponseEntity<List<TransactionHistoryDTO>> getTransactionHistory(@PathVariable String address) {
        log.info("REST request to get transaction history for wallet: {}", address);

        List<TransactionHistoryDTO> history = transactionService.getTransactionHistory(address);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/wallet/{address}/sent")
    @Operation(summary = "Get sent transactions", description = "Gets transactions sent from a wallet")
    public ResponseEntity<List<TransactionResponseDTO>> getSentTransactions(@PathVariable String address) {
        log.info("REST request to get sent transactions for wallet: {}", address);

        List<TransactionResponseDTO> transactions = transactionService.getSentTransactions(address);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/wallet/{address}/received")
    @Operation(summary = "Get received transactions", description = "Gets transactions received by a wallet")
    public ResponseEntity<List<TransactionResponseDTO>> getReceivedTransactions(@PathVariable String address) {
        log.info("REST request to get received transactions for wallet: {}", address);

        List<TransactionResponseDTO> transactions = transactionService.getReceivedTransactions(address);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending transactions", description = "Gets all pending transactions")
    public ResponseEntity<List<TransactionResponseDTO>> getPendingTransactions() {
        log.info("REST request to get pending transactions");

        List<TransactionResponseDTO> transactions = transactionService.getPendingTransactions();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/confirmed")
    @Operation(summary = "Get confirmed transactions", description = "Gets all confirmed transactions")
    public ResponseEntity<List<TransactionResponseDTO>> getConfirmedTransactions() {
        log.info("REST request to get confirmed transactions");

        List<TransactionResponseDTO> transactions = transactionService.getConfirmedTransactions();
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/{hash}/confirm")
    @Operation(summary = "Confirm transaction", description = "Confirms a transaction (adds to block)")
    public ResponseEntity<Void> confirmTransaction(
            @PathVariable String hash,
            @RequestParam Long blockId) {
        log.info("REST request to confirm transaction: {} in block: {}", hash, blockId);

        try {
            transactionService.confirmTransaction(hash, blockId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Failed to confirm transaction", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/wallet/{address}/count")
    @Operation(summary = "Get transaction count", description = "Gets total transaction count for a wallet")
    public ResponseEntity<Long> getTransactionCount(@PathVariable String address) {
        log.info("REST request to get transaction count for wallet: {}", address);

        Long count = transactionService.getTransactionCount(address);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/total-volume")
    @Operation(summary = "Get total transaction volume", description = "Gets total transaction volume")
    public ResponseEntity<BigDecimal> getTotalTransactionVolume() {
        log.info("REST request to get total transaction volume");

        BigDecimal volume = transactionService.getTotalTransactionVolume();
        return ResponseEntity.ok(volume);
    }

    @GetMapping("/calculate-fee")
    @Operation(summary = "Calculate transaction fee", description = "Calculates fee for a transaction amount")
    public ResponseEntity<BigDecimal> calculateTransactionFee(@RequestParam BigDecimal amount) {
        log.debug("REST request to calculate fee for amount: {}", amount);

        BigDecimal fee = transactionService.calculateTransactionFee(amount);
        return ResponseEntity.ok(fee);
    }
}