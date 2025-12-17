package com.wallet.biochain.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Blockchain configuration properties
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "blockchain")
public class BlockchainConfig {

    /**
     * Chain ID and name
     */
    private String chainId = "biochain-main-001";
    private String chainName = "BioChain";

    /**
     * Genesis block configuration
     */
    private Genesis genesis = new Genesis();

    /**
     * Mining configuration
     */
    private Mining mining = new Mining();

    /**
     * Consensus configuration
     */
    private Consensus consensus = new Consensus();

    @Getter
    @Setter
    public static class Genesis {
        private String previousHash = "0000000000000000000000000000000000000000000000000000000000000000";
        private Long timestamp = 1609459200000L; // Jan 1, 2021
        private String minerAddress = "0x0000000000000000000000000000000000000000";
        private String message = "BioChain Genesis Block - Morocco Blockchain Wallet";
    }

    @Getter
    @Setter
    public static class Mining {
        private Integer initialDifficulty = 4;
        private Integer minDifficulty = 1;
        private Integer maxDifficulty = 10;
        private BigDecimal initialReward = new BigDecimal("50");
        private Integer halvingInterval = 210000; // Blocks until reward halves
        private Long targetBlockTime = 600000L; // 10 minutes in milliseconds
    }

    @Getter
    @Setter
    public static class Consensus {
        private String type = "POW"; // POW, POS, or HYBRID
        private BigDecimal minStakeAmount = new BigDecimal("1000");
        private Integer minStakingPeriod = 86400; // 24 hours in seconds
        private BigDecimal stakingRewardRate = new BigDecimal("0.05"); // 5% annual
    }
}