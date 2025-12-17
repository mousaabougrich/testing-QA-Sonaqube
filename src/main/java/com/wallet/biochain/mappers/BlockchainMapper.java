package com.wallet.biochain.mappers;

import com.wallet.biochain.dto.BlockchainStatusDTO;
import com.wallet.biochain.entities.Blockchain;
import com.wallet.biochain.entities.Block;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class BlockchainMapper {

    public BlockchainStatusDTO toStatusDTO(
            Blockchain blockchain,
            Integer totalTransactions,
            Integer pendingTransactions,
            Integer averageBlockTime,
            Block latestBlock
    ) {
        if (blockchain == null) {
            return null;
        }

        LocalDateTime lastBlockTime = null;
        String latestBlockHash = null;

        if (latestBlock != null) {
            latestBlockHash = latestBlock.getHash();
            if (latestBlock.getTimestamp() != null) {
                lastBlockTime = LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(latestBlock.getTimestamp()),
                        ZoneId.systemDefault()
                );
            }
        }

        return new BlockchainStatusDTO(
                blockchain.getChainId(),
                blockchain.getName(),
                blockchain.getCurrentHeight(),
                latestBlockHash,
                blockchain.getDifficulty(),
                blockchain.getBlockReward(),
                blockchain.getConsensusType(),
                totalTransactions,
                pendingTransactions,
                blockchain.getIsValid(),
                averageBlockTime,
                lastBlockTime
        );
    }

    public BlockchainStatusDTO toStatusDTO(Blockchain blockchain) {
        return toStatusDTO(blockchain, 0, 0, null, null);
    }
}