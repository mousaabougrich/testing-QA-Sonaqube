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
            @RequestParam(required = false) Integer difficulty,
            @RequestBody(required = false) Integer difficultyBody) {
        Integer diffValue = difficulty != null ? difficulty : difficultyBody;
        log.info("REST request to update difficulty for blockchain: {} to {}", chainId, diffValue);

        try {
            if (diffValue == null) {
                log.error("Difficulty value is required");
                return ResponseEntity.badRequest().build();
            }
            blockchainService.updateDifficulty(chainId, diffValue);
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

    // ===================== DEFAULT ENDPOINTS (No chainId required) =====================

    @GetMapping("/status")
    @Operation(summary = "Get default blockchain status", description = "Gets status of the default/first blockchain")
    public ResponseEntity<BlockchainStatusDTO> getDefaultBlockchainStatus() {
        log.info("REST request to get default blockchain status");

        try {
            List<Blockchain> blockchains = blockchainService.getAllBlockchains();
            if (blockchains.isEmpty()) {
                log.warn("No blockchain found, initializing default blockchain");
                blockchainService.initializeBlockchain("BioChain", ConsensusType.PROOF_OF_WORK);
                blockchains = blockchainService.getAllBlockchains();
            }

            Blockchain blockchain = blockchains.get(0);
            BlockchainStatusDTO status = blockchainService.getBlockchainStatus(blockchain.getChainId());
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Failed to get default blockchain status", e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/total-transactions")
    @Operation(summary = "Get total transactions", description = "Gets total transaction count from default blockchain")
    public ResponseEntity<Integer> getDefaultTotalTransactions() {
        log.info("REST request to get total transactions from default blockchain");

        try {
            List<Blockchain> blockchains = blockchainService.getAllBlockchains();
            if (blockchains.isEmpty()) {
                return ResponseEntity.ok(0);
            }

            Blockchain blockchain = blockchains.get(0);
            Integer total = blockchainService.getTotalTransactions(blockchain.getChainId());
            return ResponseEntity.ok(total);
        } catch (Exception e) {
            log.error("Failed to get total transactions", e);
            return ResponseEntity.ok(0);
        }
    }

    @GetMapping("/default")
    @Operation(summary = "Get default blockchain", description = "Gets the default/first blockchain")
    public ResponseEntity<Blockchain> getDefaultBlockchain() {
        log.info("REST request to get default blockchain");

        List<Blockchain> blockchains = blockchainService.getAllBlockchains();
        if (blockchains.isEmpty()) {
            log.info("No blockchain found, initializing default blockchain");
            Blockchain blockchain = blockchainService.initializeBlockchain("BioChain", ConsensusType.PROOF_OF_WORK);
            return ResponseEntity.ok(blockchain);
        }

        return ResponseEntity.ok(blockchains.get(0));
    }

    @PutMapping("/difficulty")
    @Operation(summary = "Update default blockchain difficulty", description = "Updates mining difficulty for default blockchain")
    public ResponseEntity<Void> updateDefaultDifficulty(
            @RequestParam(required = false) Integer difficulty,
            @RequestBody(required = false) Integer difficultyBody) {
        Integer diffValue = difficulty != null ? difficulty : difficultyBody;
        log.info("REST request to update difficulty for default blockchain to {}", diffValue);

        try {
            if (diffValue == null) {
                log.error("Difficulty value is required");
                return ResponseEntity.badRequest().build();
            }

            List<Blockchain> blockchains = blockchainService.getAllBlockchains();
            if (blockchains.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Blockchain blockchain = blockchains.get(0);
            blockchainService.updateDifficulty(blockchain.getChainId(), diffValue);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to update difficulty", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/height")
    @Operation(summary = "Get default blockchain height", description = "Gets current height of default blockchain")
    public ResponseEntity<Integer> getDefaultBlockchainHeight() {
        log.info("REST request to get default blockchain height");

        try {
            List<Blockchain> blockchains = blockchainService.getAllBlockchains();
            if (blockchains.isEmpty()) {
                return ResponseEntity.ok(0);
            }

            Blockchain blockchain = blockchains.get(0);
            Integer height = blockchainService.getBlockchainHeight(blockchain.getChainId());
            return ResponseEntity.ok(height);
        } catch (Exception e) {
            log.error("Failed to get blockchain height", e);
            return ResponseEntity.ok(0);
        }
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate default blockchain", description = "Validates entire default blockchain integrity")
    public ResponseEntity<Boolean> validateDefaultBlockchain() {
        log.info("REST request to validate default blockchain");

        try {
            List<Blockchain> blockchains = blockchainService.getAllBlockchains();
            if (blockchains.isEmpty()) {
                return ResponseEntity.ok(true);
            }

            Blockchain blockchain = blockchains.get(0);
            boolean isValid = blockchainService.validateBlockchain(blockchain.getChainId());
            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            log.error("Failed to validate blockchain", e);
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/integrity")
    @Operation(summary = "Check default blockchain integrity", description = "Checks default blockchain integrity")
    public ResponseEntity<Boolean> checkDefaultIntegrity() {
        log.info("REST request to check default blockchain integrity");

        try {
            List<Blockchain> blockchains = blockchainService.getAllBlockchains();
            if (blockchains.isEmpty()) {
                return ResponseEntity.ok(true);
            }

            Blockchain blockchain = blockchains.get(0);
            boolean hasIntegrity = blockchainService.checkIntegrity(blockchain.getChainId());
            return ResponseEntity.ok(hasIntegrity);
        } catch (Exception e) {
            log.error("Failed to check integrity", e);
            return ResponseEntity.ok(false);
        }
    }

    @PostMapping("/sync")
    @Operation(summary = "Synchronize default blockchain", description = "Triggers default blockchain synchronization")
    public ResponseEntity<Void> synchronizeDefaultBlockchain() {
        log.info("REST request to synchronize default blockchain");

        try {
            List<Blockchain> blockchains = blockchainService.getAllBlockchains();
            if (blockchains.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Blockchain blockchain = blockchains.get(0);
            blockchainService.synchronizeBlockchain(blockchain.getChainId());
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Failed to synchronize blockchain", e);
            return ResponseEntity.badRequest().build();
        }
    }
}