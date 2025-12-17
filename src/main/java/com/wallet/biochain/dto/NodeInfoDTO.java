package com.wallet.biochain.dto;

import com.wallet.biochain.enums.NodeStatus;
import com.wallet.biochain.enums.NodeType;
import java.time.LocalDateTime;

public record NodeInfoDTO(
        Long id,
        String nodeId,
        String ipAddress,
        Integer port,
        NodeStatus status,
        NodeType nodeType,
        LocalDateTime lastSeen,
        Integer blockHeight,
        String version,
        Long latencyMs,
        Boolean isTrusted,
        Integer connectionCount,
        Integer peerCount
) {}