package com.wallet.biochain.controllers;

import com.wallet.biochain.entities.Block;
import com.wallet.biochain.entities.Stake;
import com.wallet.biochain.services.PoSConsensusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/consensus/pos")
@RequiredArgsConstructor
@Tag(name = "PoS Consensus", description = "Proof of Stake consensus mechanism endpoints")
public class PoSConsensusController {

    private final PoSConsensusService posConsensusService;

    @PostMapping("/validate-stake")
    @Operation(summary = "Validate stake", description = "Validates stake for a validator")
    public ResponseEntity<Boolean> validateStake(
            @RequestBody Block block,
            @RequestParam String validatorAddress) {
        log.info("REST request to validate stake for validator: {}", validatorAddress);

        try {
            boolean isValid = posConsensusService.validateStake(block, validatorAddress);
            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            log.error("Failed to validate stake", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/select-validator")
    @Operation(summary = "Select validator", description = "Selects next validator based on stake")
    public ResponseEntity<String> selectValidator() {
        log.info("REST request to select validator");

        try {
            String validator = posConsensusService.selectValidator();
            return ResponseEntity.ok(validator);
        } catch (IllegalStateException e) {
            log.error("No eligible validators", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/validator-probability/{address}")
    @Operation(summary = "Calculate validator probability", description = "Calculates probability of being selected as validator")
    public ResponseEntity<Map<String, Object>> calculateValidatorProbability(@PathVariable String address) {
        log.info("REST request to calculate validator probability for: {}", address);

        try {
            Double probability = posConsensusService.calculateValidatorProbability(address);

            Map<String, Object> response = new HashMap<>();
            response.put("address", address);
            response.put("probability", probability);
            response.put("probabilityPercentage", String.format("%.2f%%", probability));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to calculate validator probability", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/eligible-validators")
    @Operation(summary = "Get eligible validators", description = "Gets list of all eligible validators")
    public ResponseEntity<List<String>> getEligibleValidators() {
        log.info("REST request to get eligible validators");

        try {
            List<String> validators = posConsensusService.getEligibleValidators();
            return ResponseEntity.ok(validators);
        } catch (Exception e) {
            log.error("Failed to get eligible validators", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/has-minimum-stake/{address}")
    @Operation(summary = "Check minimum stake", description = "Checks if address has minimum required stake")
    public ResponseEntity<Map<String, Object>> hasMinimumStake(@PathVariable String address) {
        log.debug("REST request to check minimum stake for: {}", address);

        try {
            boolean hasMinStake = posConsensusService.hasMinimumStake(address);

            Map<String, Object> response = new HashMap<>();
            response.put("address", address);
            response.put("hasMinimumStake", hasMinStake);
            response.put("isEligible", hasMinStake);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to check minimum stake", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/total-staked")
    @Operation(summary = "Get total staked amount", description = "Gets total amount staked in network")
    public ResponseEntity<Map<String, Object>> getTotalStakedAmount() {
        log.info("REST request to get total staked amount");

        try {
            BigDecimal totalStaked = posConsensusService.getTotalStakedAmount();

            Map<String, Object> response = new HashMap<>();
            response.put("totalStaked", totalStaked);
            response.put("currency", "BIO");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get total staked amount", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/calculate-reward")
    @Operation(summary = "Calculate staking reward", description = "Calculates staking reward for a stake")
    public ResponseEntity<Map<String, Object>> calculateStakingReward(@RequestBody Stake stake) {
        log.info("REST request to calculate staking reward");

        try {
            BigDecimal reward = posConsensusService.calculateStakingReward(stake);

            Map<String, Object> response = new HashMap<>();
            response.put("stakeId", stake.getId());
            response.put("stakedAmount", stake.getStakedAmount());
            response.put("reward", reward);
            response.put("currency", "BIO");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to calculate staking reward", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/validator-info/{address}")
    @Operation(summary = "Get validator info", description = "Gets comprehensive validator information")
    public ResponseEntity<Map<String, Object>> getValidatorInfo(@PathVariable String address) {
        log.info("REST request to get validator info for: {}", address);

        try {
            boolean hasMinStake = posConsensusService.hasMinimumStake(address);
            Double probability = posConsensusService.calculateValidatorProbability(address);
            List<String> allValidators = posConsensusService.getEligibleValidators();
            boolean isEligible = allValidators.contains(address);

            Map<String, Object> info = new HashMap<>();
            info.put("address", address);
            info.put("isEligible", isEligible);
            info.put("hasMinimumStake", hasMinStake);
            info.put("selectionProbability", probability);
            info.put("probabilityPercentage", String.format("%.2f%%", probability));
            info.put("rank", allValidators.indexOf(address) + 1);
            info.put("totalValidators", allValidators.size());

            return ResponseEntity.ok(info);
        } catch (Exception e) {
            log.error("Failed to get validator info", e);
            return ResponseEntity.badRequest().build();
        }
    }
}