package com.wallet.biochain.entities;
import com.wallet.biochain.enums.NodeStatus;
import com.wallet.biochain.enums.NodeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "nodes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Node {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "node_id", nullable = false, unique = true)
    private String nodeId;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private Integer port;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NodeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "node_type", nullable = false)
    private NodeType nodeType;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    @Column(name = "block_height")
    private Integer blockHeight;

    @Column(nullable = false)
    private String version;

    @Column(name = "latency_ms")
    private Long latencyMs;

    @Column(name = "is_trusted")
    private Boolean isTrusted;

    @Column(name = "connection_count")
    private Integer connectionCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "node_peers",
            joinColumns = @JoinColumn(name = "node_id"),
            inverseJoinColumns = @JoinColumn(name = "peer_id")
    )
    private List<Node> peers = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        lastSeen = LocalDateTime.now();
        if (status == null) {
            status = NodeStatus.ACTIVE;
        }
        if (isTrusted == null) {
            isTrusted = false;
        }
        if (connectionCount == null) {
            connectionCount = 0;
        }
        if (blockHeight == null) {
            blockHeight = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Node(String nodeId, String ipAddress, Integer port, NodeType nodeType) {
        this.nodeId = nodeId;
        this.ipAddress = ipAddress;
        this.port = port;
        this.nodeType = nodeType;
        this.status = NodeStatus.ACTIVE;
        this.version = "1.0.0";
    }

}