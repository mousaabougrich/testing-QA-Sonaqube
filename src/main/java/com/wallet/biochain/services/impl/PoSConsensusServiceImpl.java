package com.wallet.biochain.services.impl;

import com.wallet.biochain.config.BlockchainConfig;
import com.wallet.biochain.entities.Block;
import com.wallet.biochain.entities.Stake;
import com.wallet.biochain.repositories.StakeRepository;
import com.wallet.biochain.services.PoSConsensusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PoSConsensusServiceImpl implements PoSConsensusService {

    private final StakeRepository stakeRepository;
    private final BlockchainConfig blockchainConfig;
    private final SecureRandom random = new SecureRandom();

    @Override
    public boolean validateStake(Block block, String validatorAddress) {
        log.debug("Validating stake for validator: {}", validatorAddress);

        // Check if validator has minimum stake
        if (!hasMinimumStake(validatorAddress)) {
            log.warn("Validator does not meet minimum stake requirement");
            return false;
        }

        // Verify validator was selected (in real implementation, check selection proof)
        List<String> eligibleValidators = getEligibleValidators();
        if (!eligibleValidators.contains(validatorAddress)) {
            log.warn("Validator is not eligible");
            return false;
        }

        log.info("Stake validation successful for validator: {}", validatorAddress);
        return true;
    }

    @Override
    public String selectValidator() {
        log.debug("Selecting validator based on stake");

        List<String> eligibleValidators = getEligibleValidators();
        if (eligibleValidators.isEmpty()) {
            throw new IllegalStateException("No eligible validators available");
        }

        // Get all active stakes
        List<Stake> activeStakes = stakeRepository.findActiveStakes();
        BigDecimal totalStake = getTotalStakedAmount();

        if (totalStake.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalStateException("No stake available");
        }

        // Random selection weighted by stake amount
        BigDecimal randomValue = new BigDecimal(random.nextDouble()).multiply(totalStake);
        BigDecimal cumulativeStake = BigDecimal.ZERO;

        for (Stake stake : activeStakes) {
            cumulativeStake = cumulativeStake.add(stake.getStakedAmount());
            if (cumulativeStake.compareTo(randomValue) >= 0) {
                log.info("Selected validator: {}", stake.getWalletAddress());
                return stake.getWalletAddress();
            }
        }

        // Fallback to first eligible validator
        String validator = eligibleValidators.get(0);
        log.info("Fallback validator selected: {}", validator);
        return validator;
    }

    @Override
    public Double calculateValidatorProbability(String validatorAddress) {
        BigDecimal validatorStake = stakeRepository.getTotalStakedAmountByAddress(validatorAddress);
        if (validatorStake == null) {
            return 0.0;
        }

        BigDecimal totalStake = getTotalStakedAmount();
        if (totalStake.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }

        return validatorStake.divide(totalStake, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .doubleValue();
    }

    @Override
    public List<String> getEligibleValidators() {
        return stakeRepository.findActiveStakes().stream()
                .filter(stake -> stake.getStakedAmount()
                        .compareTo(blockchainConfig.getConsensus().getMinStakeAmount()) >= 0)
                .map(Stake::getWalletAddress)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasMinimumStake(String address) {
        BigDecimal stakedAmount = stakeRepository.getTotalStakedAmountByAddress(address);
        if (stakedAmount == null) {
            return false;
        }
        return stakedAmount.compareTo(blockchainConfig.getConsensus().getMinStakeAmount()) >= 0;
    }

    @Override
    public BigDecimal getTotalStakedAmount() {
        BigDecimal total = stakeRepository.getTotalStakedAmount();
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculateStakingReward(Stake stake) {
        // Annual reward rate from config
        BigDecimal annualRate = blockchainConfig.getConsensus().getStakingRewardRate();

        // Calculate daily reward
        BigDecimal dailyRate = annualRate.divide(new BigDecimal("365"), 8, RoundingMode.HALF_UP);

        // Calculate reward based on staked amount
        BigDecimal reward = stake.getStakedAmount().multiply(dailyRate);

        // Apply stake weight multiplier
        if (stake.getStakeWeight() != null) {
            reward = reward.multiply(new BigDecimal(stake.getStakeWeight()));
        }

        log.debug("Calculated staking reward: {} for stake: {}", reward, stake.getId());
        return reward;
    }
}