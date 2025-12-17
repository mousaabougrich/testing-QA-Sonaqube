package com.wallet.biochain.services.impl;

import com.wallet.biochain.config.NetworkConfig;
import com.wallet.biochain.entities.Block;
import com.wallet.biochain.entities.Node;
import com.wallet.biochain.enums.NodeStatus;
import com.wallet.biochain.repositories.BlockRepository;
import com.wallet.biochain.repositories.NodeRepository;
import com.wallet.biochain.services.BlockService;
import com.wallet.biochain.services.BlockchainService;
import com.wallet.biochain.services.BlockchainSyncService;
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
public class BlockchainSyncServiceImpl implements BlockchainSyncService {

    private final BlockRepository blockRepository;
    private final NodeRepository nodeRepository;
    private final BlockService blockService;
    private final BlockchainService blockchainService;
    private final NetworkConfig networkConfig;

    @Override
    @Transactional
    public void synchronize() {
        log.info("Starting blockchain synchronization");

        List<Node> activeNodes = nodeRepository.findByStatus(NodeStatus.ACTIVE);
        if (activeNodes.isEmpty()) {
            log.warn("No active nodes available for synchronization");
            return;
        }

        // Find node with highest block height
        Node bestNode = activeNodes.stream()
                .filter(node -> node.getBlockHeight() != null)
                .max((n1, n2) -> Integer.compare(n1.getBlockHeight(), n2.getBlockHeight()))
                .orElse(null);

        if (bestNode != null && bestNode.getBlockHeight() != null) {
            log.info("Selected peer {} with block height {} for synchronization",
                    bestNode.getNodeId(), bestNode.getBlockHeight());
            synchronizeWithPeer(bestNode.getNodeId());
        } else {
            log.warn("No suitable peer found for synchronization");
        }

        log.info("Blockchain synchronization completed");
    }

