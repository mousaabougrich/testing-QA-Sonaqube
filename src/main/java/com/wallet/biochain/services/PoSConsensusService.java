package com.wallet.biochain.services;

import com.wallet.biochain.entities.Block;
import com.wallet.biochain.entities.Stake;

import java.math.BigDecimal;
import java.util.List;

public interface PoSConsensusService {

    /**
     * Validate block using Proof of Stake
     */
    boolean validateStake(Block block, String validatorAddress);

    /**
     * Select validator based on stake
     */
    String selectValidator();

    /**
     * Calculate validator probability
     */
    Double calculateValidatorProbability(String validatorAddress);

    /**
     * Get eligible validators
     */
    List<String> getEligibleValidators();

    /**
     * Check minimum stake requirement
     */
    boolean hasMinimumStake(String address);

    /**
     * Get total staked amount
     */
    BigDecimal getTotalStakedAmount();

    /**
     * Calculate staking reward
     */
    BigDecimal calculateStakingReward(Stake stake);
}