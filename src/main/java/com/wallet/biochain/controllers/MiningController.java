package com.wallet.biochain.controllers;

import com.wallet.biochain.dto.MiningResultDTO;
import com.wallet.biochain.entities.Transaction;
import com.wallet.biochain.services.MiningService;
import com.wallet.biochain.services.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/mining")
@RequiredArgsConstructor
@Tag(name = "Mining", description = "Mining operations endpoints")
public class MiningController {

    private final MiningService miningService;
    private final TransactionService transactionService;

    @PostMapping("/mine")
    @Operation(summary = "Mine block", description = "Mines a new block with pending transactions")
    public ResponseEntity<MiningResultDTO> mineBlock(@RequestParam String minerAddress) {
        log.info("REST request to mine block for: {}", minerAddress);

        try {
            // Get pending transactions
            List<Transaction> pendingTxs = transactionService.getPendingTransactions()
                    .stream()
                    .map(dto -> {
                        Transaction tx = new Transaction();
                        tx.setTransactionHash(dto.transactionHash());
                        tx.setSenderAddress(dto.senderAddress());
                        tx.setRecipientAddress(dto.recipientAddress());
                        tx.setAmount(dto.amount());
                        tx.setFee(dto.fee());
                        tx.setTimestamp(dto.timestamp());
                        return tx;
                    })
                    .limit(10) // Limit to 10 transactions per block
                    .toList();

            MiningResultDTO result = miningService.mineBlock(pendingTxs, minerAddress);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Mining failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/mine-with-difficulty")
    @Operation(summary = "Mine with difficulty", description = "Mines a block with specific difficulty")
    public ResponseEntity<MiningResultDTO> mineBlockWithDifficulty(
            @RequestParam String minerAddress,
            @RequestParam Integer difficulty) {
        log.info("REST request to mine block with difficulty {} for: {}", difficulty, minerAddress);

        try {
            List<Transaction> pendingTxs = transactionService.getPendingTransactions()
                    .stream()
                    .map(dto -> {
                        Transaction tx = new Transaction();
                        tx.setTransactionHash(dto.transactionHash());
                        tx.setSenderAddress(dto.senderAddress());
                        tx.setRecipientAddress(dto.recipientAddress());
                        tx.setAmount(dto.amount());
                        tx.setFee(dto.fee());
                        tx.setTimestamp(dto.timestamp());
                        return tx;
                    })
                    .limit(10)
                    .toList();

            MiningResultDTO result = miningService.mineBlockWithDifficulty(pendingTxs, minerAddress, difficulty);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Mining with difficulty failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/difficulty")
    @Operation(summary = "Get current difficulty", description = "Gets current mining difficulty")
    public ResponseEntity<Integer> getCurrentDifficulty() {
        log.info("REST request to get current difficulty");

        Integer difficulty = miningService.getCurrentDifficulty();
        return ResponseEntity.ok(difficulty);
    }

    @GetMapping("/reward/{blockIndex}")
    @Operation(summary = "Calculate mining reward", description = "Calculates mining reward for block index")
    public ResponseEntity<BigDecimal> calculateMiningReward(@PathVariable Integer blockIndex) {
        log.info("REST request to calculate mining reward for block: {}", blockIndex);

        BigDecimal reward = miningService.calculateMiningReward(blockIndex);
        return ResponseEntity.ok(reward);
    }

    @GetMapping("/estimated-time/{difficulty}")
    @Operation(summary = "Get estimated mining time", description = "Gets estimated mining time for difficulty")
    public ResponseEntity<Long> getEstimatedMiningTime(@PathVariable Integer difficulty) {
        log.debug("REST request to get estimated mining time for difficulty: {}", difficulty);

        Long estimatedTime = miningService.getEstimatedMiningTime(difficulty);
        return ResponseEntity.ok(estimatedTime);
    }
}