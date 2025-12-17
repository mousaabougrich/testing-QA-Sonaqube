package com.wallet.biochain.services.impl;

import com.wallet.biochain.entities.Block;
import com.wallet.biochain.services.BlockService;
import com.wallet.biochain.services.PoWConsensusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PoWConsensusServiceImpl implements PoWConsensusService {

    private final BlockService blockService;

    @Override
    public Block mineBlock(Block block, Integer difficulty) {
        log.info("Mining block {} with difficulty {}", block.getBlockIndex(), difficulty);

        String target = getDifficultyTarget(difficulty);
        String hash;
        int nonce = 0;

        do {
            nonce++;
            block.setNonce(nonce);
            hash = calculateHash(block);

            if (nonce % 100000 == 0) {
                log.debug("Tried {} nonces...", nonce);
            }
        } while (!meetsTarget(hash, difficulty));

        block.setHash(hash);
        log.info("Block mined! Nonce: {}, Hash: {}", nonce, hash);
        return block;
    }

    @Override
    public boolean validateProofOfWork(Block block, Integer difficulty) {
        String hash = calculateHash(block);
        return meetsTarget(hash, difficulty) && hash.equals(block.getHash());
    }

    @Override
    public String calculateHash(Block block) {
        return blockService.calculateBlockHash(block);
    }

    @Override
    public boolean meetsTarget(String hash, Integer difficulty) {
        String target = getDifficultyTarget(difficulty);
        return hash.substring(0, difficulty).equals(target);
    }

    @Override
    public String getDifficultyTarget(Integer difficulty) {
        return "0".repeat(Math.max(0, difficulty));
    }
}