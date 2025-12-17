package com.wallet.biochain.services.impl;

import com.wallet.biochain.config.NetworkConfig;
import com.wallet.biochain.dto.NetworkStatusDTO;
import com.wallet.biochain.entities.Block;
import com.wallet.biochain.entities.Node;
import com.wallet.biochain.entities.Transaction;
import com.wallet.biochain.enums.NodeStatus;
import com.wallet.biochain.mappers.NetworkMapper;
import com.wallet.biochain.repositories.NodeRepository;
import com.wallet.biochain.services.P2PNetworkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class P2PNetworkServiceImpl implements P2PNetworkService {

    private final NodeRepository nodeRepository;
    private final NetworkMapper networkMapper;
    private final NetworkConfig networkConfig;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void broadcastBlock(Block block) {
        log.info("Broadcasting block {} to network", block.getBlockIndex());

        List<Node> activeNodes = nodeRepository.findByStatus(NodeStatus.ACTIVE);
        int successCount = 0;

        for (Node node : activeNodes) {
            try {
                sendBlockToNode(node, block);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to send block to node: {}", node.getNodeId(), e);
            }
        }

        log.info("Block broadcasted to {}/{} nodes", successCount, activeNodes.size());

        // Broadcast via WebSocket
        messagingTemplate.convertAndSend("/topic/blocks", block);
    }

    @Override
    public void broadcastTransaction(Transaction transaction) {
        log.info("Broadcasting transaction {} to network", transaction.getTransactionHash());

        List<Node> activeNodes = nodeRepository.findByStatus(NodeStatus.ACTIVE);
        int successCount = 0;

        for (Node node : activeNodes) {
            try {
                sendTransactionToNode(node, transaction);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to send transaction to node: {}", node.getNodeId(), e);
            }
        }

        log.info("Transaction broadcasted to {}/{} nodes", successCount, activeNodes.size());

        // Broadcast via WebSocket
        messagingTemplate.convertAndSend("/topic/transactions", transaction);
    }

    @Override
    public List<String> discoverPeers() {
        log.info("Discovering peers...");

        List<String> discoveredPeers = new ArrayList<>();

        // Check seed nodes from config
        for (String seedNode : networkConfig.getP2p().getSeedNodes()) {
            try {
                // In production, actually connect to seed node
                discoveredPeers.add(seedNode);
                log.debug("Discovered seed node: {}", seedNode);
            } catch (Exception e) {
                log.error("Failed to connect to seed node: {}", seedNode, e);
            }
        }

        // Query existing nodes for their peers
        List<Node> activeNodes = nodeRepository.findByStatus(NodeStatus.ACTIVE);
        for (Node node : activeNodes) {
            for (Node peer : node.getPeers()) {
                String peerAddress = peer.getIpAddress() + ":" + peer.getPort();
                if (!discoveredPeers.contains(peerAddress)) {
                    discoveredPeers.add(peerAddress);
                }
            }
        }

        log.info("Discovered {} peers", discoveredPeers.size());
        return discoveredPeers;
    }

    @Override
    public void connectToNetwork() {
        log.info("Connecting to P2P network...");

        if (!networkConfig.getNode().getEnabled()) {
            log.warn("P2P network is disabled in configuration");
            return;
        }

        // Discover and connect to peers
        List<String> peers = discoverPeers();

        int targetPeers = Math.min(peers.size(), networkConfig.getP2p().getMaxPeers());
        log.info("Attempting to connect to {} peers", targetPeers);

        // In production, actually establish connections
        log.info("Connected to P2P network");
    }

    @Override
    public void disconnectFromNetwork() {
        log.info("Disconnecting from P2P network...");

        List<Node> activeNodes = nodeRepository.findByStatus(NodeStatus.ACTIVE);
        for (Node node : activeNodes) {
            try {
                // In production, properly close connections
                node.setStatus(NodeStatus.DISCONNECTED);
                nodeRepository.save(node);
            } catch (Exception e) {
                log.error("Error disconnecting node: {}", node.getNodeId(), e);
            }
        }

        log.info("Disconnected from P2P network");
    }

    @Override
    public NetworkStatusDTO getNetworkStatus() {
        List<Node> allNodes = nodeRepository.findAll();
        return networkMapper.toStatusDTO(allNodes);
    }

    @Override
    public void sendMessageToPeer(String nodeId, String message) {
        log.debug("Sending message to node: {}", nodeId);

        Node node = nodeRepository.findByNodeId(nodeId)
                .orElseThrow(() -> new IllegalArgumentException("Node not found: " + nodeId));

        // In production, send actual network message
        log.debug("Message sent to {}: {}", nodeId, message);
    }

    @Override
    public void broadcastMessage(String message) {
        log.info("Broadcasting message to all peers");

        List<Node> activeNodes = nodeRepository.findByStatus(NodeStatus.ACTIVE);
        for (Node node : activeNodes) {
            try {
                sendMessageToPeer(node.getNodeId(), message);
            } catch (Exception e) {
                log.error("Failed to send message to node: {}", node.getNodeId(), e);
            }
        }

        // Broadcast via WebSocket
        messagingTemplate.convertAndSend("/topic/messages", message);
    }

    @Override
    public void requestBlockchainFromPeer(String nodeId) {
        log.info("Requesting blockchain from peer: {}", nodeId);
        sendMessageToPeer(nodeId, "REQUEST_BLOCKCHAIN");
    }

    @Override
    public void synchronizeWithNetwork() {
        log.info("Synchronizing with P2P network");
        // Delegate to BlockchainSyncService
    }

    @Override
    public boolean isNetworkHealthy() {
        long activeCount = nodeRepository.countByStatus(NodeStatus.ACTIVE);
        int minPeers = networkConfig.getP2p().getMinPeers();

        boolean healthy = activeCount >= minPeers;
        log.debug("Network health: {} active nodes (min: {})", activeCount, minPeers);

        return healthy;
    }

    @Override
    public Integer getConnectedPeersCount() {
        return Math.toIntExact(nodeRepository.countByStatus(NodeStatus.ACTIVE));
    }

    private void sendBlockToNode(Node node, Block block) {
        // In production, send via actual network protocol
        log.debug("Sending block {} to node {}", block.getBlockIndex(), node.getNodeId());
    }

    private void sendTransactionToNode(Node node, Transaction transaction) {
        // In production, send via actual network protocol
        log.debug("Sending transaction {} to node {}",
                transaction.getTransactionHash(), node.getNodeId());
    }
}