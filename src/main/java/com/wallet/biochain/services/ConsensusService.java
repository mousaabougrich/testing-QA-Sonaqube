package com.wallet.biochain.services;

import com.wallet.biochain.entities.Block;
import com.wallet.biochain.enums.ConsensusType;

public interface ConsensusService {

    /**
     * Validate block using configured consensus mechanism
     */
    boolean validateBlock(Block block);

    /**
     * Select consensus mechanism
     */
    void setConsensusType(ConsensusType consensusType);

    /**
     * Get current consensus type
     */
    ConsensusType getCurrentConsensusType();

    /**
     * Validate block with PoW
     */
    boolean validateWithPoW(Block block);

    /**
     * Validate block with PoS
     */
    boolean validateWithPoS(Block block);

    /**
     * Check if validator is eligible (PoS)
     */
    boolean isValidatorEligible(String validatorAddress);

    /**
     * Select next validator (PoS)
     */
    String selectNextValidator();
}