package com.wallet.biochain.services;

import com.wallet.biochain.entities.Block;

public interface PoWConsensusService {

    /**
     * Mine block using Proof of Work
     */
    Block mineBlock(Block block, Integer difficulty);

    /**
     * Validate Proof of Work
     */
    boolean validateProofOfWork(Block block, Integer difficulty);

    /**
     * Calculate hash with nonce
     */
    String calculateHash(Block block);

    /**
     * Check if hash meets difficulty target
     */
    boolean meetsTarget(String hash, Integer difficulty);

    /**
     * Get difficulty target prefix
     */
    String getDifficultyTarget(Integer difficulty);
}