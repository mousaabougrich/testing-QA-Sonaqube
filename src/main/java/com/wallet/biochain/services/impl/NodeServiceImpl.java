package com.wallet.biochain.services.impl;

import com.wallet.biochain.config.NetworkConfig;
import com.wallet.biochain.dto.NodeConnectionRequestDTO;
import com.wallet.biochain.dto.NodeInfoDTO;
import com.wallet.biochain.dto.PeerListDTO;
import com.wallet.biochain.entities.Node;
import com.wallet.biochain.enums.NodeStatus;
import com.wallet.biochain.enums.NodeType;
import com.wallet.biochain.mappers.NodeMapper;
import com.wallet.biochain.repositories.NodeRepository;
import com.wallet.biochain.services.NodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NodeServiceImpl implements NodeService {

    private final NodeRepository nodeRepository;
    private final NodeMapper nodeMapper;
    private final NetworkConfig networkConfig;

    @Override
    @Transactional
    public NodeInfoDTO registerNode(NodeConnectionRequestDTO request) {
        log.info("Registering node: {}", request.nodeId());

        // Check if node already exists
        Optional<Node> existing = nodeRepository.findByNodeId(request.nodeId());
        if (existing.isPresent()) {
            log.info("Node already registered, updating: {}", request.nodeId());
            Node node = existing.get();
            node.setStatus(NodeStatus.ACTIVE);
            node.setLastSeen(LocalDateTime.now());
            return nodeMapper.toDTO(nodeRepository.save(node));
        }

        // Create new node
        Node node = new Node();
        node.setNodeId(request.nodeId());
        node.setIpAddress(request.ipAddress());
        node.setPort(request.port());
        node.setNodeType(request.nodeType());
        node.setStatus(NodeStatus.ACTIVE);
        node.setVersion(request.version());
        node.setBlockHeight(0);
        node.setIsTrusted(false);
        node.setConnectionCount(0);

        Node savedNode = nodeRepository.save(node);
        log.info("Node registered successfully: {}", request.nodeId());

        return nodeMapper.toDTO(savedNode);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NodeInfoDTO> getNodeById(Long id) {
        return nodeRepository.findById(id).map(nodeMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NodeInfoDTO> getNodeByNodeId(String nodeId) {
        return nodeRepository.findByNodeId(nodeId).map(nodeMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NodeInfoDTO> getAllNodes() {
        return nodeMapper.toDTOList(nodeRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NodeInfoDTO> getActiveNodes() {
        return nodeMapper.toDTOList(nodeRepository.findByStatus(NodeStatus.ACTIVE));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NodeInfoDTO> getNodesByStatus(NodeStatus status) {
        return nodeMapper.toDTOList(nodeRepository.findByStatus(status));
    }

    @Override
    @Transactional
    public void updateNodeStatus(String nodeId, NodeStatus status) {
        Node node = nodeRepository.findByNodeId(nodeId)
                .orElseThrow(() -> new IllegalArgumentException("Node not found: " + nodeId));

        node.setStatus(status);
        node.setLastSeen(LocalDateTime.now());
        nodeRepository.save(node);

        log.info("Node status updated: {} -> {}", nodeId, status);
    }

    @Override
    @Transactional
    public void updateBlockHeight(String nodeId, Integer blockHeight) {
        Node node = nodeRepository.findByNodeId(nodeId)
                .orElseThrow(() -> new IllegalArgumentException("Node not found: " + nodeId));

        node.setBlockHeight(blockHeight);
        node.setLastSeen(LocalDateTime.now());
        nodeRepository.save(node);
    }

    @Override
    @Transactional
    public void connectToPeer(String nodeId, String peerNodeId) {
        Node node = nodeRepository.findByNodeId(nodeId)
                .orElseThrow(() -> new IllegalArgumentException("Node not found: " + nodeId));

        Node peer = nodeRepository.findByNodeId(peerNodeId)
                .orElseThrow(() -> new IllegalArgumentException("Peer not found: " + peerNodeId));

        if (!node.getPeers().contains(peer)) {
            node.getPeers().add(peer);
            node.setConnectionCount(node.getPeers().size());
            nodeRepository.save(node);
            log.info("Node {} connected to peer {}", nodeId, peerNodeId);
        }
    }

    @Override
    @Transactional
    public void disconnectFromPeer(String nodeId, String peerNodeId) {
        Node node = nodeRepository.findByNodeId(nodeId)
                .orElseThrow(() -> new IllegalArgumentException("Node not found: " + nodeId));

        Node peer = nodeRepository.findByNodeId(peerNodeId)
                .orElseThrow(() -> new IllegalArgumentException("Peer not found: " + peerNodeId));

        if (node.getPeers().remove(peer)) {
            node.setConnectionCount(node.getPeers().size());
            nodeRepository.save(node);
            log.info("Node {} disconnected from peer {}", nodeId, peerNodeId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PeerListDTO getPeerList(String nodeId) {
        Node node = nodeRepository.findByNodeId(nodeId)
                .orElseThrow(() -> new IllegalArgumentException("Node not found: " + nodeId));

        return nodeMapper.toPeerListDTO(node.getPeers());
    }

    @Override
    public boolean pingNode(String nodeId) {
        try {
            Node node = nodeRepository.findByNodeId(nodeId)
                    .orElseThrow(() -> new IllegalArgumentException("Node not found: " + nodeId));

            // Simulate ping (in production, actual network ping)
            node.setLastSeen(LocalDateTime.now());
            nodeRepository.save(node);

            return true;
        } catch (Exception e) {
            log.error("Failed to ping node: {}", nodeId, e);
            return false;
        }
    }

    @Override
    public Long getNodeLatency(String nodeId) {
        Node node = nodeRepository.findByNodeId(nodeId)
                .orElseThrow(() -> new IllegalArgumentException("Node not found: " + nodeId));

        // Simulate latency measurement (in production, actual ping)
        long latency = 50L + (long) (Math.random() * 200); // 50-250ms
        node.setLatencyMs(latency);
        nodeRepository.save(node);

        return latency;
    }

    @Override
    @Transactional
    public void trustNode(String nodeId) {
        Node node = nodeRepository.findByNodeId(nodeId)
                .orElseThrow(() -> new IllegalArgumentException("Node not found: " + nodeId));

        node.setIsTrusted(true);
        nodeRepository.save(node);
        log.info("Node marked as trusted: {}", nodeId);
    }

    @Override
    @Transactional
    public void untrustNode(String nodeId) {
        Node node = nodeRepository.findByNodeId(nodeId)
                .orElseThrow(() -> new IllegalArgumentException("Node not found: " + nodeId));

        node.setIsTrusted(false);
        nodeRepository.save(node);
        log.info("Node marked as untrusted: {}", nodeId);
    }

    @Override
    @Transactional
    public void removeStaleNodes() {
        LocalDateTime threshold = LocalDateTime.now()
                .minusSeconds(networkConfig.getP2p().getPingInterval() / 1000 * 5);

        List<Node> staleNodes = nodeRepository.findStaleNodes(threshold);

        for (Node node : staleNodes) {
            node.setStatus(NodeStatus.DISCONNECTED);
            nodeRepository.save(node);
        }

        log.info("Marked {} stale nodes as disconnected", staleNodes.size());
    }

    @Override
    @Transactional
    public void synchronizeWithPeer(String nodeId, String peerNodeId) {
        log.info("Synchronizing node {} with peer {}", nodeId, peerNodeId);
        // Delegate to BlockchainSyncService
    }
}