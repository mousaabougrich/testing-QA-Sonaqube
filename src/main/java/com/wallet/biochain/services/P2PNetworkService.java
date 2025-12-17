package com.wallet.biochain.services;

import com.wallet.biochain.dto.NetworkStatusDTO;
import com.wallet.biochain.entities.Block;
import com.wallet.biochain.entities.Transaction;

import java.util.List;

public interface P2PNetworkService {

    /**
     * Broadcast block to network
     */
    void broadcastBlock(Block block);

    /**
     * Broadcast transaction to network
     */
    void broadcastTransaction(Transaction transaction);

    /**
     * Discover peers
     */
    List<String> discoverPeers();

    /**
     * Connect to network
     */
    void connectToNetwork();

    /**
     * Disconnect from network
     */
    void disconnectFromNetwork();

    /**
     * Get network status
     */
    NetworkStatusDTO getNetworkStatus();

    /**
     * Send message to peer
     */
    void sendMessageToPeer(String nodeId, String message);

    /**
     * Broadcast message to all peers
     */
    void broadcastMessage(String message);

    /**
     * Request blockchain from peer
     */
    void requestBlockchainFromPeer(String nodeId);

    /**
     * Synchronize with network
     */
    void synchronizeWithNetwork();

    /**
     * Check network health
     */
    boolean isNetworkHealthy();

    /**
     * Get connected peers count
     */
    Integer getConnectedPeersCount();
}