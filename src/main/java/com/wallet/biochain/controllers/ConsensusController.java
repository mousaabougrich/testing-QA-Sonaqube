package com.wallet.biochain.controllers;

import com.wallet.biochain.entities.Block;
import com.wallet.biochain.enums.ConsensusType;
import com.wallet.biochain.services.ConsensusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/consensus")
@RequiredArgsConstructor
@Tag(name = "Consensus", description = "Consensus mechanism management endpoints")
public class ConsensusController {

    private final ConsensusService consensusService;

    @GetMapping("/current")
    @Operation(summary = "Get current consensus", description = "Gets the current consensus mechanism")
    public ResponseEntity<ConsensusType> getCurrentConsensusType() {
        log.info("REST request to get current consensus type");

        ConsensusType consensusType = consensusService.getCurrentConsensusType();
        return ResponseEntity.ok(consensusType);
    }

    @PutMapping("/switch")
    @Operation(summary = "Switch consensus", description = "Switches to a different consensus mechanism")
    public ResponseEntity<Void> switchConsensus(@RequestParam ConsensusType newConsensusType) {
        log.info("REST request to switch consensus to: {}", newConsensusType);

        try {
            consensusService.setConsensusType(newConsensusType);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to switch consensus", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/validate-block")
    @Operation(summary = "Validate block", description = "Validates a block using current consensus")
    public ResponseEntity<Boolean> validateBlock(@RequestBody Block block) {
        log.info("REST request to validate block: {}", block.getBlockIndex());

        boolean isValid = consensusService.validateBlock(block);
        return ResponseEntity.ok(isValid);
    }

    @GetMapping("/validator-eligible/{address}")
    @Operation(summary = "Check validator eligibility", description = "Checks if address is eligible as validator")
    public ResponseEntity<Boolean> isValidatorEligible(@PathVariable String address) {
        log.info("REST request to check validator eligibility: {}", address);

        boolean isEligible = consensusService.isValidatorEligible(address);
        return ResponseEntity.ok(isEligible);
    }

    @GetMapping("/select-validator")
    @Operation(summary = "Select next validator", description = "Selects next validator (PoS only)")
    public ResponseEntity<String> selectNextValidator() {
        log.info("REST request to select next validator");

        String validator = consensusService.selectNextValidator();
        if (validator == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(validator);
    }

    @GetMapping("/supported-types")
    @Operation(summary = "Get supported consensus types", description = "Lists all supported consensus mechanisms")
    public ResponseEntity<ConsensusType[]> getSupportedConsensusTypes() {
        log.debug("REST request to get supported consensus types");

        return ResponseEntity.ok(ConsensusType.values());
    }
}