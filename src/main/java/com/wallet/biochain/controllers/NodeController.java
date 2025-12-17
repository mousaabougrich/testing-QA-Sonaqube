package com.wallet.biochain.controllers;

import com.wallet.biochain.dto.NodeConnectionRequestDTO;
import com.wallet.biochain.dto.NodeInfoDTO;
import com.wallet.biochain.dto.PeerListDTO;
import com.wallet.biochain.enums.NodeStatus;
import com.wallet.biochain.services.NodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/nodes")
@RequiredArgsConstructor
@Tag(name = "Node", description = "Node management endpoints for P2P network")
public class NodeController {

    private final NodeService nodeService;

    @PostMapping("/register")
    @Operation(summary = "Register node", description = "Registers a new node in the network")
    public ResponseEntity<NodeInfoDTO> registerNode(@RequestBody NodeConnectionRequestDTO request) {
        log.info("REST request to register node: {}", request.nodeId());

        try {
            NodeInfoDTO node = nodeService.registerNode(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(node);
        } catch (Exception e) {
            log.error("Failed to register node", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get node by ID", description = "Retrieves node information by ID")
    public ResponseEntity<NodeInfoDTO> getNodeById(@PathVariable Long id) {
        log.info("REST request to get node by ID: {}", id);

        return nodeService.getNodeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/node-id/{nodeId}")
    @Operation(summary = "Get node by node ID", description = "Retrieves node by unique node ID")
    public ResponseEntity<NodeInfoDTO> getNodeByNodeId(@PathVariable String nodeId) {
        log.info("REST request to get node by node ID: {}", nodeId);

        return nodeService.getNodeByNodeId(nodeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all nodes", description = "Retrieves all registered nodes")
    public ResponseEntity<List<NodeInfoDTO>> getAllNodes() {
        log.info("REST request to get all nodes");

        List<NodeInfoDTO> nodes = nodeService.getAllNodes();
        return ResponseEntity.ok(nodes);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active nodes", description = "Retrieves only active nodes")
    public ResponseEntity<List<NodeInfoDTO>> getActiveNodes() {
        log.info("REST request to get active nodes");

        List<NodeInfoDTO> nodes = nodeService.getActiveNodes();
        return ResponseEntity.ok(nodes);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get nodes by status", description = "Retrieves nodes filtered by status")
    public ResponseEntity<List<NodeInfoDTO>> getNodesByStatus(@PathVariable NodeStatus status) {
        log.info("REST request to get nodes by status: {}", status);

        List<NodeInfoDTO> nodes = nodeService.getNodesByStatus(status);
        return ResponseEntity.ok(nodes);
    }

    @PutMapping("/{nodeId}/status")
    @Operation(summary = "Update node status", description = "Updates the status of a node")
    public ResponseEntity<Void> updateNodeStatus(
            @PathVariable String nodeId,
            @RequestParam NodeStatus status) {
        log.info("REST request to update node status: {} to {}", nodeId, status);

        try {
            nodeService.updateNodeStatus(nodeId, status);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Node not found: {}", nodeId);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{nodeId}/block-height")
    @Operation(summary = "Update block height", description = "Updates node's blockchain height")
    public ResponseEntity<Void> updateBlockHeight(
            @PathVariable String nodeId,
            @RequestParam Integer blockHeight) {
        log.info("REST request to update block height for node: {} to {}", nodeId, blockHeight);

        try {
            nodeService.updateBlockHeight(nodeId, blockHeight);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{nodeId}/connect/{peerNodeId}")
    @Operation(summary = "Connect to peer", description = "Connects a node to another peer")
    public ResponseEntity<Void> connectToPeer(
            @PathVariable String nodeId,
            @PathVariable String peerNodeId) {
        log.info("REST request to connect node {} to peer {}", nodeId, peerNodeId);

        try {
            nodeService.connectToPeer(nodeId, peerNodeId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Failed to connect nodes", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{nodeId}/disconnect/{peerNodeId}")
    @Operation(summary = "Disconnect from peer", description = "Disconnects a node from a peer")
    public ResponseEntity<Void> disconnectFromPeer(
            @PathVariable String nodeId,
            @PathVariable String peerNodeId) {
        log.info("REST request to disconnect node {} from peer {}", nodeId, peerNodeId);

        try {
            nodeService.disconnectFromPeer(nodeId, peerNodeId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{nodeId}/peers")
    @Operation(summary = "Get peer list", description = "Gets list of peers connected to a node")
    public ResponseEntity<PeerListDTO> getPeerList(@PathVariable String nodeId) {
        log.info("REST request to get peer list for node: {}", nodeId);

        try {
            PeerListDTO peerList = nodeService.getPeerList(nodeId);
            return ResponseEntity.ok(peerList);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{nodeId}/ping")
    @Operation(summary = "Ping node", description = "Pings a node to check if it's alive")
    public ResponseEntity<Boolean> pingNode(@PathVariable String nodeId) {
        log.debug("REST request to ping node: {}", nodeId);

        boolean isAlive = nodeService.pingNode(nodeId);
        return ResponseEntity.ok(isAlive);
    }

    @GetMapping("/{nodeId}/latency")
    @Operation(summary = "Get node latency", description = "Measures network latency to a node")
    public ResponseEntity<Long> getNodeLatency(@PathVariable String nodeId) {
        log.debug("REST request to get latency for node: {}", nodeId);

        try {
            Long latency = nodeService.getNodeLatency(nodeId);
            return ResponseEntity.ok(latency);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{nodeId}/trust")
    @Operation(summary = "Trust node", description = "Marks a node as trusted")
    public ResponseEntity<Void> trustNode(@PathVariable String nodeId) {
        log.info("REST request to trust node: {}", nodeId);

        try {
            nodeService.trustNode(nodeId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{nodeId}/untrust")
    @Operation(summary = "Untrust node", description = "Removes trusted status from a node")
    public ResponseEntity<Void> untrustNode(@PathVariable String nodeId) {
        log.info("REST request to untrust node: {}", nodeId);

        try {
            nodeService.untrustNode(nodeId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/stale")
    @Operation(summary = "Remove stale nodes", description = "Removes nodes that haven't responded")
    public ResponseEntity<Void> removeStaleNodes() {
        log.info("REST request to remove stale nodes");

        nodeService.removeStaleNodes();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{nodeId}/sync/{peerNodeId}")
    @Operation(summary = "Synchronize with peer", description = "Triggers sync between two nodes")
    public ResponseEntity<Void> synchronizeWithPeer(
            @PathVariable String nodeId,
            @PathVariable String peerNodeId) {
        log.info("REST request to sync node {} with peer {}", nodeId, peerNodeId);

        try {
            nodeService.synchronizeWithPeer(nodeId, peerNodeId);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Failed to synchronize nodes", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}