package com.wallet.biochain.controllers;

import com.wallet.biochain.dto.BlockDTO;
import com.wallet.biochain.dto.BlockValidationDTO;
import com.wallet.biochain.entities.Block;
import com.wallet.biochain.services.BlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/blocks")
@RequiredArgsConstructor
@Tag(name = "Block", description = "Block management endpoints")
public class BlockController {

    private final BlockService blockService;

    @GetMapping("/{id}")
    @Operation(summary = "Get block by ID", description = "Retrieves block by ID")
    public ResponseEntity<BlockDTO> getBlockById(@PathVariable Long id) {
        log.info("REST request to get block by ID: {}", id);

        return blockService.getBlockById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/hash/{hash}")
    @Operation(summary = "Get block by hash", description = "Retrieves block by hash")
    public ResponseEntity<BlockDTO> getBlockByHash(@PathVariable String hash) {
        log.info("REST request to get block by hash: {}", hash);

        return blockService.getBlockByHash(hash)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/index/{index}")
    @Operation(summary = "Get block by index", description = "Retrieves block by index")
    public ResponseEntity<BlockDTO> getBlockByIndex(@PathVariable Integer index) {
        log.info("REST request to get block by index: {}", index);

        return blockService.getBlockByIndex(index)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/latest")
    @Operation(summary = "Get latest block", description = "Retrieves the latest block")
    public ResponseEntity<BlockDTO> getLatestBlock() {
        log.info("REST request to get latest block");

        return blockService.getLatestBlock()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all blocks", description = "Retrieves all blocks")
    public ResponseEntity<List<BlockDTO>> getAllBlocks() {
        log.info("REST request to get all blocks");

        List<BlockDTO> blocks = blockService.getAllBlocks();
        return ResponseEntity.ok(blocks);
    }

    @GetMapping("/range")
    @Operation(summary = "Get blocks in range", description = "Retrieves blocks in index range")
    public ResponseEntity<List<BlockDTO>> getBlocksInRange(
            @RequestParam Integer startIndex,
            @RequestParam Integer endIndex) {
        log.info("REST request to get blocks in range: {} to {}", startIndex, endIndex);

        List<BlockDTO> blocks = blockService.getBlocksInRange(startIndex, endIndex);
        return ResponseEntity.ok(blocks);
    }

    @GetMapping("/miner/{address}")
    @Operation(summary = "Get blocks by miner", description = "Retrieves blocks mined by address")
    public ResponseEntity<List<BlockDTO>> getBlocksByMiner(@PathVariable String address) {
        log.info("REST request to get blocks by miner: {}", address);

        List<BlockDTO> blocks = blockService.getBlocksByMiner(address);
        return ResponseEntity.ok(blocks);
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate block", description = "Validates a block")
    public ResponseEntity<BlockValidationDTO> validateBlock(@RequestBody Block block) {
        log.info("REST request to validate block: {}", block.getBlockIndex());

        BlockValidationDTO validation = blockService.validateBlock(block);
        return ResponseEntity.ok(validation);
    }

    @GetMapping("/count")
    @Operation(summary = "Get block count", description = "Gets total block count")
    public ResponseEntity<Long> getBlockCount() {
        log.info("REST request to get block count");

        Long count = blockService.getBlockCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/miner/{address}/count")
    @Operation(summary = "Get blocks mined count", description = "Gets count of blocks mined by address")
    public ResponseEntity<Long> getBlocksMinedCount(@PathVariable String address) {
        log.info("REST request to get blocks mined count for: {}", address);

        Long count = blockService.getBlocksMinedCount(address);
        return ResponseEntity.ok(count);
    }
}