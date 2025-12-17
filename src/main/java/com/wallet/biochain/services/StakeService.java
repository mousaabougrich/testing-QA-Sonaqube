package com.wallet.biochain.services;

import com.wallet.biochain.dto.StakeDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StakeService {

    /**
     * Create a new stake
     */
    StakeDTO createStake(Long userId, String walletAddress, BigDecimal amount, LocalDateTime lockedUntil);

    /**
     * Get stake by ID
     */
    Optional<StakeDTO> getStakeById(Long id);

    /**
     * Get stakes by user
     */
    List<StakeDTO> getStakesByUserId(Long userId);

    /**
     * Get stakes by wallet address
     */
    List<StakeDTO> getStakesByWalletAddress(String walletAddress);

    /**
     * Get active stakes
     */
    List<StakeDTO> getActiveStakes();

    /**
     * Unlock expired stakes
     */
    void unlockExpiredStakes();

    /**
     * Withdraw stake
     */
    void withdrawStake(Long stakeId);

    /**
     * Calculate rewards
     */
    BigDecimal calculateRewards(Long stakeId);

    /**
     * Distribute rewards
     */
    void distributeRewards();

    /**
     * Get total staked amount
     */
    BigDecimal getTotalStakedAmount();

    /**
     * Get total staked amount by address
     */
    BigDecimal getTotalStakedAmountByAddress(String walletAddress);

    /**
     * Get top stakers
     */
    List<StakeDTO> getTopStakers();
}