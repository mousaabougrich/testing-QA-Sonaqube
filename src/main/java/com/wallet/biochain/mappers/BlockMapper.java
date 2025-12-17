package com.wallet.biochain.mappers;

import com.wallet.biochain.dto.BlockDTO;
import com.wallet.biochain.dto.BlockMinedEventDTO;
import com.wallet.biochain.dto.BlockValidationDTO;
import com.wallet.biochain.entities.Block;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BlockMapper {

    private final TransactionMapper transactionMapper;

    public BlockMapper(TransactionMapper transactionMapper) {
        this.transactionMapper = transactionMapper;
    }

    public BlockDTO toDTO(Block block) {
        if (block == null) {
            return null;
        }

        return new BlockDTO(
                block.getId(),
                block.getBlockIndex(),
                block.getHash(),
                block.getPreviousHash(),
                block.getTimestamp(),
                block.getNonce(),
                block.getDifficulty(),
                block.getMerkleRoot(),
                block.getMinerAddress(),
                block.getTransactions() != null ? block.getTransactions().size() : 0,
                transactionMapper.toResponseDTOList(block.getTransactions()),
                block.getCreatedAt()
        );
    }

    public BlockDTO toDTOWithoutTransactions(Block block) {
        if (block == null) {
            return null;
        }

        return new BlockDTO(
                block.getId(),
                block.getBlockIndex(),
                block.getHash(),
                block.getPreviousHash(),
                block.getTimestamp(),
                block.getNonce(),
                block.getDifficulty(),
                block.getMerkleRoot(),
                block.getMinerAddress(),
                block.getTransactions() != null ? block.getTransactions().size() : 0,
                null,
                block.getCreatedAt()
        );
    }

    public List<BlockDTO> toDTOList(List<Block> blocks) {
        if (blocks == null) {
            return null;
        }

        return blocks.stream()
                .map(this::toDTOWithoutTransactions)
                .collect(Collectors.toList());
    }

    public BlockMinedEventDTO toMinedEventDTO(Block block, java.math.BigDecimal reward) {
        if (block == null) {
            return null;
        }

        return new BlockMinedEventDTO(
                block.getBlockIndex(),
                block.getHash(),
                block.getMinerAddress(),
                reward,
                block.getTransactions() != null ? block.getTransactions().size() : 0,
                block.getTimestamp()
        );
    }

    public BlockValidationDTO toValidationDTO(boolean isValid, Block block, List<String> errors) {
        return new BlockValidationDTO(
                isValid,
                block != null ? block.getHash() : null,
                block != null ? block.getBlockIndex() : null,
                errors,
                isValid ? "Block is valid" : "Block validation failed"
        );
    }
}