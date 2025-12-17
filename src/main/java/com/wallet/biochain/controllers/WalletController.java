package com.wallet.biochain.controllers;

import com.wallet.biochain.dto.BalanceDTO;
import com.wallet.biochain.dto.WalletCreateRequestDTO;
import com.wallet.biochain.dto.WalletCreateResponseDTO;
import com.wallet.biochain.dto.WalletDTO;
import com.wallet.biochain.services.WalletService;
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
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallet", description = "Wallet management endpoints")
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    @Operation(summary = "Create a new wallet", description = "Creates a new wallet with generated keys for a user")
    public ResponseEntity<WalletCreateResponseDTO> createWallet(@RequestBody WalletCreateRequestDTO request) {
        log.info("REST request to create wallet for user: {}", request.userId());

        try {
            WalletCreateResponseDTO wallet = walletService.createWallet(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(wallet);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request to create wallet", e);
            throw e;
        }
    }

    @GetMapping("/{address}")
    @Operation(summary = "Get wallet by address", description = "Retrieves wallet information by wallet address")
    public ResponseEntity<WalletDTO> getWalletByAddress(@PathVariable String address) {
        log.info("REST request to get wallet: {}", address);

        return walletService.getWalletByAddress(address)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get wallets by user ID", description = "Retrieves all wallets for a specific user")
    public ResponseEntity<List<WalletDTO>> getWalletsByUserId(@PathVariable Long userId) {
        log.info("REST request to get wallets for user: {}", userId);

        List<WalletDTO> wallets = walletService.getWalletsByUserId(userId);
        return ResponseEntity.ok(wallets);
    }

    @GetMapping("/user/{userId}/active")
    @Operation(summary = "Get active wallets by user ID", description = "Retrieves only active wallets for a user")
    public ResponseEntity<List<WalletDTO>> getActiveWalletsByUserId(@PathVariable Long userId) {
        log.info("REST request to get active wallets for user: {}", userId);

        List<WalletDTO> wallets = walletService.getActiveWalletsByUserId(userId);
        return ResponseEntity.ok(wallets);
    }

    @GetMapping("/{address}/balance")
    @Operation(summary = "Get wallet balance", description = "Retrieves balance including pending transactions")
    public ResponseEntity<BalanceDTO> getBalance(@PathVariable String address) {
        log.info("REST request to get balance for wallet: {}", address);

        try {
            BalanceDTO balance = walletService.getBalance(address);
            return ResponseEntity.ok(balance);
        } catch (IllegalArgumentException e) {
            log.error("Wallet not found: {}", address);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{address}/balance")
    @Operation(summary = "Update wallet balance", description = "Updates the wallet balance (admin only)")
    public ResponseEntity<Void> updateBalance(
            @PathVariable String address,
            @RequestParam BigDecimal newBalance) {
        log.info("REST request to update balance for wallet: {} to {}", address, newBalance);

        try {
            walletService.updateBalance(address, newBalance);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid balance update request", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{address}/activate")
    @Operation(summary = "Activate wallet", description = "Activates a deactivated wallet")
    public ResponseEntity<Void> activateWallet(@PathVariable String address) {
        log.info("REST request to activate wallet: {}", address);

        try {
            walletService.activateWallet(address);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Failed to activate wallet", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{address}/deactivate")
    @Operation(summary = "Deactivate wallet", description = "Deactivates a wallet")
    public ResponseEntity<Void> deactivateWallet(@PathVariable String address) {
        log.info("REST request to deactivate wallet: {}", address);

        try {
            walletService.deactivateWallet(address);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Failed to deactivate wallet", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/total-balance")
    @Operation(summary = "Get total balance", description = "Gets total balance across all active wallets")
    public ResponseEntity<BigDecimal> getTotalBalance() {
        log.info("REST request to get total balance");

        BigDecimal totalBalance = walletService.getTotalBalance();
        return ResponseEntity.ok(totalBalance);
    }

    @PostMapping("/{address}/export")
    @Operation(summary = "Export wallet", description = "Exports wallet private key (requires authentication)")
    public ResponseEntity<String> exportWallet(
            @PathVariable String address,
            @RequestParam String password) {
        log.info("REST request to export wallet: {}", address);

        try {
            String privateKey = walletService.exportWallet(address, password);
            return ResponseEntity.ok(privateKey);
        } catch (Exception e) {
            log.error("Failed to export wallet", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/import")
    @Operation(summary = "Import wallet", description = "Imports a wallet from private key")
    public ResponseEntity<WalletDTO> importWallet(
            @RequestParam Long userId,
            @RequestParam String privateKey,
            @RequestParam String password) {
        log.info("REST request to import wallet for user: {}", userId);

        try {
            WalletDTO wallet = walletService.importWallet(userId, privateKey, password);
            return ResponseEntity.status(HttpStatus.CREATED).body(wallet);
        } catch (Exception e) {
            log.error("Failed to import wallet", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/validate/{address}")
    @Operation(summary = "Validate address", description = "Checks if an address is valid")
    public ResponseEntity<Boolean> validateAddress(@PathVariable String address) {
        log.debug("REST request to validate address: {}", address);

        boolean isValid = walletService.isValidAddress(address);
        return ResponseEntity.ok(isValid);
    }

    @GetMapping("/exists/{address}")
    @Operation(summary = "Check wallet exists", description = "Checks if a wallet exists")
    public ResponseEntity<Boolean> walletExists(@PathVariable String address) {
        log.debug("REST request to check wallet exists: {}", address);

        boolean exists = walletService.walletExists(address);
        return ResponseEntity.ok(exists);
    }
}