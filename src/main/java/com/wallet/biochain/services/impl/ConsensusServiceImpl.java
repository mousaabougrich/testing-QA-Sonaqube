package com.wallet.biochain.services.impl;

import com.wallet.biochain.entities.Block;
import com.wallet.biochain.enums.ConsensusType;
import com.wallet.biochain.services.ConsensusService;
import com.wallet.biochain.services.PoSConsensusService;
import com.wallet.biochain.services.PoWConsensusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsensusServiceImpl implements ConsensusService {

    private final PoWConsensusService powConsensusService;
    private final PoSConsensusService posConsensusService;

    private ConsensusType currentConsensusType = ConsensusType.PROOF_OF_WORK;

    @Override
    public boolean validateBlock(Block block) {
        log.debug("Validating block {} with {} consensus",
                block.getBlockIndex(), currentConsensusType);

        return switch (currentConsensusType) {
            case PROOF_OF_WORK -> validateWithPoW(block);
            case PROOF_OF_STAKE -> validateWithPoS(block);
            case HYBRID -> validateWithPoW(block) && validateWithPoS(block);
        };
    }

    @Override
    public void setConsensusType(ConsensusType consensusType) {
        log.info("Switching consensus from {} to {}", currentConsensusType, consensusType);
        this.currentConsensusType = consensusType;
    }

    @Override
    public ConsensusType getCurrentConsensusType() {
        return currentConsensusType;
    }

    @Override
    public boolean validateWithPoW(Block block) {
        try {
            boolean isValid = powConsensusService.validateProofOfWork(block, block.getDifficulty());
            log.debug("PoW validation result: {}", isValid);
            return isValid;
        } catch (Exception e) {
            log.error("PoW validation failed", e);
            return false;
        }
    }

    @Override
    public boolean validateWithPoS(Block block) {
        try {
            String minerAddress = block.getMinerAddress();
            if (minerAddress == null || minerAddress.isEmpty()) {
                log.warn("No miner address for PoS validation");
                return false;
            }

            boolean isValid = posConsensusService.validateStake(block, minerAddress);
            log.debug("PoS validation result: {}", isValid);
            return isValid;
        } catch (Exception e) {
            log.error("PoS validation failed", e);
            return false;
        }
    }

    @Override
    public boolean isValidatorEligible(String validatorAddress) {
        return posConsensusService.hasMinimumStake(validatorAddress);
    }

    @Override
    public String selectNextValidator() {
        if (currentConsensusType == ConsensusType.PROOF_OF_STAKE ||
                currentConsensusType == ConsensusType.HYBRID) {
            return posConsensusService.selectValidator();
        }

        log.warn("Cannot select validator for PoW consensus");
        return null;
    }
}