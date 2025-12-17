package com.wallet.biochain.controllers;

import com.wallet.biochain.dto.BlockchainStatusDTO;
import com.wallet.biochain.dto.BlockDTO;
import com.wallet.biochain.entities.Blockchain;
import com.wallet.biochain.enums.ConsensusType;
import com.wallet.biochain.services.BlockService;
import com.wallet.biochain.services.BlockchainService;
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
@RequestMapping("/api/blockchain")
@RequiredArgsConstructor
@Tag(name = "Blockchain", description = "Blockchain management endpoints")
public class BlockchainController {

    private final BlockchainService blockchainService;
    private final BlockService blockService;

    @PostMapping("/initialize")
    @Operation(summary = "Initialize blockchain", description = "Initializes a new blockchain with genesis block")
    public ResponseEntity<Blockchain> initializeBlockchain(
            @RequestParam String name,
            @RequestParam ConsensusType consensusType) {
        log.info("REST request to initialize blockchain: {} with consensus: {}", name, consensusType);

        try {
            Blockchain blockchain = blockchainService.initializeBlockchain(name, consensusType);
            return ResponseEntity.status(HttpStatus.CREATED).body(blockchain);
        } catch (Exception e) {
            log.error("Failed to initialize blockchain", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{chainId}/status")
    @Operation(summary = "Get blockchain status", description = "Gets current blockchain status and statistics")
    public ResponseEntity<BlockchainStatusDTO> getBlockchainStatus(@PathVariable String chainId) {
        log.info("REST request to get blockchain status: {}", chainId);

        try {
            BlockchainStatusDTO status = blockchainService.getBlockchainStatus(chainId);
            return ResponseEntity.ok(status);
        } catch (IllegalArgumentException e) {
            log.error("Blockchain not found: {}", chainId);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @Operation(summary = "Get all blockchains", description = "Retrieves all blockchains")
    public ResponseEntity<List<Blockchain>> getAllBlockchains() {
        log.info("REST request to get all blockchains");

        List<Blockchain> blockchains = blockchainService.getAllBlockchains();
        return ResponseEntity.ok(blockchains);
    }

    @GetMapping("/{chainId}/height")
    @Operation(summary = "Get blockchain height", description = "Gets current blockchain height")
    public ResponseEntity<Integer> getBlockchainHeight(@PathVariable String chainId) {
        log.info("REST request to get blockchain height: {}", chainId);

        try {
            Integer height = blockchainService.getBlockchainHeight(chainId);
            return ResponseEntity.ok(height);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{chainId}/difficulty")
    @Operation(summary = "Update difficulty", description = "Updates mining difficulty (admin only)")
    public ResponseEntity<Void> updateDifficulty(
            @PathVariable String chainId,
            @RequestParam Integer difficulty) {
        log.info("REST request to update difficulty for blockchain: {} to {}", chainId, difficulty);

        try {
            blockchainService.updateDifficulty(chainId, difficulty);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to update difficulty", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{chainId}/validate")
    @Operation(summary = "Validate blockchain", description = "Validates entire blockchain integrity")
    public ResponseEntity<Boolean> validateBlockchain(@PathVariable String chainId) {
        log.info("REST request to validate blockchain: {}", chainId);

        try {
            boolean isValid = blockchainService.validateBlockchain(chainId);
            return ResponseEntity.ok(isValid);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{chainId}/genesis")
    @Operation(summary = "Get genesis block", description = "Retrieves the genesis block")
    public ResponseEntity<BlockDTO> getGenesisBlock(@PathVariable String chainId) {
        log.info("REST request to get genesis block for: {}", chainId);

        try {
            var genesisBlock = blockchainService.getGenesisBlock(chainId);
            var blockDTO = blockService.getBlockByHash(genesisBlock.getHash());
            return blockDTO.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{chainId}/total-transactions")
    @Operation(summary = "Get total transactions", description = "Gets total transaction count in blockchain")
    public ResponseEntity<Integer> getTotalTransactions(@PathVariable String chainId) {
        log.info("REST request to get total transactions for: {}", chainId);

        try {
            Integer total = blockchainService.getTotalTransactions(chainId);
            return ResponseEntity.ok(total);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{chainId}/sync")
    @Operation(summary = "Synchronize blockchain", description = "Triggers blockchain synchronization")
    public ResponseEntity<Void> synchronizeBlockchain(@PathVariable String chainId) {
        log.info("REST request to synchronize blockchain: {}", chainId);

        try {
            blockchainService.synchronizeBlockchain(chainId);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Failed to synchronize blockchain", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{chainId}/integrity")
    @Operation(summary = "Check integrity", description = "Checks blockchain integrity")
    public ResponseEntity<Boolean> checkIntegrity(@PathVariable String chainId) {
        log.info("REST request to check integrity for: {}", chainId);

        try {
            boolean hasIntegrity = blockchainService.checkIntegrity(chainId);
            return ResponseEntity.ok(hasIntegrity);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}