package com.wallet.biochain.controllers;

import com.wallet.biochain.entities.Block;
import com.wallet.biochain.services.PoWConsensusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/consensus/pow")
@RequiredArgsConstructor
@Tag(name = "PoW Consensus", description = "Proof of Work consensus mechanism endpoints")
public class PoWConsensusController {

    private final PoWConsensusService powConsensusService;

    @PostMapping("/mine")
    @Operation(summary = "Mine block", description = "Mines a block using Proof of Work")
    public ResponseEntity<Block> mineBlock(
            @RequestBody Block block,
            @RequestParam Integer difficulty) {
        log.info("REST request to mine block with PoW, difficulty: {}", difficulty);

        try {
            Block minedBlock = powConsensusService.mineBlock(block, difficulty);
            return ResponseEntity.ok(minedBlock);
        } catch (Exception e) {
            log.error("Failed to mine block", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate proof of work", description = "Validates a block's proof of work")
    public ResponseEntity<Map<String, Object>> validateProofOfWork(
            @RequestBody Block block,
            @RequestParam Integer difficulty) {
        log.info("REST request to validate PoW for block: {}", block.getBlockIndex());

        try {
            boolean isValid = powConsensusService.validateProofOfWork(block, difficulty);

            Map<String, Object> response = new HashMap<>();
            response.put("isValid", isValid);
            response.put("blockIndex", block.getBlockIndex());
            response.put("blockHash", block.getHash());
            response.put("nonce", block.getNonce());
            response.put("difficulty", difficulty);
            response.put("message", isValid ? "Proof of work is valid" : "Invalid proof of work");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to validate proof of work", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/calculate-hash")
    @Operation(summary = "Calculate block hash", description = "Calculates hash for a block")
    public ResponseEntity<Map<String, String>> calculateHash(@RequestBody Block block) {
        log.debug("REST request to calculate hash for block: {}", block.getBlockIndex());

        try {
            String hash = powConsensusService.calculateHash(block);

            Map<String, String> response = new HashMap<>();
            response.put("blockIndex", String.valueOf(block.getBlockIndex()));
            response.put("hash", hash);
            response.put("previousHash", block.getPreviousHash());
            response.put("nonce", String.valueOf(block.getNonce()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to calculate hash", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/meets-target")
    @Operation(summary = "Check if hash meets target", description = "Checks if a hash meets difficulty target")
    public ResponseEntity<Map<String, Object>> meetsTarget(
            @RequestParam String hash,
            @RequestParam Integer difficulty) {
        log.debug("REST request to check if hash meets target");

        try {
            boolean meetsTarget = powConsensusService.meetsTarget(hash, difficulty);
            String target = powConsensusService.getDifficultyTarget(difficulty);

            Map<String, Object> response = new HashMap<>();
            response.put("hash", hash);
            response.put("difficulty", difficulty);
            response.put("target", target);
            response.put("meetsTarget", meetsTarget);
            response.put("hashPrefix", hash.substring(0, Math.min(difficulty, hash.length())));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to check target", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/difficulty-target/{difficulty}")
    @Operation(summary = "Get difficulty target", description = "Gets the target string for a difficulty level")
    public ResponseEntity<Map<String, Object>> getDifficultyTarget(@PathVariable Integer difficulty) {
        log.debug("REST request to get difficulty target for: {}", difficulty);

        try {
            String target = powConsensusService.getDifficultyTarget(difficulty);

            Map<String, Object> response = new HashMap<>();
            response.put("difficulty", difficulty);
            response.put("target", target);
            response.put("leadingZeros", difficulty);
            response.put("description", "Hash must start with " + difficulty + " zeros");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get difficulty target", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/mining-info/{difficulty}")
    @Operation(summary = "Get mining information", description = "Gets information about mining at difficulty level")
    public ResponseEntity<Map<String, Object>> getMiningInfo(@PathVariable Integer difficulty) {
        log.info("REST request to get mining info for difficulty: {}", difficulty);

        try {
            String target = powConsensusService.getDifficultyTarget(difficulty);
            long estimatedAttempts = (long) Math.pow(16, difficulty);

            Map<String, Object> info = new HashMap<>();
            info.put("difficulty", difficulty);
            info.put("target", target);
            info.put("leadingZeros", difficulty);
            info.put("estimatedAttempts", estimatedAttempts);
            info.put("algorithm", "SHA-256");
            info.put("description", "Find nonce where hash starts with " + difficulty + " zeros");

            return ResponseEntity.ok(info);
        } catch (Exception e) {
            log.error("Failed to get mining info", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/test-mine")
    @Operation(summary = "Test mine (limited attempts)", description = "Tests mining with limited attempts for demo")
    public ResponseEntity<Map<String, Object>> testMine(
            @RequestBody Block block,
            @RequestParam Integer difficulty,
            @RequestParam(defaultValue = "10000") Integer maxAttempts) {
        log.info("REST request to test mine with max {} attempts", maxAttempts);

        try {
            long startTime = System.currentTimeMillis();
            String target = powConsensusService.getDifficultyTarget(difficulty);

            int nonce = 0;
            String hash = "";
            boolean found = false;

            while (nonce < maxAttempts) {
                nonce++;
                block.setNonce(nonce);
                hash = powConsensusService.calculateHash(block);

                if (powConsensusService.meetsTarget(hash, difficulty)) {
                    found = true;
                    break;
                }
            }

            long duration = System.currentTimeMillis() - startTime;

            Map<String, Object> response = new HashMap<>();
            response.put("success", found);
            response.put("attempts", nonce);
            response.put("maxAttempts", maxAttempts);
            response.put("difficulty", difficulty);
            response.put("target", target);
            response.put("hash", hash);
            response.put("nonce", found ? block.getNonce() : null);
            response.put("durationMs", duration);
            response.put("message", found ? "Valid nonce found!" : "No valid nonce found in limit");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to test mine", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}