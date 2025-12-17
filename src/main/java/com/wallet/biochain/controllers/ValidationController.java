package com.wallet.biochain.controllers;

import com.wallet.biochain.entities.Block;
import com.wallet.biochain.entities.Transaction;
import com.wallet.biochain.services.ValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/api/validation")
@RequiredArgsConstructor
@Tag(name = "Validation", description = "Validation endpoints for blockchain entities")
public class ValidationController {

    private final ValidationService validationService;

    @PostMapping("/transaction")
    @Operation(summary = "Validate transaction", description = "Validates a transaction")
    public ResponseEntity<Boolean> validateTransaction(@RequestBody Transaction transaction) {
        log.info("REST request to validate transaction: {}", transaction.getTransactionHash());

        boolean isValid = validationService.validateTransaction(transaction);
        return ResponseEntity.ok(isValid);
    }

    @PostMapping("/block")
    @Operation(summary = "Validate block", description = "Validates a block")
    public ResponseEntity<Boolean> validateBlock(@RequestBody Block block) {
        log.info("REST request to validate block: {}", block.getBlockIndex());

        boolean isValid = validationService.validateBlock(block);
        return ResponseEntity.ok(isValid);
    }

    @PostMapping("/signature")
    @Operation(summary = "Validate signature", description = "Validates a transaction signature")
    public ResponseEntity<Boolean> validateSignature(@RequestBody Transaction transaction) {
        log.debug("REST request to validate signature for transaction: {}", transaction.getTransactionHash());

        boolean isValid = validationService.validateSignature(transaction);
        return ResponseEntity.ok(isValid);
    }

    @PostMapping("/balance")
    @Operation(summary = "Validate balance", description = "Validates if wallet has sufficient balance")
    public ResponseEntity<Boolean> validateBalance(
            @RequestParam String walletAddress,
            @RequestParam BigDecimal amount) {
        log.debug("REST request to validate balance for wallet: {}", walletAddress);

        boolean isValid = validationService.validateBalance(walletAddress, amount);
        return ResponseEntity.ok(isValid);
    }

    @GetMapping("/address/{address}")
    @Operation(summary = "Validate address", description = "Validates a wallet address format")
    public ResponseEntity<Boolean> validateAddress(@PathVariable String address) {
        log.debug("REST request to validate address: {}", address);

        boolean isValid = validationService.validateAddressFormat(address);
        return ResponseEntity.ok(isValid);
    }

    @PostMapping("/block-hash")
    @Operation(summary = "Validate block hash", description = "Validates block hash matches content")
    public ResponseEntity<Boolean> validateBlockHash(@RequestBody Block block) {
        log.debug("REST request to validate block hash: {}", block.getBlockIndex());

        boolean isValid = validationService.validateBlockHash(block);
        return ResponseEntity.ok(isValid);
    }

    @PostMapping("/transaction-format")
    @Operation(summary = "Validate transaction format", description = "Validates transaction format and fields")
    public ResponseEntity<Boolean> validateTransactionFormat(@RequestBody Transaction transaction) {
        log.debug("REST request to validate transaction format");

        boolean isValid = validationService.validateTransactionFormat(transaction);
        return ResponseEntity.ok(isValid);
    }

    @PostMapping("/double-spend-check")
    @Operation(summary = "Check double spending", description = "Checks for double spending attempts")
    public ResponseEntity<Boolean> checkDoubleSpending(@RequestBody Transaction transaction) {
        log.info("REST request to check double spending");

        boolean isDoubleSpend = validationService.checkDoubleSpending(transaction);
        return ResponseEntity.ok(isDoubleSpend);
    }
}