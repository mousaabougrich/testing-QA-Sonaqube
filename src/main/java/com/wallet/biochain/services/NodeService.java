package com.wallet.biochain.services;

import com.wallet.biochain.dto.NodeInfoDTO;
import com.wallet.biochain.dto.NodeConnectionRequestDTO;
import com.wallet.biochain.dto.PeerListDTO;
import com.wallet.biochain.entities.Node;
import com.wallet.biochain.enums.NodeStatus;

import java.util.List;
import java.util.Optional;

public interface NodeService {

    /**
     * Register a new node
     */
    NodeInfoDTO registerNode(NodeConnectionRequestDTO request);

    /**
     * Get node by ID
     */
    Optional<NodeInfoDTO> getNodeById(Long id);

    /**
     * Get node by node ID
     */
    Optional<NodeInfoDTO> getNodeByNodeId(String nodeId);

    /**
     * Get all nodes
     */
    List<NodeInfoDTO> getAllNodes();

    /**
     * Get active nodes
     */
    List<NodeInfoDTO> getActiveNodes();

    /**
     * Get nodes by status
     */
    List<NodeInfoDTO> getNodesByStatus(NodeStatus status);

    /**
     * Update node status
     */
    void updateNodeStatus(String nodeId, NodeStatus status);

    /**
     * Update node block height
     */
    void updateBlockHeight(String nodeId, Integer blockHeight);

    /**
     * Connect to peer
     */
    void connectToPeer(String nodeId, String peerNodeId);

    /**
     * Disconnect from peer
     */
    void disconnectFromPeer(String nodeId, String peerNodeId);

    /**
     * Get peer list for node
     */
    PeerListDTO getPeerList(String nodeId);

    /**
     * Ping node
     */
    boolean pingNode(String nodeId);

    /**
     * Get node latency
     */
    Long getNodeLatency(String nodeId);

    /**
     * Mark node as trusted
     */
    void trustNode(String nodeId);

    /**
     * Mark node as untrusted
     */
    void untrustNode(String nodeId);

    /**
     * Remove stale nodes
     */
    void removeStaleNodes();

    /**
     * Synchronize with peer
     */
    void synchronizeWithPeer(String nodeId, String peerNodeId);
}