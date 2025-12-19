package com.wallet.biochain.mappers;

import com.wallet.biochain.dto.NodeInfoDTO;
import com.wallet.biochain.dto.PeerListDTO;
import com.wallet.biochain.entities.Node;
import com.wallet.biochain.enums.NodeStatus;
import com.wallet.biochain.enums.NodeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NodeMapperTest {

    private NodeMapper nodeMapper;
    private Node node;

    @BeforeEach
    void setUp() {
        nodeMapper = new NodeMapper();

        node = new Node();
        node.setId(1L);
        node.setNodeId("node-001");
        node.setIpAddress("192.168.1.100");
        node.setPort(8080);
        node.setStatus(NodeStatus.ACTIVE);
        node.setNodeType(NodeType.FULL_NODE);
        node.setLastSeen(LocalDateTime.now());
        node.setBlockHeight(1000);
        node.setVersion("1.0.0");
        node.setLatencyMs(50L);
        node.setIsTrusted(true);
        node.setConnectionCount(10);
    }

    @Test
    void testToDTO_success() {
        NodeInfoDTO result = nodeMapper.toDTO(node);

        assertNotNull(result);
        assertEquals(node.getId(), result.id());
        assertEquals(node.getNodeId(), result.nodeId());
        assertEquals(node.getIpAddress(), result.ipAddress());
        assertEquals(node.getPort(), result.port());
        assertEquals(node.getStatus(), result.status());
        assertEquals(node.getNodeType(), result.nodeType());
        assertEquals(node.getVersion(), result.version());
        assertEquals(node.getIsTrusted(), result.isTrusted());
    }

    @Test
    void testToDTO_withNullNode() {
        NodeInfoDTO result = nodeMapper.toDTO(null);

        assertNull(result);
    }

    @Test
    void testToDTO_withNullPeers() {
        node.setPeers(null);
        NodeInfoDTO result = nodeMapper.toDTO(node);

        assertNotNull(result);
        assertEquals(0, result.peerCount());
    }

    @Test
    void testToDTO_withPeers() {
        Node peer1 = new Node();
        peer1.setId(2L);
        Node peer2 = new Node();
        peer2.setId(3L);

        node.setPeers(Arrays.asList(peer1, peer2));
        NodeInfoDTO result = nodeMapper.toDTO(node);

        assertNotNull(result);
        assertEquals(2, result.peerCount());
    }

    @Test
    void testToDTOList_success() {
        Node node2 = new Node();
        node2.setId(2L);
        node2.setNodeId("node-002");
        node2.setIpAddress("192.168.1.101");
        node2.setPort(8081);
        node2.setStatus(NodeStatus.INACTIVE);
        node2.setNodeType(NodeType.LIGHT_NODE);
        node2.setBlockHeight(999);

        List<Node> nodes = Arrays.asList(node, node2);
        List<NodeInfoDTO> result = nodeMapper.toDTOList(nodes);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(node.getId(), result.get(0).id());
        assertEquals(node2.getId(), result.get(1).id());
    }

    @Test
    void testToDTOList_withNullList() {
        List<NodeInfoDTO> result = nodeMapper.toDTOList(null);

        assertNull(result);
    }

    @Test
    void testToDTOList_emptyList() {
        List<NodeInfoDTO> result = nodeMapper.toDTOList(new ArrayList<>());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testToPeerListDTO_success() {
        Node peer1 = new Node();
        peer1.setId(2L);
        peer1.setNodeId("peer-001");
        peer1.setStatus(NodeStatus.ACTIVE);

        Node peer2 = new Node();
        peer2.setId(3L);
        peer2.setNodeId("peer-002");
        peer2.setStatus(NodeStatus.ACTIVE);

        List<Node> peers = Arrays.asList(peer1, peer2);
        PeerListDTO result = nodeMapper.toPeerListDTO(peers);

        assertNotNull(result);
        assertEquals(2, result.totalPeers());
        assertEquals(2, result.connectedPeers());
        assertEquals(2, result.peers().size());
    }

    @Test
    void testToPeerListDTO_withNullList() {
        PeerListDTO result = nodeMapper.toPeerListDTO(null);

        assertNull(result);
    }
}

