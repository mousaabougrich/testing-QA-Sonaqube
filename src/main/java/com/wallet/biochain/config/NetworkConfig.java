package com.wallet.biochain.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * P2P Network configuration properties
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "network")
public class NetworkConfig {

    /**
     * Node configuration
     */
    private Node node = new Node();

    /**
     * P2P network settings
     */
    private P2P p2p = new P2P();

    /**
     * Sync configuration
     */
    private Sync sync = new Sync();

    @Getter
    @Setter
    public static class Node {
        private String nodeId = generateNodeId();
        private String host = "localhost";
        private Integer port = 8545;
        private String type = "FULL_NODE"; // FULL_NODE, LIGHT_NODE, MINING_NODE
        private Boolean enabled = true;

        private static String generateNodeId() {
            return "node-" + System.currentTimeMillis();
        }
    }

    @Getter
    @Setter
    public static class P2P {
        private Integer maxPeers = 50;
        private Integer minPeers = 3;
        private Long connectionTimeout = 30000L; // 30 seconds
        private Long pingInterval = 60000L; // 1 minute
        private Long syncInterval = 300000L; // 5 minutes
        private List<String> seedNodes = new ArrayList<>(List.of(
                "localhost:8546",
                "localhost:8547"
        ));
        private Boolean discoveryEnabled = true;
    }

    @Getter
    @Setter
    public static class Sync {
        private Integer batchSize = 100; // Blocks to sync at once
        private Long timeout = 60000L; // 1 minute
        private Boolean autoSync = true;
        private Integer maxRetries = 3;
    }
}