    @Override
    @Transactional
    public void synchronizeWithPeer(String peerNodeId) {
        log.info("Synchronizing with peer: {}", peerNodeId);

        Node peer = nodeRepository.findByNodeId(peerNodeId)
                .orElseThrow(() -> new IllegalArgumentException("Peer not found: " + peerNodeId));

        Integer currentHeight = blockRepository.findMaxBlockIndex();
        Integer peerHeight = peer.getBlockHeight();

        if (currentHeight == null) {
            currentHeight = -1;
        }

        if (peerHeight == null || peerHeight <= currentHeight) {
            log.info("Already up to date. Current height: {}, Peer height: {}",
                    currentHeight, peerHeight);
            return;
        }

        log.info("Peer is ahead. Current height: {}, Peer height: {}", currentHeight, peerHeight);

        // Request missing blocks in batches
        int batchSize = networkConfig.getSync().getBatchSize();
        int fromIndex = currentHeight + 1;

        while (fromIndex <= peerHeight) {
            int toIndex = Math.min(fromIndex + batchSize - 1, peerHeight);

            log.debug("Requesting blocks {}-{} from peer {}", fromIndex, toIndex, peerNodeId);

            try {
                List<Block> missingBlocks = requestBlocksFromPeer(peerNodeId, fromIndex, toIndex);

                if (missingBlocks.isEmpty()) {
                    log.warn("No blocks received from peer, stopping synchronization");
                    break;
                }

                if (validateReceivedBlocks(missingBlocks)) {
                    addSynchronizedBlocks(missingBlocks);
                    fromIndex = toIndex + 1;
                } else {
                    log.error("Received blocks validation failed, stopping synchronization");
                    break;
                }
            } catch (Exception e) {
                log.error("Error during synchronization", e);
                break;
            }
        }

        log.info("Synchronization with peer {} completed", peerNodeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Block> getMissingBlocks(Integer fromIndex, Integer toIndex) {
        log.debug("Identifying missing blocks from {} to {}", fromIndex, toIndex);

        List<Block> allBlocks = blockRepository.findBlocksInRange(fromIndex, toIndex);
        List<Block> missingBlocks = new ArrayList<>();

        // Check which blocks are missing in the range
        for (int i = fromIndex; i <= toIndex; i++) {
            final int index = i;
            boolean exists = allBlocks.stream()
                    .anyMatch(b -> b.getBlockIndex().equals(index));

            if (!exists) {
                log.debug("Block {} is missing", index);
                // Create placeholder for missing block (would be filled by network request)
                Block missingBlock = new Block();
                missingBlock.setBlockIndex(index);
                missingBlocks.add(missingBlock);
            }
        }

        log.debug("Found {} missing blocks", missingBlocks.size());
        return missingBlocks;
    }

    @Override
    public List<Block> requestBlocksFromPeer(String peerNodeId, Integer fromIndex, Integer toIndex) {
        log.info("Requesting blocks {}-{} from peer {}", fromIndex, toIndex, peerNodeId);

        Node peer = nodeRepository.findByNodeId(peerNodeId)
                .orElseThrow(() -> new IllegalArgumentException("Peer not found: " + peerNodeId));

        List<Block> receivedBlocks = new ArrayList<>();

        try {
            // In production, this would make an actual network call to the peer
            // For now, simulate by fetching from local database (if they exist)
            receivedBlocks = blockRepository.findBlocksInRange(fromIndex, toIndex);

            // In a real P2P implementation, you would:
            // 1. Send HTTP/WebSocket request to peer's node
            // 2. Peer would query its local blockchain
            // 3. Peer would return serialized blocks
            // 4. Deserialize and return blocks

            log.info("Received {} blocks from peer {}", receivedBlocks.size(), peerNodeId);

            // Update peer's last seen time
            peer.setLastSeen(java.time.LocalDateTime.now());
            nodeRepository.save(peer);

        } catch (Exception e) {
            log.error("Failed to request blocks from peer: {}", peerNodeId, e);

            // Mark peer as problematic if request fails
            peer.setStatus(NodeStatus.SYNCING);
            nodeRepository.save(peer);
        }

        return receivedBlocks;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateReceivedBlocks(List<Block> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            log.debug("No blocks to validate");
            return true;
        }

        log.debug("Validating {} received blocks", blocks.size());

        // Validate each block individually
        for (Block block : blocks) {
            var validationResult = blockService.validateBlock(block);
            if (!validationResult.isValid()) {
                log.error("Invalid block detected at index {}: {}",
                        block.getBlockIndex(), validationResult.message());
                return false;
            }
        }

        // Validate chain continuity
        for (int i = 1; i < blocks.size(); i++) {
            Block current = blocks.get(i);
            Block previous = blocks.get(i - 1);

            if (!current.getPreviousHash().equals(previous.getHash())) {
                log.error("Chain continuity broken between blocks {} and {}",
                        previous.getBlockIndex(), current.getBlockIndex());
                return false;
            }

            if (current.getBlockIndex() != previous.getBlockIndex() + 1) {
                log.error("Block index sequence invalid: {} -> {}",
                        previous.getBlockIndex(), current.getBlockIndex());
                return false;
            }
        }

        // Validate connection to existing chain
        if (!blocks.isEmpty()) {
            Block firstNewBlock = blocks.get(0);
            Integer expectedIndex = firstNewBlock.getBlockIndex();

            if (expectedIndex > 0) {
                Optional<Block> previousBlock = blockRepository.findByBlockIndex(expectedIndex - 1);

                if (previousBlock.isPresent()) {
                    if (!firstNewBlock.getPreviousHash().equals(previousBlock.get().getHash())) {
                        log.error("New blocks do not connect to existing chain");
                        return false;
                    }
                }
            }
        }

        log.info("All received blocks validated successfully");
        return true;
    }

    @Override
    @Transactional
    public void addSynchronizedBlocks(List<Block> blocks) {
        log.info("Adding {} synchronized blocks to chain", blocks.size());

        int successCount = 0;
        int failCount = 0;

        for (Block block : blocks) {
            try {
                // Check if block already exists
                if (blockRepository.existsByBlockIndex(block.getBlockIndex())) {
                    log.debug("Block {} already exists, skipping", block.getBlockIndex());
                    continue;
                }

                // Save block
                Block savedBlock = blockService.addBlock(block);

                // Update blockchain height if needed
                String chainId = networkConfig.getNode().getNodeId();
                try {
                    var blockchain = blockchainService.getBlockchainByChainId(chainId);
                    if (blockchain.isPresent() &&
                            savedBlock.getBlockIndex() > blockchain.get().getCurrentHeight()) {
                        blockchainService.addBlockToChain(chainId, savedBlock);
                    }
                } catch (Exception e) {
                    log.warn("Could not update blockchain height", e);
                }

                successCount++;
                log.debug("Added block {} to chain", block.getBlockIndex());

            } catch (Exception e) {
                log.error("Failed to add block {}: {}", block.getBlockIndex(), e.getMessage());
                failCount++;
            }
        }

        log.info("Synchronized blocks added: {} successful, {} failed", successCount, failCount);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean needsSynchronization() {
        Integer currentHeight = blockRepository.findMaxBlockIndex();
        if (currentHeight == null) {
            currentHeight = -1;
        }

        // Check if any active peer has higher block height
        List<Node> activeNodes = nodeRepository.findByStatus(NodeStatus.ACTIVE);

        for (Node node : activeNodes) {
            if (node.getBlockHeight() != null && node.getBlockHeight() > currentHeight) {
                log.info("Synchronization needed. Current height: {}, Peer {} height: {}",
                        currentHeight, node.getNodeId(), node.getBlockHeight());
                return true;
            }
        }

        log.debug("No synchronization needed. Current height: {}", currentHeight);
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getSyncProgress() {
        Integer currentHeight = blockRepository.findMaxBlockIndex();
        if (currentHeight == null) {
            currentHeight = 0;
        }

        // Find maximum height among active peers
        List<Node> activeNodes = nodeRepository.findByStatus(NodeStatus.ACTIVE);
        Integer maxPeerHeight = activeNodes.stream()
                .map(Node::getBlockHeight)
                .filter(h -> h != null)
                .max(Integer::compare)
                .orElse(currentHeight);

        if (maxPeerHeight == 0) {
            return 100;
        }

        int progress = (int) ((currentHeight * 100.0) / maxPeerHeight);
        log.debug("Sync progress: {}% ({}/{})", progress, currentHeight, maxPeerHeight);

        return progress;
    }

    @Override
    @Transactional
    public void resolveConflicts() {
        log.info("Resolving blockchain conflicts");

        List<Node> activeNodes = nodeRepository.findByStatus(NodeStatus.ACTIVE);

        if (activeNodes.isEmpty()) {
            log.warn("No active nodes to resolve conflicts with");
            return;
        }

        // Find node with longest valid chain
        Node longestChainNode = activeNodes.stream()
                .filter(node -> node.getBlockHeight() != null)
                .max((n1, n2) -> Integer.compare(n1.getBlockHeight(), n2.getBlockHeight()))
                .orElse(null);

        if (longestChainNode == null) {
            log.warn("No suitable node found for conflict resolution");
            return;
        }

        Integer currentHeight = blockRepository.findMaxBlockIndex();
        if (currentHeight == null) {
            currentHeight = -1;
        }

        Integer peerHeight = longestChainNode.getBlockHeight();

        if (peerHeight > currentHeight) {
            log.info("Adopting longer chain from node: {} (height: {})",
                    longestChainNode.getNodeId(), peerHeight);

            // Synchronize with the peer that has the longest chain
            synchronizeWithPeer(longestChainNode.getNodeId());

            log.info("Conflict resolution completed");
        } else {
            log.info("Current chain is already the longest, no conflict to resolve");
        }
    }

    /**
     * Check if a specific block exists locally
     */
    private boolean blockExists(Integer blockIndex) {
        return blockRepository.existsByBlockIndex(blockIndex);
    }

    /**
     * Get blocks that need to be synchronized
     */
    private List<Integer> getBlocksToSync(Integer fromIndex, Integer toIndex) {
        List<Integer> blocksToSync = new ArrayList<>();

        for (int i = fromIndex; i <= toIndex; i++) {
            if (!blockExists(i)) {
                blocksToSync.add(i);
            }
        }

        return blocksToSync;
    }
}