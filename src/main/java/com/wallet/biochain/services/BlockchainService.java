package com.wallet.biochain.services;

import com.wallet.biochain.dto.BlockchainStatusDTO;
import com.wallet.biochain.entities.Blockchain;
import com.wallet.biochain.entities.Block;
import com.wallet.biochain.enums.ConsensusType;

import java.util.List;
import java.util.Optional;

public interface BlockchainService {

    /**
     * Initialize blockchain with genesis block
     */
    Blockchain initializeBlockchain(String name, ConsensusType consensusType);

    /**
     * Get blockchain by ID
     */
    Optional<Blockchain> getBlockchainById(Long id);

    /**
     * Get blockchain by chain ID
     */
    Optional<Blockchain> getBlockchainByChainId(String chainId);

    /**
     * Get blockchain status
     */
    BlockchainStatusDTO getBlockchainStatus(String chainId);

    /**
     * Get all blockchains
     */
    List<Blockchain> getAllBlockchains();

    /**
     * Add block to blockchain
     */
    void addBlockToChain(String chainId, Block block);

    /**
     * Validate entire blockchain
     */
    boolean validateBlockchain(String chainId);

    /**
     * Get blockchain height
     */
    Integer getBlockchainHeight(String chainId);

    /**
     * Update blockchain difficulty
     */
    void updateDifficulty(String chainId, Integer newDifficulty);

    /**
     * Get genesis block
     */
    Block getGenesisBlock(String chainId);

    /**
     * Check blockchain integrity
     */
    boolean checkIntegrity(String chainId);

    /**
     * Get total transactions in blockchain
     */
    Integer getTotalTransactions(String chainId);

    /**
     * Synchronize blockchain
     */
    void synchronizeBlockchain(String chainId);
}