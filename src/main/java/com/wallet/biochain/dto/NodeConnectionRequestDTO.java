package com.wallet.biochain.dto;

import com.wallet.biochain.enums.NodeType;

public record NodeConnectionRequestDTO(
        String nodeId,
        String ipAddress,
        Integer port,
        NodeType nodeType,
        String version
) {}