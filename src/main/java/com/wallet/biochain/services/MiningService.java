package com.wallet.biochain.services;

import com.wallet.biochain.dto.MiningResultDTO;
import com.wallet.biochain.entities.Block;
import com.wallet.biochain.entities.Transaction;

import java.util.List;

public interface MiningService {

    /**
     * Mine a new block (PoW)
     */
    MiningResultDTO mineBlock(List<Transaction> transactions, String minerAddress);

    /**
     * Mine block with specific difficulty
     */
    MiningResultDTO mineBlockWithDifficulty(List<Transaction> transactions, String minerAddress, Integer difficulty);

    /**
     * Calculate proof of work
     */
    String calculateProofOfWork(Block block, Integer difficulty);

    /**
     * Verify proof of work
     */
    boolean verifyProofOfWork(Block block, Integer difficulty);

    /**
     * Get current mining difficulty
     */
    Integer getCurrentDifficulty();

    /**
     * Adjust difficulty based on mining time
     */
    Integer adjustDifficulty(Long averageMiningTime);

    /**
     * Calculate mining reward
     */
    java.math.BigDecimal calculateMiningReward(Integer blockIndex);

    /**
     * Get estimated mining time
     */
    Long getEstimatedMiningTime(Integer difficulty);

    /**
     * Check if hash meets difficulty requirement
     */
    boolean hashMeetsDifficulty(String hash, Integer difficulty);
}