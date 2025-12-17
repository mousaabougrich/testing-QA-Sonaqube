package com.wallet.biochain.services;

import com.wallet.biochain.entities.Block;

import java.util.List;

public interface BlockchainSyncService {

    /**
     * Synchronize blockchain with network
     */
    void synchronize();

    /**
     * Synchronize with specific peer
     */
    void synchronizeWithPeer(String peerNodeId);

    /**
     * Get missing blocks
     */
    List<Block> getMissingBlocks(Integer fromIndex, Integer toIndex);

    /**
     * Request blocks from peer
     */
    List<Block> requestBlocksFromPeer(String peerNodeId, Integer fromIndex, Integer toIndex);

    /**
     * Validate received blocks
     */
    boolean validateReceivedBlocks(List<Block> blocks);

    /**
     * Add synchronized blocks to chain
     */
    void addSynchronizedBlocks(List<Block> blocks);

    /**
     * Check if synchronization is needed
     */
    boolean needsSynchronization();

    /**
     * Get synchronization progress
     */
    Integer getSyncProgress();

    /**
     * Resolve blockchain conflicts
     */
    void resolveConflicts();
}