package com.wallet.biochain.mappers;

import com.wallet.biochain.dto.NodeInfoDTO;
import com.wallet.biochain.dto.PeerListDTO;
import com.wallet.biochain.entities.Node;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NodeMapper {

    public NodeInfoDTO toDTO(Node node) {
        if (node == null) {
            return null;
        }

        return new NodeInfoDTO(
                node.getId(),
                node.getNodeId(),
                node.getIpAddress(),
                node.getPort(),
                node.getStatus(),
                node.getNodeType(),
                node.getLastSeen(),
                node.getBlockHeight(),
                node.getVersion(),
                node.getLatencyMs(),
                node.getIsTrusted(),
                node.getConnectionCount(),
                node.getPeers() != null ? node.getPeers().size() : 0
        );
    }

    public List<NodeInfoDTO> toDTOList(List<Node> nodes) {
        if (nodes == null) {
            return null;
        }

        return nodes.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PeerListDTO toPeerListDTO(List<Node> peers) {
        if (peers == null) {
            return null;
        }

        List<NodeInfoDTO> peerDTOs = toDTOList(peers);
        int connectedPeers = (int) peers.stream()
                .filter(node -> node.getStatus() != null &&
                        node.getStatus().toString().equals("ACTIVE"))
                .count();

        return new PeerListDTO(
                peerDTOs,
                peerDTOs.size(),
                connectedPeers
        );
    }
}