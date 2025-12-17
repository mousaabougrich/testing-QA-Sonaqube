package com.wallet.biochain.services.impl;

import com.wallet.biochain.dto.MiningResultDTO;
import com.wallet.biochain.entities.Block;
import com.wallet.biochain.entities.Transaction;
import com.wallet.biochain.mappers.MiningMapper;
import com.wallet.biochain.services.BlockService;
import com.wallet.biochain.services.CryptographyService;
import com.wallet.biochain.services.MiningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MiningServiceImpl implements MiningService {

    private final BlockService blockService;
    private final CryptographyService cryptographyService;
    private final MiningMapper miningMapper;

    private static final int DEFAULT_DIFFICULTY = 4;
    private static final BigDecimal INITIAL_REWARD = new BigDecimal("50");
    private static final int HALVING_INTERVAL = 210000;

    @Override
    public MiningResultDTO mineBlock(List<Transaction> transactions, String minerAddress) {
        return mineBlockWithDifficulty(transactions, minerAddress, DEFAULT_DIFFICULTY);
    }

    @Override
    public MiningResultDTO mineBlockWithDifficulty(List<Transaction> transactions, String minerAddress, Integer difficulty) {
        log.info("Starting mining with difficulty: {}", difficulty);
        long startTime = System.currentTimeMillis();

        try {
            // Get latest block index
            Integer nextIndex = blockService.getBlockCount().intValue();
            String previousHash = "0";

            if (nextIndex > 0) {
                var latestBlock = blockService.getLatestBlock();
                if (latestBlock.isPresent()) {
                    previousHash = latestBlock.get().hash();
                }
            }

            // Create block
            Block block = blockService.createBlock(nextIndex, previousHash, transactions);
            // Ensure previous hash is set on the block (some implementations/tests assert this explicitly)
            block.setPreviousHash(previousHash);
            block.setDifficulty(difficulty);
            block.setMinerAddress(minerAddress);

            // Mine (find valid nonce)
            String proofOfWork = calculateProofOfWork(block, difficulty);
            block.setHash(proofOfWork);

            // Save block
            Block minedBlock = blockService.addBlock(block);

            long miningDuration = System.currentTimeMillis() - startTime;
            BigDecimal reward = calculateMiningReward(nextIndex);

            log.info("Block mined successfully in {} ms", miningDuration);
            return miningMapper.toResultDTO(minedBlock, miningDuration, reward);

        } catch (Exception e) {
            log.error("Mining failed", e);
            long duration = System.currentTimeMillis() - startTime;
            return miningMapper.toFailedResultDTO("Mining failed: " + e.getMessage());
        }
    }

    @Override
    public String calculateProofOfWork(Block block, Integer difficulty) {
        String target = getDifficultyTarget(difficulty);
        String hash;
        int nonce = 0;

        log.debug("Mining with target: {}", target);

        do {
            nonce++;
            block.setNonce(nonce);
            hash = blockService.calculateBlockHash(block);

            if (nonce % 100000 == 0) {
                log.debug("Tried {} nonces...", nonce);
            }
        } while (!hash.substring(0, difficulty).equals(target));

        log.info("Proof of work found! Nonce: {}, Hash: {}", nonce, hash);
        return hash;
    }

    @Override
    public boolean verifyProofOfWork(Block block, Integer difficulty) {
        String hash = blockService.calculateBlockHash(block);
        return hashMeetsDifficulty(hash, difficulty);
    }

    @Override
    public Integer getCurrentDifficulty() {
        return DEFAULT_DIFFICULTY;
    }

    @Override
    public Integer adjustDifficulty(Long averageMiningTime) {
        final long TARGET_TIME = 600000; // 10 minutes in milliseconds

        if (averageMiningTime <= TARGET_TIME / 2) {
            return DEFAULT_DIFFICULTY + 1;
        } else if (averageMiningTime >= TARGET_TIME * 2) {
            return Math.max(1, DEFAULT_DIFFICULTY - 1);
        }

        return DEFAULT_DIFFICULTY;
    }

    @Override
    public BigDecimal calculateMiningReward(Integer blockIndex) {
        int halvings = blockIndex / HALVING_INTERVAL;
        BigDecimal reward = INITIAL_REWARD;

        for (int i = 0; i < halvings; i++) {
            reward = reward.divide(new BigDecimal("2"));
        }

        return reward;
    }

    @Override
    public Long getEstimatedMiningTime(Integer difficulty) {
        // Rough estimate based on difficulty; ensure it's at least 1 ms to avoid zero for small difficulties
        long estimate = (long) (Math.pow(16.0, difficulty) / 1_000_000.0);
        return Math.max(1L, estimate);
    }

    @Override
    public boolean hashMeetsDifficulty(String hash, Integer difficulty) {
        String target = getDifficultyTarget(difficulty);
        return hash.substring(0, difficulty).equals(target);
    }

    private String getDifficultyTarget(Integer difficulty) {
        return "0".repeat(difficulty);
    }
}