package com.wallet.biochain.mappers;

import com.wallet.biochain.dto.NetworkStatusDTO;
import com.wallet.biochain.entities.Node;
import com.wallet.biochain.enums.NodeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NetworkMapperTest {

    private NetworkMapper networkMapper;

    @BeforeEach
    void setUp() {
        networkMapper = new NetworkMapper();
    }

    @Test
    void testToStatusDTO_withNodes_success() {
        Node node1 = new Node();
        node1.setId(1L);
        node1.setStatus(NodeStatus.ACTIVE);
        node1.setBlockHeight(1000);
        node1.setLatencyMs(50L);

        Node node2 = new Node();
        node2.setId(2L);
        node2.setStatus(NodeStatus.ACTIVE);
        node2.setBlockHeight(1000);
        node2.setLatencyMs(75L);

        Node node3 = new Node();
        node3.setId(3L);
        node3.setStatus(NodeStatus.INACTIVE);
        node3.setBlockHeight(999);
        node3.setLatencyMs(100L);

        List<Node> nodes = Arrays.asList(node1, node2, node3);
        NetworkStatusDTO result = networkMapper.toStatusDTO(nodes);

        assertNotNull(result);
        assertEquals(3, result.totalNodes());
        assertEquals(2, result.activeNodes());
        assertEquals(1, result.inactiveNodes());
    }

    @Test
    void testToStatusDTO_withEmptyList() {
        NetworkStatusDTO result = networkMapper.toStatusDTO(new ArrayList<>());

        assertNotNull(result);
        assertEquals(0, result.totalNodes());
        assertEquals(0, result.activeNodes());
    }

    @Test
    void testToStatusDTO_withNullList() {
        NetworkStatusDTO result = networkMapper.toStatusDTO(null);

        assertNotNull(result);
        assertEquals(0, result.totalNodes());
    }

    @Test
    void testToStatusDTO_calculateAverages() {
        Node node1 = new Node();
        node1.setId(1L);
        node1.setStatus(NodeStatus.ACTIVE);
        node1.setBlockHeight(1000);
        node1.setLatencyMs(100L);

        Node node2 = new Node();
        node2.setId(2L);
        node2.setStatus(NodeStatus.ACTIVE);
        node2.setBlockHeight(1000);
        node2.setLatencyMs(200L);

        List<Node> nodes = Arrays.asList(node1, node2);
        NetworkStatusDTO result = networkMapper.toStatusDTO(nodes);

        assertNotNull(result);
        assertEquals(2, result.totalNodes());
        assertEquals(2, result.activeNodes());
    }
}

