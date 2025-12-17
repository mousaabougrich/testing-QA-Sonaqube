package com.wallet.biochain.services.impl;

import com.wallet.biochain.dto.BlockDTO;
import com.wallet.biochain.dto.BlockValidationDTO;
import com.wallet.biochain.entities.Block;
import com.wallet.biochain.entities.Transaction;
import com.wallet.biochain.mappers.BlockMapper;
import com.wallet.biochain.repositories.BlockRepository;
import com.wallet.biochain.services.BlockService;
import com.wallet.biochain.services.CryptographyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockServiceImpl implements BlockService {

    private final BlockRepository blockRepository;
    private final CryptographyService cryptographyService;
    private final BlockMapper blockMapper;

    @Override
    @Transactional
    public Block createBlock(Integer blockIndex, String previousHash, List<Transaction> transactions) {
        log.info("Creating block at index: {}", blockIndex);

        Block block = new Block();
        block.setBlockIndex(blockIndex);
        block.setPreviousHash(previousHash);
        block.setTimestamp(System.currentTimeMillis());
        block.setTransactions(transactions != null ? transactions : new ArrayList<>());
        block.setNonce(0);
        block.setDifficulty(4);

        // Calculate merkle root
        String merkleRoot = calculateMerkleRoot(block.getTransactions());
        block.setMerkleRoot(merkleRoot);

        // Calculate block hash
        String hash = calculateBlockHash(block);
        block.setHash(hash);

        log.info("Block created with hash: {}", hash);
        return block;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BlockDTO> getBlockById(Long id) {
        return blockRepository.findById(id).map(blockMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BlockDTO> getBlockByHash(String hash) {
        return blockRepository.findByHash(hash).map(blockMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BlockDTO> getBlockByIndex(Integer blockIndex) {
        return blockRepository.findByBlockIndex(blockIndex).map(blockMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BlockDTO> getLatestBlock() {
        return blockRepository.findLatestBlock().map(blockMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlockDTO> getAllBlocks() {
        return blockMapper.toDTOList(blockRepository.findAllOrderByBlockIndexDesc());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlockDTO> getBlocksInRange(Integer startIndex, Integer endIndex) {
        return blockMapper.toDTOList(blockRepository.findBlocksInRange(startIndex, endIndex));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlockDTO> getBlocksByMiner(String minerAddress) {
        return blockMapper.toDTOList(blockRepository.findByMinerAddress(minerAddress));
    }

    @Override
    public String calculateBlockHash(Block block) {
        String data = block.getBlockIndex() +
                block.getPreviousHash() +
                block.getTimestamp() +
                block.getNonce() +
                block.getMerkleRoot();
        return cryptographyService.hash(data);
    }

    @Override
    @Transactional(readOnly = true)
    public BlockValidationDTO validateBlock(Block block) {
        List<String> errors = new ArrayList<>();

        // Validate hash
        String calculatedHash = calculateBlockHash(block);
        if (!calculatedHash.equals(block.getHash())) {
            errors.add("Block hash mismatch");
        }

        // Validate previous hash if not genesis block
        if (block.getBlockIndex() > 0) {
            Optional<Block> previousBlock = blockRepository.findByBlockIndex(block.getBlockIndex() - 1);
            if (previousBlock.isPresent()) {
                if (!block.getPreviousHash().equals(previousBlock.get().getHash())) {
                    errors.add("Previous hash mismatch");
                }
            } else {
                errors.add("Previous block not found");
            }
        }

        // Validate timestamp
        if (block.getTimestamp() == null || block.getTimestamp() <= 0) {
            errors.add("Invalid timestamp");
        }

        boolean isValid = errors.isEmpty();
        return blockMapper.toValidationDTO(isValid, block, errors);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateBlockchain(List<Block> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return false;
        }

        for (int i = 1; i < blocks.size(); i++) {
            Block currentBlock = blocks.get(i);
            Block previousBlock = blocks.get(i - 1);

            // Validate hash
            if (!calculateBlockHash(currentBlock).equals(currentBlock.getHash())) {
                log.warn("Invalid hash for block: {}", currentBlock.getBlockIndex());
                return false;
            }

            // Validate link
            if (!currentBlock.getPreviousHash().equals(previousBlock.getHash())) {
                log.warn("Invalid previous hash link at block: {}", currentBlock.getBlockIndex());
                return false;
            }
        }

        return true;
    }

    @Override
    @Transactional
    public Block addBlock(Block block) {
        log.info("Adding block to chain: {}", block.getBlockIndex());
        return blockRepository.save(block);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getBlockCount() {
        return blockRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getBlocksMinedCount(String minerAddress) {
        return blockRepository.countBlocksByMiner(minerAddress);
    }

    @Override
    public String calculateMerkleRoot(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return cryptographyService.hash("empty");
        }

        List<String> hashes = transactions.stream()
                .map(tx -> tx.getTransactionHash())
                .toList();

        while (hashes.size() > 1) {
            List<String> newHashes = new ArrayList<>();
            for (int i = 0; i < hashes.size(); i += 2) {
                String left = hashes.get(i);
                String right = (i + 1 < hashes.size()) ? hashes.get(i + 1) : left;
                String combined = cryptographyService.hash(left + right);
                newHashes.add(combined);
            }
            hashes = newHashes;
        }

        return hashes.get(0);
    }
}