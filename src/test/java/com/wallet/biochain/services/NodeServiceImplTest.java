package com.wallet.biochain.services;

import com.wallet.biochain.config.NetworkConfig;
import com.wallet.biochain.dto.NodeConnectionRequestDTO;
import com.wallet.biochain.dto.NodeInfoDTO;
import com.wallet.biochain.dto.PeerListDTO;
import com.wallet.biochain.entities.Node;
import com.wallet.biochain.enums.NodeStatus;
import com.wallet.biochain.enums.NodeType;
import com.wallet.biochain.mappers.NodeMapper;
import com.wallet.biochain.repositories.NodeRepository;
import com.wallet.biochain.services.impl.NodeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class NodeServiceImplTest {

    @Mock
    private NodeRepository nodeRepository;
    @Mock
    private NodeMapper nodeMapper;
    @Mock
    private NetworkConfig networkConfig;

    @InjectMocks
    private NodeServiceImpl nodeService;

    private NetworkConfig.P2P p2pConfig;

    @BeforeEach
    void setUp() {
        p2pConfig = new NetworkConfig.P2P();
        p2pConfig.setPingInterval(1000L);
        p2pConfig.setMaxPeers(50);
        p2pConfig.setMinPeers(3);

        when(networkConfig.getP2p()).thenReturn(p2pConfig);
    }

    @Test
    void registerNode_newNode() {
        NodeConnectionRequestDTO req = new NodeConnectionRequestDTO(
                "id", "127.0.0.1", 8080, NodeType.FULL_NODE, "v1"
        );

        when(nodeRepository.findByNodeId("id")).thenReturn(Optional.empty());

        Node node = new Node();
        node.setNodeId("id");
        when(nodeRepository.save(any(Node.class))).thenReturn(node);

        NodeInfoDTO dto = mock(NodeInfoDTO.class);
        when(nodeMapper.toDTO(node)).thenReturn(dto);

        NodeInfoDTO result = nodeService.registerNode(req);

        assertEquals(dto, result);
        verify(nodeRepository).save(any(Node.class));
    }

    @Test
    void registerNode_existingUpdatesStatus() {
        Node existing = new Node();
        existing.setNodeId("id");
        existing.setStatus(NodeStatus.INACTIVE);

        when(nodeRepository.findByNodeId("id")).thenReturn(Optional.of(existing));
        when(nodeRepository.save(existing)).thenReturn(existing);

        NodeInfoDTO dto = mock(NodeInfoDTO.class);
        when(nodeMapper.toDTO(existing)).thenReturn(dto);

        NodeConnectionRequestDTO req = new NodeConnectionRequestDTO(
                "id", "127.0.0.1", 8080, NodeType.FULL_NODE, "v1"
        );

        NodeInfoDTO result = nodeService.registerNode(req);

        assertEquals(dto, result);
        assertEquals(NodeStatus.ACTIVE, existing.getStatus());
        assertNotNull(existing.getLastSeen());
    }

    @Test
    void getNodeById_returnsDto() {
        Node node = new Node();
        node.setId(1L);

        when(nodeRepository.findById(1L)).thenReturn(Optional.of(node));

        NodeInfoDTO dto = mock(NodeInfoDTO.class);
        when(nodeMapper.toDTO(node)).thenReturn(dto);

        Optional<NodeInfoDTO> result = nodeService.getNodeById(1L);

        assertTrue(result.isPresent());
        assertEquals(dto, result.get());
    }

    @Test
    void getActiveNodes_returnsList() {
        Node n1 = new Node();
        n1.setStatus(NodeStatus.ACTIVE);

        when(nodeRepository.findByStatus(NodeStatus.ACTIVE))
                .thenReturn(List.of(n1));

        when(nodeMapper.toDTOList(anyList()))
                .thenReturn(List.of(mock(NodeInfoDTO.class)));

        List<NodeInfoDTO> result = nodeService.getActiveNodes();

        assertEquals(1, result.size());
    }

    @Test
    void updateNodeStatus_updatesAndSaves() {
        Node node = new Node();
        node.setNodeId("n1");
        node.setStatus(NodeStatus.ACTIVE);

        when(nodeRepository.findByNodeId("n1")).thenReturn(Optional.of(node));

        nodeService.updateNodeStatus("n1", NodeStatus.INACTIVE);

        assertEquals(NodeStatus.INACTIVE, node.getStatus());
        assertNotNull(node.getLastSeen());
        verify(nodeRepository).save(node);
    }

    @Test
    void updateBlockHeight_updatesAndSaves() {
        Node node = new Node();
        node.setNodeId("n1");
        node.setBlockHeight(5);

        when(nodeRepository.findByNodeId("n1")).thenReturn(Optional.of(node));

        nodeService.updateBlockHeight("n1", 10);

        assertEquals(10, node.getBlockHeight());
        assertNotNull(node.getLastSeen());
        verify(nodeRepository).save(node);
    }

    @Test
    void connectToPeer_addsPeer() {
        Node node = new Node();
        node.setNodeId("n1");
        node.setPeers(new ArrayList<>());

        Node peer = new Node();
        peer.setNodeId("n2");

        when(nodeRepository.findByNodeId("n1")).thenReturn(Optional.of(node));
        when(nodeRepository.findByNodeId("n2")).thenReturn(Optional.of(peer));

        nodeService.connectToPeer("n1", "n2");

        assertTrue(node.getPeers().contains(peer));
        assertEquals(1, node.getConnectionCount());
        verify(nodeRepository).save(node);
    }

    @Test
    void connectToPeer_alreadyConnected_doesNotDuplicate() {
        Node peer = new Node();
        peer.setNodeId("n2");

        Node node = new Node();
        node.setNodeId("n1");
        node.setPeers(new ArrayList<>(List.of(peer)));
        node.setConnectionCount(1);

        when(nodeRepository.findByNodeId("n1")).thenReturn(Optional.of(node));
        when(nodeRepository.findByNodeId("n2")).thenReturn(Optional.of(peer));

        nodeService.connectToPeer("n1", "n2");

        assertEquals(1, node.getPeers().size());
        verify(nodeRepository, never()).save(node);
    }

    @Test
    void disconnectFromPeer_removesPeer() {
        Node peer = new Node();
        peer.setNodeId("n2");

        Node node = new Node();
        node.setNodeId("n1");
        node.setPeers(new ArrayList<>(List.of(peer)));
        node.setConnectionCount(1);

        when(nodeRepository.findByNodeId("n1")).thenReturn(Optional.of(node));
        when(nodeRepository.findByNodeId("n2")).thenReturn(Optional.of(peer));

        nodeService.disconnectFromPeer("n1", "n2");

        assertFalse(node.getPeers().contains(peer));
        assertEquals(0, node.getConnectionCount());
        verify(nodeRepository).save(node);
    }

    @Test
    void getPeerList_usesMapper() {
        Node node = new Node();
        node.setNodeId("n1");
        node.setPeers(new ArrayList<>());

        when(nodeRepository.findByNodeId("n1")).thenReturn(Optional.of(node));

        PeerListDTO dto = mock(PeerListDTO.class);
        when(nodeMapper.toPeerListDTO(node.getPeers())).thenReturn(dto);

        PeerListDTO result = nodeService.getPeerList("n1");

        assertEquals(dto, result);
    }

    @Test
    void pingNode_updatesLastSeen() {
        Node node = new Node();
        node.setNodeId("n1");

        when(nodeRepository.findByNodeId("n1")).thenReturn(Optional.of(node));

        assertTrue(nodeService.pingNode("n1"));
        assertNotNull(node.getLastSeen());
        verify(nodeRepository).save(node);
    }

    @Test
    void pingNode_notFound_returnsFalse() {
        when(nodeRepository.findByNodeId("n1")).thenReturn(Optional.empty());

        assertFalse(nodeService.pingNode("n1"));
    }

    @Test
    void getNodeLatency_returnsValue() {
        Node node = new Node();
        node.setNodeId("n1");

        when(nodeRepository.findByNodeId("n1")).thenReturn(Optional.of(node));

        Long latency = nodeService.getNodeLatency("n1");

        assertNotNull(latency);
        assertTrue(latency >= 50 && latency <= 250);
        verify(nodeRepository).save(node);
    }

    @Test
    void trustNode_setsFlag() {
        Node node = new Node();
        node.setNodeId("n1");
        node.setIsTrusted(false);

        when(nodeRepository.findByNodeId("n1")).thenReturn(Optional.of(node));

        nodeService.trustNode("n1");

        assertTrue(node.getIsTrusted());
        verify(nodeRepository).save(node);
    }

    @Test
    void untrustNode_unsetsFlag() {
        Node node = new Node();
        node.setNodeId("n1");
        node.setIsTrusted(true);

        when(nodeRepository.findByNodeId("n1")).thenReturn(Optional.of(node));

        nodeService.untrustNode("n1");

        assertFalse(node.getIsTrusted());
        verify(nodeRepository).save(node);
    }

    @Test
    void removeStaleNodes_marksDisconnected() {
        Node stale = new Node();
        stale.setNodeId("stale");
        stale.setStatus(NodeStatus.ACTIVE);

        when(nodeRepository.findStaleNodes(any(LocalDateTime.class)))
                .thenReturn(List.of(stale));

        nodeService.removeStaleNodes();

        assertEquals(NodeStatus.DISCONNECTED, stale.getStatus());
        verify(nodeRepository).save(stale);
    }

    @Test
    void removeStaleNodes_noStaleNodes_doesNothing() {
        when(nodeRepository.findStaleNodes(any(LocalDateTime.class)))
                .thenReturn(List.of());

        nodeService.removeStaleNodes();

        verify(nodeRepository, never()).save(any());
    }
}
