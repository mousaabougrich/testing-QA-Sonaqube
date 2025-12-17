package com.wallet.biochain.mappers;

import com.wallet.biochain.dto.NetworkStatusDTO;
import com.wallet.biochain.entities.Node;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class NetworkMapper {

    public NetworkStatusDTO toStatusDTO(List<Node> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return new NetworkStatusDTO(
                    0, 0, 0, 0, 0, 0L, 0, false, LocalDateTime.now()
            );
        }

        int totalNodes = nodes.size();
        long activeCount = nodes.stream()
                .filter(n -> "ACTIVE".equals(n.getStatus().toString()))
                .count();
        long inactiveCount = nodes.stream()
                .filter(n -> "INACTIVE".equals(n.getStatus().toString()) ||
                        "DISCONNECTED".equals(n.getStatus().toString()))
                .count();
        long syncingCount = nodes.stream()
                .filter(n -> "SYNCING".equals(n.getStatus().toString()))
                .count();

        double avgBlockHeight = nodes.stream()
                .filter(n -> n.getBlockHeight() != null)
                .mapToInt(Node::getBlockHeight)
                .average()
                .orElse(0.0);

        double avgLatency = nodes.stream()
                .filter(n -> n.getLatencyMs() != null)
                .mapToLong(Node::getLatencyMs)
                .average()
                .orElse(0.0);

        int totalConnections = nodes.stream()
                .filter(n -> n.getConnectionCount() != null)
                .mapToInt(Node::getConnectionCount)
                .sum();

        boolean isHealthy = activeCount >= totalNodes * 0.5; // At least 50% active

        return new NetworkStatusDTO(
                totalNodes,
                (int) activeCount,
                (int) inactiveCount,
                (int) syncingCount,
                (int) Math.round(avgBlockHeight),
                Math.round(avgLatency),
                totalConnections,
                isHealthy,
                LocalDateTime.now()
        );
    }
}