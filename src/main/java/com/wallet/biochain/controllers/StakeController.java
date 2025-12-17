package com.wallet.biochain.controllers;

import com.wallet.biochain.dto.StakeDTO;
import com.wallet.biochain.services.StakeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/stakes")
@RequiredArgsConstructor
@Tag(name = "Stake", description = "Staking operations endpoints")
public class StakeController {

    private final StakeService stakeService;

    @PostMapping
    @Operation(summary = "Create stake", description = "Creates a new stake")
    public ResponseEntity<StakeDTO> createStake(
            @RequestParam Long userId,
            @RequestParam String walletAddress,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lockedUntil) {
        log.info("REST request to create stake for user: {}", userId);

        try {
            StakeDTO stake = stakeService.createStake(userId, walletAddress, amount, lockedUntil);
            return ResponseEntity.status(HttpStatus.CREATED).body(stake);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Failed to create stake", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get stake by ID", description = "Retrieves stake by ID")
    public ResponseEntity<StakeDTO> getStakeById(@PathVariable Long id) {
        log.info("REST request to get stake: {}", id);

        return stakeService.getStakeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get stakes by user", description = "Retrieves all stakes for a user")
    public ResponseEntity<List<StakeDTO>> getStakesByUserId(@PathVariable Long userId) {
        log.info("REST request to get stakes for user: {}", userId);

        List<StakeDTO> stakes = stakeService.getStakesByUserId(userId);
        return ResponseEntity.ok(stakes);
    }

    @GetMapping("/wallet/{address}")
    @Operation(summary = "Get stakes by wallet", description = "Retrieves all stakes for a wallet")
    public ResponseEntity<List<StakeDTO>> getStakesByWalletAddress(@PathVariable String address) {
        log.info("REST request to get stakes for wallet: {}", address);

        List<StakeDTO> stakes = stakeService.getStakesByWalletAddress(address);
        return ResponseEntity.ok(stakes);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active stakes", description = "Retrieves all active stakes")
    public ResponseEntity<List<StakeDTO>> getActiveStakes() {
        log.info("REST request to get active stakes");

        List<StakeDTO> stakes = stakeService.getActiveStakes();
        return ResponseEntity.ok(stakes);
    }

    @PostMapping("/unlock-expired")
    @Operation(summary = "Unlock expired stakes", description = "Unlocks all expired stakes")
    public ResponseEntity<Void> unlockExpiredStakes() {
        log.info("REST request to unlock expired stakes");

        stakeService.unlockExpiredStakes();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/withdraw")
    @Operation(summary = "Withdraw stake", description = "Withdraws a stake with rewards")
    public ResponseEntity<Void> withdrawStake(@PathVariable Long id) {
        log.info("REST request to withdraw stake: {}", id);

        try {
            stakeService.withdrawStake(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Failed to withdraw stake", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/rewards")
    @Operation(summary = "Calculate rewards", description = "Calculates pending rewards for stake")
    public ResponseEntity<BigDecimal> calculateRewards(@PathVariable Long id) {
        log.info("REST request to calculate rewards for stake: {}", id);

        try {
            BigDecimal rewards = stakeService.calculateRewards(id);
            return ResponseEntity.ok(rewards);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/distribute-rewards")
    @Operation(summary = "Distribute rewards", description = "Distributes rewards to all active stakes")
    public ResponseEntity<Void> distributeRewards() {
        log.info("REST request to distribute rewards");

        stakeService.distributeRewards();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/total-staked")
    @Operation(summary = "Get total staked amount", description = "Gets total staked amount across network")
    public ResponseEntity<BigDecimal> getTotalStakedAmount() {
        log.info("REST request to get total staked amount");

        BigDecimal total = stakeService.getTotalStakedAmount();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/wallet/{address}/total-staked")
    @Operation(summary = "Get total staked by wallet", description = "Gets total staked amount for wallet")
    public ResponseEntity<BigDecimal> getTotalStakedAmountByAddress(@PathVariable String address) {
        log.info("REST request to get total staked for wallet: {}", address);

        BigDecimal total = stakeService.getTotalStakedAmountByAddress(address);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/top-stakers")
    @Operation(summary = "Get top stakers", description = "Gets top stakers leaderboard")
    public ResponseEntity<List<StakeDTO>> getTopStakers() {
        log.info("REST request to get top stakers");

        List<StakeDTO> topStakers = stakeService.getTopStakers();
        return ResponseEntity.ok(topStakers);
    }
}