package com.wallet.biochain.dto;

import java.util.List;

public record PeerListDTO(
        List<NodeInfoDTO> peers,
        Integer totalPeers,
        Integer connectedPeers
) {}