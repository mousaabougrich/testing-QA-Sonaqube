package com.wallet.biochain.controllers;

import com.wallet.biochain.dto.NetworkStatusDTO;
import com.wallet.biochain.entities.Block;
import com.wallet.biochain.entities.Transaction;
import com.wallet.biochain.services.P2PNetworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/p2p")
@RequiredArgsConstructor
@Tag(name = "P2P Network", description = "Peer-to-peer network communication endpoints")
public class P2PNetworkController {

    private final P2PNetworkService p2pNetworkService;

    @GetMapping("/status")
    @Operation(summary = "Get network status", description = "Gets current P2P network status and statistics")
    public ResponseEntity<NetworkStatusDTO> getNetworkStatus() {
        log.info("REST request to get P2P network status");

        NetworkStatusDTO status = p2pNetworkService.getNetworkStatus();
        return ResponseEntity.ok(status);
    }

    @PostMapping("/connect")
    @Operation(summary = "Connect to network", description = "Connects to the P2P blockchain network")
    public ResponseEntity<Void> connectToNetwork() {
        log.info("REST request to connect to P2P network");

        try {
            p2pNetworkService.connectToNetwork();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to connect to network", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/disconnect")
    @Operation(summary = "Disconnect from network", description = "Disconnects from the P2P network")
    public ResponseEntity<Void> disconnectFromNetwork() {
        log.info("REST request to disconnect from P2P network");

        try {
            p2pNetworkService.disconnectFromNetwork();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to disconnect from network", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/peers/discover")
    @Operation(summary = "Discover peers", description = "Discovers available peers in the network")
    public ResponseEntity<List<String>> discoverPeers() {
        log.info("REST request to discover peers");

        try {
            List<String> peers = p2pNetworkService.discoverPeers();
            return ResponseEntity.ok(peers);
        } catch (Exception e) {
            log.error("Failed to discover peers", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/broadcast/block")
    @Operation(summary = "Broadcast block", description = "Broadcasts a block to all connected peers")
    public ResponseEntity<Void> broadcastBlock(@RequestBody Block block) {
        log.info("REST request to broadcast block: {}", block.getBlockIndex());

        try {
            p2pNetworkService.broadcastBlock(block);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to broadcast block", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/broadcast/transaction")
    @Operation(summary = "Broadcast transaction", description = "Broadcasts a transaction to all peers")
    public ResponseEntity<Void> broadcastTransaction(@RequestBody Transaction transaction) {
        log.info("REST request to broadcast transaction: {}", transaction.getTransactionHash());

        try {
            p2pNetworkService.broadcastTransaction(transaction);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to broadcast transaction", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/message/send")
    @Operation(summary = "Send message to peer", description = "Sends a message to a specific peer")
    public ResponseEntity<Void> sendMessageToPeer(
            @RequestParam String nodeId,
            @RequestParam String message) {
        log.info("REST request to send message to peer: {}", nodeId);

        try {
            p2pNetworkService.sendMessageToPeer(nodeId, message);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Peer not found: {}", nodeId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to send message", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/message/broadcast")
    @Operation(summary = "Broadcast message", description = "Broadcasts a message to all peers")
    public ResponseEntity<Void> broadcastMessage(@RequestParam String message) {
        log.info("REST request to broadcast message to all peers");

        try {
            p2pNetworkService.broadcastMessage(message);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to broadcast message", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/blockchain/request")
    @Operation(summary = "Request blockchain from peer", description = "Requests blockchain data from a peer")
    public ResponseEntity<Void> requestBlockchainFromPeer(@RequestParam String nodeId) {
        log.info("REST request to request blockchain from peer: {}", nodeId);

        try {
            p2pNetworkService.requestBlockchainFromPeer(nodeId);
            return ResponseEntity.accepted().build();
        } catch (IllegalArgumentException e) {
            log.error("Peer not found: {}", nodeId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to request blockchain", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/synchronize")
    @Operation(summary = "Synchronize with network", description = "Synchronizes blockchain with the network")
    public ResponseEntity<Void> synchronizeWithNetwork() {
        log.info("REST request to synchronize with network");

        try {
            p2pNetworkService.synchronizeWithNetwork();
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Failed to synchronize with network", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Check network health", description = "Checks if the P2P network is healthy")
    public ResponseEntity<Boolean> isNetworkHealthy() {
        log.debug("REST request to check network health");

        boolean healthy = p2pNetworkService.isNetworkHealthy();
        return ResponseEntity.ok(healthy);
    }

    @GetMapping("/peers/count")
    @Operation(summary = "Get connected peers count", description = "Gets the number of connected peers")
    public ResponseEntity<Integer> getConnectedPeersCount() {
        log.debug("REST request to get connected peers count");

        Integer count = p2pNetworkService.getConnectedPeersCount();
        return ResponseEntity.ok(count);
    }
}