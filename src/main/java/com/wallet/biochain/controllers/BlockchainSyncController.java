package com.wallet.biochain.controllers;

import com.wallet.biochain.entities.Block;
import com.wallet.biochain.services.BlockchainSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
@Tag(name = "Blockchain Sync", description = "Blockchain synchronization endpoints")
public class BlockchainSyncController {

    private final BlockchainSyncService syncService;

    @PostMapping("/synchronize")
    @Operation(summary = "Synchronize blockchain", description = "Synchronizes blockchain with best peer")
    public ResponseEntity<Void> synchronize() {
        log.info("REST request to synchronize blockchain");

        syncService.synchronize();
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/synchronize-with-peer")
    @Operation(summary = "Synchronize with peer", description = "Synchronizes blockchain with specific peer")
    public ResponseEntity<Void> synchronizeWithPeer(@RequestParam String peerNodeId) {
        log.info("REST request to synchronize with peer: {}", peerNodeId);

        try {
            syncService.synchronizeWithPeer(peerNodeId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Failed to synchronize with peer", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/missing-blocks")
    @Operation(summary = "Get missing blocks", description = "Identifies missing blocks in range")
    public ResponseEntity<List<Block>> getMissingBlocks(
            @RequestParam Integer fromIndex,
            @RequestParam Integer toIndex) {
        log.info("REST request to get missing blocks from {} to {}", fromIndex, toIndex);

        List<Block> missingBlocks = syncService.getMissingBlocks(fromIndex, toIndex);
        return ResponseEntity.ok(missingBlocks);
    }

    @PostMapping("/request-blocks")
    @Operation(summary = "Request blocks from peer", description = "Requests blocks from a peer node")
    public ResponseEntity<List<Block>> requestBlocksFromPeer(
            @RequestParam String peerNodeId,
            @RequestParam Integer fromIndex,
            @RequestParam Integer toIndex) {
        log.info("REST request to request blocks {}-{} from peer: {}", fromIndex, toIndex, peerNodeId);

        try {
            List<Block> blocks = syncService.requestBlocksFromPeer(peerNodeId, fromIndex, toIndex);
            return ResponseEntity.ok(blocks);
        } catch (IllegalArgumentException e) {
            log.error("Failed to request blocks", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/validate-blocks")
    @Operation(summary = "Validate received blocks", description = "Validates a list of blocks")
    public ResponseEntity<Boolean> validateReceivedBlocks(@RequestBody List<Block> blocks) {
        log.info("REST request to validate {} blocks", blocks.size());

        boolean isValid = syncService.validateReceivedBlocks(blocks);
        return ResponseEntity.ok(isValid);
    }

    @PostMapping("/add-blocks")
    @Operation(summary = "Add synchronized blocks", description = "Adds validated blocks to chain")
    public ResponseEntity<Void> addSynchronizedBlocks(@RequestBody List<Block> blocks) {
        log.info("REST request to add {} synchronized blocks", blocks.size());

        try {
            syncService.addSynchronizedBlocks(blocks);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to add synchronized blocks", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/needs-synchronization")
    @Operation(summary = "Check if sync needed", description = "Checks if synchronization is needed")
    public ResponseEntity<Boolean> needsSynchronization() {
        log.debug("REST request to check if synchronization is needed");

        boolean needsSync = syncService.needsSynchronization();
        return ResponseEntity.ok(needsSync);
    }

    @GetMapping("/sync-progress")
    @Operation(summary = "Get sync progress", description = "Gets synchronization progress percentage")
    public ResponseEntity<Integer> getSyncProgress() {
        log.debug("REST request to get sync progress");

        Integer progress = syncService.getSyncProgress();
        return ResponseEntity.ok(progress);
    }

    @PostMapping("/resolve-conflicts")
    @Operation(summary = "Resolve conflicts", description = "Resolves blockchain conflicts (longest chain)")
    public ResponseEntity<Void> resolveConflicts() {
        log.info("REST request to resolve blockchain conflicts");

        syncService.resolveConflicts();
        return ResponseEntity.ok().build();
    }
}