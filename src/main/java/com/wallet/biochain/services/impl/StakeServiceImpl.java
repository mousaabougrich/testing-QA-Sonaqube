package com.wallet.biochain.services.impl;

import com.wallet.biochain.config.BlockchainConfig;
import com.wallet.biochain.dto.StakeDTO;
import com.wallet.biochain.entities.Stake;
import com.wallet.biochain.entities.User;
import com.wallet.biochain.entities.Wallet;
import com.wallet.biochain.enums.StakeStatus;
import com.wallet.biochain.mappers.StakeMapper;
import com.wallet.biochain.repositories.StakeRepository;
import com.wallet.biochain.repositories.UserRepository;
import com.wallet.biochain.repositories.WalletRepository;
import com.wallet.biochain.services.PoSConsensusService;
import com.wallet.biochain.services.StakeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StakeServiceImpl implements StakeService {

    private final StakeRepository stakeRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PoSConsensusService posConsensusService;
    private final StakeMapper stakeMapper;
    private final BlockchainConfig blockchainConfig;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public StakeDTO createStake(Long userId, String walletAddress, BigDecimal amount, LocalDateTime lockedUntil) {
        log.info("Creating stake for user {} with amount {}", userId, amount);

        // Validate minimum stake amount
        BigDecimal minStake = blockchainConfig.getConsensus().getMinStakeAmount();
        if (amount.compareTo(minStake) < 0) {
            throw new IllegalArgumentException("Stake amount must be at least " + minStake);
        }

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Validate wallet exists and belongs to user
        Wallet wallet = walletRepository.findByAddress(walletAddress)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + walletAddress));

        if (!wallet.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Wallet does not belong to user");
        }

        // Validate sufficient balance
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance for staking. Required: " + amount +
                    ", Available: " + wallet.getBalance());
        }

        // Validate lock period
        if (lockedUntil != null && lockedUntil.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Lock until date must be in the future");
        }

        // Create stake
        Stake stake = new Stake(user, walletAddress, amount, lockedUntil);
        if (lockedUntil != null) {
            stake.setStatus(StakeStatus.LOCKED);
        } else {
            stake.setStatus(StakeStatus.ACTIVE);
        }

        // Deduct from wallet balance
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        Stake savedStake = stakeRepository.save(stake);
        log.info("Stake created successfully with ID: {}", savedStake.getId());

        // Broadcast stake event via WebSocket
        try {
            StakeDTO stakeDTO = stakeMapper.toDTO(savedStake);
            messagingTemplate.convertAndSend("/topic/stakes", stakeDTO);
        } catch (Exception e) {
            log.error("Failed to broadcast stake creation event", e);
        }

        return stakeMapper.toDTO(savedStake);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StakeDTO> getStakeById(Long id) {
        log.debug("Fetching stake by ID: {}", id);
        return stakeRepository.findById(id).map(stakeMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StakeDTO> getStakesByUserId(Long userId) {
        log.debug("Fetching stakes for user ID: {}", userId);
        return stakeMapper.toDTOList(stakeRepository.findByUserId(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StakeDTO> getStakesByWalletAddress(String walletAddress) {
        log.debug("Fetching stakes for wallet: {}", walletAddress);
        return stakeMapper.toDTOList(stakeRepository.findByWalletAddress(walletAddress));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StakeDTO> getActiveStakes() {
        log.debug("Fetching all active stakes");
        return stakeMapper.toDTOList(stakeRepository.findActiveStakes());
    }

    @Override
    @Transactional
    public void unlockExpiredStakes() {
        log.info("Unlocking expired stakes");

        LocalDateTime now = LocalDateTime.now();
        List<Stake> expiredStakes = stakeRepository.findExpiredLocks(now);

        int unlockedCount = 0;
        for (Stake stake : expiredStakes) {
            if (stake.getStatus() == StakeStatus.LOCKED) {
                stake.setStatus(StakeStatus.UNLOCKED);
                stakeRepository.save(stake);
                unlockedCount++;

                log.debug("Unlocked stake ID: {}", stake.getId());

                // Broadcast unlock event
                try {
                    messagingTemplate.convertAndSend("/topic/stakes/unlocked",
                            stakeMapper.toDTO(stake));
                } catch (Exception e) {
                    log.error("Failed to broadcast unlock event", e);
                }
            }
        }

        log.info("Unlocked {} expired stakes", unlockedCount);
    }

    @Override
    @Transactional
    public void withdrawStake(Long stakeId) {
        log.info("Withdrawing stake: {}", stakeId);

        Stake stake = stakeRepository.findById(stakeId)
                .orElseThrow(() -> new IllegalArgumentException("Stake not found with ID: " + stakeId));

        // Check if stake is locked
        if (stake.isLocked()) {
            throw new IllegalStateException("Cannot withdraw locked stake. Lock expires at: " +
                    stake.getLockedUntil());
        }

        // Check if already withdrawn
        if (stake.getStatus() == StakeStatus.WITHDRAWN) {
            throw new IllegalStateException("Stake has already been withdrawn");
        }

        // Calculate total amount to return (staked amount + rewards)
        BigDecimal stakedAmount = stake.getStakedAmount() != null ? stake.getStakedAmount() : BigDecimal.ZERO;
        BigDecimal rewards = stake.getRewardsEarned() != null ? stake.getRewardsEarned() : BigDecimal.ZERO;
        BigDecimal totalReturn = stakedAmount.add(rewards);

        // Return to wallet
        Wallet wallet = walletRepository.findByAddress(stake.getWalletAddress())
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + stake.getWalletAddress()));

        wallet.setBalance(wallet.getBalance().add(totalReturn));
        walletRepository.save(wallet);

        // Mark stake as withdrawn
        stake.setStatus(StakeStatus.WITHDRAWN);
        stakeRepository.save(stake);

        log.info("Stake withdrawn successfully. Amount returned: {}", totalReturn);

        // Broadcast withdrawal event
        try {
            messagingTemplate.convertAndSend("/topic/stakes/withdrawn", stakeMapper.toDTO(stake));
        } catch (Exception e) {
            log.error("Failed to broadcast withdrawal event", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateRewards(Long stakeId) {
        log.debug("Calculating rewards for stake: {}", stakeId);

        Stake stake = stakeRepository.findById(stakeId)
                .orElseThrow(() -> new IllegalArgumentException("Stake not found with ID: " + stakeId));

        BigDecimal reward = posConsensusService.calculateStakingReward(stake);
        log.debug("Calculated reward: {} for stake: {}", reward, stakeId);

        return reward;
    }

    @Override
    @Transactional
    public void distributeRewards() {
        log.info("Distributing staking rewards");

        List<Stake> activeStakes = stakeRepository.findActiveStakes();

        if (activeStakes.isEmpty()) {
            log.info("No active stakes to distribute rewards to");
            return;
        }

        BigDecimal totalRewardsDistributed = BigDecimal.ZERO;
        int successCount = 0;
        int failCount = 0;

        for (Stake stake : activeStakes) {
            try {
                // Calculate reward for this stake
                BigDecimal reward = posConsensusService.calculateStakingReward(stake);

                if (reward.compareTo(BigDecimal.ZERO) > 0) {
                    // Add reward to stake
                    BigDecimal currentRewards = stake.getRewardsEarned() != null ?
                            stake.getRewardsEarned() : BigDecimal.ZERO;
                    stake.setRewardsEarned(currentRewards.add(reward));
                    stake.setLastRewardTime(LocalDateTime.now());

                    stakeRepository.save(stake);

                    totalRewardsDistributed = totalRewardsDistributed.add(reward);
                    successCount++;

                    log.debug("Distributed reward {} to stake {}", reward, stake.getId());
                }
            } catch (Exception e) {
                log.error("Failed to distribute reward to stake: {}", stake.getId(), e);
                failCount++;
            }
        }

        log.info("Reward distribution completed. Total distributed: {}, Success: {}, Failed: {}",
                totalRewardsDistributed, successCount, failCount);

        // Broadcast reward distribution summary
        try {
            String message = String.format("Distributed %s rewards to %d stakes",
                    totalRewardsDistributed, successCount);
            messagingTemplate.convertAndSend("/topic/stakes/rewards", message);
        } catch (Exception e) {
            log.error("Failed to broadcast reward distribution", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalStakedAmount() {
        log.debug("Calculating total staked amount");
        BigDecimal total = stakeRepository.getTotalStakedAmount();
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalStakedAmountByAddress(String walletAddress) {
        log.debug("Calculating total staked amount for wallet: {}", walletAddress);
        BigDecimal total = stakeRepository.getTotalStakedAmountByAddress(walletAddress);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StakeDTO> getTopStakers() {
        log.debug("Fetching top stakers");
        List<Stake> topStakes = stakeRepository.findTopStakers();
        return stakeMapper.toDTOList(topStakes.stream().limit(10).toList());
    }
}