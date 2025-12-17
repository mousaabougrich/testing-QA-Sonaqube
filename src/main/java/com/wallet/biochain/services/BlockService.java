package com.wallet.biochain.services;

import com.wallet.biochain.dto.BlockDTO;
import com.wallet.biochain.dto.BlockValidationDTO;
import com.wallet.biochain.entities.Block;
import com.wallet.biochain.entities.Transaction;

import java.util.List;
import java.util.Optional;

public interface BlockService {

    /**
     * Create a new block
     */
    Block createBlock(Integer blockIndex, String previousHash, List<Transaction> transactions);

    /**
     * Get block by ID
     */
    Optional<BlockDTO> getBlockById(Long id);

    /**
     * Get block by hash
     */
    Optional<BlockDTO> getBlockByHash(String hash);

    /**
     * Get block by index
     */
    Optional<BlockDTO> getBlockByIndex(Integer blockIndex);

    /**
     * Get latest block
     */
    Optional<BlockDTO> getLatestBlock();

    /**
     * Get all blocks
     */
    List<BlockDTO> getAllBlocks();

    /**
     * Get blocks in range
     */
    List<BlockDTO> getBlocksInRange(Integer startIndex, Integer endIndex);

    /**
     * Get blocks by miner
     */
    List<BlockDTO> getBlocksByMiner(String minerAddress);

    /**
     * Calculate block hash
     */
    String calculateBlockHash(Block block);

    /**
     * Validate block
     */
    BlockValidationDTO validateBlock(Block block);

    /**
     * Validate block chain
     */
    boolean validateBlockchain(List<Block> blocks);

    /**
     * Add block to blockchain
     */
    Block addBlock(Block block);

    /**
     * Get block count
     */
    Long getBlockCount();

    /**
     * Get blocks mined by address count
     */
    Long getBlocksMinedCount(String minerAddress);

    /**
     * Calculate merkle root for transactions
     */
    String calculateMerkleRoot(List<Transaction> transactions);
}