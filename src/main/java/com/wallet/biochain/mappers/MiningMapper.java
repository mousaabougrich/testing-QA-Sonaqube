package com.wallet.biochain.mappers;

import com.wallet.biochain.dto.MiningResultDTO;
import com.wallet.biochain.entities.Block;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class MiningMapper {

    public MiningResultDTO toResultDTO(
            Block block,
            Long miningDuration,
            BigDecimal reward,
            boolean success,
            String message
    ) {
        if (block == null) {
            return new MiningResultDTO(
                    success,
                    null,
                    null,
                    null,
                    miningDuration,
                    null,
                    null,
                    reward,
                    0,
                    LocalDateTime.now(),
                    message
            );
        }

        LocalDateTime timestamp = block.getTimestamp() != null
                ? LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(block.getTimestamp()),
                ZoneId.systemDefault()
        )
                : LocalDateTime.now();

        return new MiningResultDTO(
                success,
                block.getHash(),
                block.getBlockIndex(),
                block.getNonce(),
                miningDuration,
                block.getDifficulty(),
                block.getMinerAddress(),
                reward,
                block.getTransactions() != null ? block.getTransactions().size() : 0,
                timestamp,
                message
        );
    }

    public MiningResultDTO toResultDTO(Block block, Long miningDuration, BigDecimal reward) {
        return toResultDTO(block, miningDuration, reward, true, "Block mined successfully");
    }

    public MiningResultDTO toFailedResultDTO(String message) {
        return toResultDTO(null, null, null, false, message);
    }
}