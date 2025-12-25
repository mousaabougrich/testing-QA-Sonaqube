package com.wallet.biochain.entities;

import com.wallet.biochain.enums.NodeStatus;
import com.wallet.biochain.enums.NodeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class NodeEntityTest {

    private Node node;

    @BeforeEach
    void setUp() {
        node = new Node();
    }

    @Test
    void testNodeCreation_AllFields() {
        // Given & When
        node.setId(1L);
        node.setNodeId("node-123");
        node.setIpAddress("192.168.1.100");
        node.setPort(8080);
        node.setStatus(NodeStatus.ACTIVE);
        node.setNodeType(NodeType.FULL_NODE);
        node.setVersion("1.0.0");
        node.setBlockHeight(1000);
        node.setLatencyMs(50L);
        node.setIsTrusted(true);
        node.setConnectionCount(10);

        // Then
        assertEquals(1L, node.getId());
        assertEquals("node-123", node.getNodeId());
        assertEquals("192.168.1.100", node.getIpAddress());
        assertEquals(8080, node.getPort());
        assertEquals(NodeStatus.ACTIVE, node.getStatus());
        assertEquals(NodeType.FULL_NODE, node.getNodeType());
        assertEquals("1.0.0", node.getVersion());
        assertEquals(1000, node.getBlockHeight());
        assertEquals(50L, node.getLatencyMs());
        assertTrue(node.getIsTrusted());
        assertEquals(10, node.getConnectionCount());
    }

    @Test
    void testNodeConstructor_WithParameters() {
        // When
        Node newNode = new Node("node-456", "192.168.1.200", 8081, NodeType.LIGHT_NODE);

        // Then
        assertEquals("node-456", newNode.getNodeId());
        assertEquals("192.168.1.200", newNode.getIpAddress());
        assertEquals(8081, newNode.getPort());
        assertEquals(NodeType.LIGHT_NODE, newNode.getNodeType());
        assertEquals(NodeStatus.ACTIVE, newNode.getStatus());
        assertEquals("1.0.0", newNode.getVersion());
    }

    @Test
    void testPrePersist_InitializesDefaults() {
        // Given
        node.setNodeId("node-123");
        node.setIpAddress("192.168.1.100");
        node.setPort(8080);
        node.setNodeType(NodeType.FULL_NODE);
        node.setVersion("1.0.0");

        // When
        node.onCreate();

        // Then
        assertNotNull(node.getCreatedAt());
        assertNotNull(node.getUpdatedAt());
        assertNotNull(node.getLastSeen());
        assertEquals(NodeStatus.ACTIVE, node.getStatus());
        assertFalse(node.getIsTrusted());
        assertEquals(0, node.getConnectionCount());
        assertEquals(0, node.getBlockHeight());
    }

    @Test
    void testPreUpdate_UpdatesTimestamp() {
        // Given
        node.onCreate();
        LocalDateTime originalCreated = node.getCreatedAt();
        LocalDateTime originalUpdated = node.getUpdatedAt();

        // When
        node.onUpdate();

        // Then
        assertNotNull(node.getUpdatedAt());
        assertEquals(originalCreated, node.getCreatedAt()); // createdAt should not change
        // updatedAt is set to now, so it should be equal or after original
        assertTrue(node.getUpdatedAt().isAfter(originalUpdated) ||
                   node.getUpdatedAt().isEqual(originalUpdated));
    }

    @Test
    void testNodeStatus_AllValues() {
        // Test all status values
        node.setStatus(NodeStatus.ACTIVE);
        assertEquals(NodeStatus.ACTIVE, node.getStatus());

        node.setStatus(NodeStatus.INACTIVE);
        assertEquals(NodeStatus.INACTIVE, node.getStatus());

        node.setStatus(NodeStatus.SYNCING);
        assertEquals(NodeStatus.SYNCING, node.getStatus());
    }

    @Test
    void testNodeType_AllValues() {
        // Test all type values
        node.setNodeType(NodeType.FULL_NODE);
        assertEquals(NodeType.FULL_NODE, node.getNodeType());

        node.setNodeType(NodeType.LIGHT_NODE);
        assertEquals(NodeType.LIGHT_NODE, node.getNodeType());

        node.setNodeType(NodeType.MINING_NODE);
        assertEquals(NodeType.MINING_NODE, node.getNodeType());
    }

    @Test
    void testPeers_EmptyList() {
        // Given
        node.setPeers(new ArrayList<>());

        // Then
        assertNotNull(node.getPeers());
        assertTrue(node.getPeers().isEmpty());
    }

    @Test
    void testPeers_AddPeer() {
        // Given
        node.setPeers(new ArrayList<>());
        Node peer = new Node();
        peer.setId(2L);
        peer.setNodeId("peer-123");

        // When
        node.getPeers().add(peer);

        // Then
        assertEquals(1, node.getPeers().size());
        assertEquals("peer-123", node.getPeers().get(0).getNodeId());
    }

    @Test
    void testPeers_MultiplePeers() {
        // Given
        node.setPeers(new ArrayList<>());
        Node peer1 = new Node();
        peer1.setId(2L);
        peer1.setNodeId("peer-1");
        Node peer2 = new Node();
        peer2.setId(3L);
        peer2.setNodeId("peer-2");

        // When
        node.getPeers().add(peer1);
        node.getPeers().add(peer2);

        // Then
        assertEquals(2, node.getPeers().size());
    }

    @Test
    void testLastSeen_Update() {
        // Given
        LocalDateTime lastSeenTime = LocalDateTime.now();
        node.setLastSeen(lastSeenTime);

        // Then
        assertEquals(lastSeenTime, node.getLastSeen());
    }

    @Test
    void testBlockHeight_Increment() {
        // Given
        node.setBlockHeight(100);

        // When
        node.setBlockHeight(node.getBlockHeight() + 1);

        // Then
        assertEquals(101, node.getBlockHeight());
    }

    @Test
    void testLatency_Measurement() {
        // Given
        Long latency = 25L;
        node.setLatencyMs(latency);

        // Then
        assertEquals(latency, node.getLatencyMs());
    }

    @Test
    void testConnectionCount_Increment() {
        // Given
        node.setConnectionCount(5);

        // When
        node.setConnectionCount(node.getConnectionCount() + 1);

        // Then
        assertEquals(6, node.getConnectionCount());
    }

    @Test
    void testConnectionCount_Decrement() {
        // Given
        node.setConnectionCount(5);

        // When
        node.setConnectionCount(node.getConnectionCount() - 1);

        // Then
        assertEquals(4, node.getConnectionCount());
    }

    @Test
    void testIsTrusted_Toggle() {
        // Given
        node.setIsTrusted(false);
        assertFalse(node.getIsTrusted());

        // When
        node.setIsTrusted(true);

        // Then
        assertTrue(node.getIsTrusted());
    }

    @Test
    void testVersion_Different() {
        // Given
        String[] versions = {"1.0.0", "1.0.1", "2.0.0", "1.5.3-beta"};

        // When & Then
        for (String version : versions) {
            node.setVersion(version);
            assertEquals(version, node.getVersion());
        }
    }

    @Test
    void testIpAddress_Different() {
        // Given
        String[] ipAddresses = {
            "192.168.1.1",
            "10.0.0.1",
            "172.16.0.1",
            "127.0.0.1"
        };

        // When & Then
        for (String ip : ipAddresses) {
            node.setIpAddress(ip);
            assertEquals(ip, node.getIpAddress());
        }
    }

    @Test
    void testPort_CommonPorts() {
        // Given
        Integer[] ports = {8080, 8081, 3000, 8888, 9999};

        // When & Then
        for (Integer port : ports) {
            node.setPort(port);
            assertEquals(port, node.getPort());
        }
    }

    @Test
    void testNodeId_UniqueIdentifier() {
        // Given
        String uniqueId = "node-" + System.currentTimeMillis();
        node.setNodeId(uniqueId);

        // Then
        assertEquals(uniqueId, node.getNodeId());
    }

    @Test
    void testBlockHeight_ZeroValue() {
        // Given
        node.setBlockHeight(0);

        // Then
        assertEquals(0, node.getBlockHeight());
    }

    @Test
    void testBlockHeight_LargeValue() {
        // Given
        node.setBlockHeight(1000000);

        // Then
        assertEquals(1000000, node.getBlockHeight());
    }

    @Test
    void testNodeEquality_SameId() {
        // Given
        Node node1 = new Node();
        node1.setId(1L);
        Node node2 = new Node();
        node2.setId(1L);

        // Then
        assertEquals(node1.getId(), node2.getId());
    }

    @Test
    void testNodeEquality_DifferentId() {
        // Given
        Node node1 = new Node();
        node1.setId(1L);
        Node node2 = new Node();
        node2.setId(2L);

        // Then
        assertNotEquals(node1.getId(), node2.getId());
    }

    @Test
    void testConnectionCount_NeverNegative() {
        // Given
        node.setConnectionCount(0);

        // Then
        assertTrue(node.getConnectionCount() >= 0);
    }

    @Test
    void testLatency_LowValue() {
        // Given
        node.setLatencyMs(5L);

        // Then
        assertEquals(5L, node.getLatencyMs());
    }

    @Test
    void testLatency_HighValue() {
        // Given
        node.setLatencyMs(5000L);

        // Then
        assertEquals(5000L, node.getLatencyMs());
    }
}

