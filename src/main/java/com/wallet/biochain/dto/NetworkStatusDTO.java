package com.wallet.biochain.dto;

import java.time.LocalDateTime;

public record NetworkStatusDTO(
        Integer totalNodes,
        Integer activeNodes,
        Integer inactiveNodes,
        Integer syncingNodes,
        Integer averageBlockHeight,
        Long averageLatency,
        Integer connectedPeers,
        Boolean isNetworkHealthy,
        LocalDateTime lastUpdate
) {}