package com.wallet.biochain.services;

import com.wallet.biochain.config.NetworkConfig;
import com.wallet.biochain.dto.NetworkStatusDTO;
import com.wallet.biochain.entities.Block;
import com.wallet.biochain.entities.Node;
import com.wallet.biochain.entities.Transaction;
import com.wallet.biochain.enums.NodeStatus;
import com.wallet.biochain.mappers.NetworkMapper;
import com.wallet.biochain.repositories.NodeRepository;
import com.wallet.biochain.services.impl.P2PNetworkServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class P2PNetworkServiceImplTest {

    @Mock
    private NodeRepository nodeRepository;
    @Mock
    private NetworkMapper networkMapper;
    @Mock
    private NetworkConfig networkConfig;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private P2PNetworkServiceImpl p2pService;

    private NetworkConfig.P2P p2pConfig;
    private NetworkConfig.Node nodeConfig;

    @BeforeEach
    void setUp() {
        // Create real nested config objects
        p2pConfig = new NetworkConfig.P2P();
        p2pConfig.setMaxPeers(50);
        p2pConfig.setMinPeers(3);
        p2pConfig.setConnectionTimeout(30000L);
        p2pConfig.setPingInterval(60000L);
        p2pConfig.setSyncInterval(300000L);
        p2pConfig.setSeedNodes(List.of("localhost:8546", "localhost:8547"));
        p2pConfig.setDiscoveryEnabled(true);

        nodeConfig = new NetworkConfig.Node();
        nodeConfig.setNodeId("test-node");
        nodeConfig.setHost("localhost");
        nodeConfig.setPort(8545);
        nodeConfig.setEnabled(true);

        // Mock the getters
        when(networkConfig.getP2p()).thenReturn(p2pConfig);
        when(networkConfig.getNode()).thenReturn(nodeConfig);
    }

    @Test
    void broadcastBlock_sendsToActiveNodesAndWebSocket() {
        Node n = new Node();
        n.setNodeId("n1");
        n.setStatus(NodeStatus.ACTIVE);

        when(nodeRepository.findByStatus(NodeStatus.ACTIVE)).thenReturn(List.of(n));

        Block block = new Block();
        block.setBlockIndex(1);

        p2pService.broadcastBlock(block);

        verify(nodeRepository).findByStatus(NodeStatus.ACTIVE);
        verify(messagingTemplate).convertAndSend("/topic/blocks", block);
    }

    @Test
    void broadcastBlock_noActiveNodes_stillBroadcastsWebSocket() {
        when(nodeRepository.findByStatus(NodeStatus.ACTIVE)).thenReturn(List.of());

        Block block = new Block();
        block.setBlockIndex(1);

        p2pService.broadcastBlock(block);

        verify(messagingTemplate).convertAndSend("/topic/blocks", block);
    }

    @Test
    void broadcastTransaction_sendsToActiveNodesAndWebSocket() {
        Node n = new Node();
        n.setNodeId("n1");
        n.setStatus(NodeStatus.ACTIVE);

        when(nodeRepository.findByStatus(NodeStatus.ACTIVE)).thenReturn(List.of(n));

        Transaction tx = new Transaction();
        tx.setTransactionHash("h");

        p2pService.broadcastTransaction(tx);

        verify(nodeRepository).findByStatus(NodeStatus.ACTIVE);
        verify(messagingTemplate).convertAndSend("/topic/transactions", tx);
    }

    @Test
    void discoverPeers_collectsSeedAndExistingPeers() {
        Node n = new Node();
        n.setStatus(NodeStatus.ACTIVE);
        Node peer = new Node();
        peer.setIpAddress("127.0.0.1");
        peer.setPort(8080);
        n.setPeers(new ArrayList<>(List.of(peer)));

        when(nodeRepository.findByStatus(NodeStatus.ACTIVE)).thenReturn(List.of(n));

        List<String> peers = p2pService.discoverPeers();

        assertTrue(peers.contains("localhost:8546"));
        assertTrue(peers.contains("localhost:8547"));
        assertTrue(peers.contains("127.0.0.1:8080"));
    }

    @Test
    void discoverPeers_handlesEmptySeedNodes() {
        p2pConfig.setSeedNodes(List.of());

        when(nodeRepository.findByStatus(NodeStatus.ACTIVE)).thenReturn(List.of());

        List<String> peers = p2pService.discoverPeers();

        assertTrue(peers.isEmpty());
    }

    @Test
    void connectToNetwork_disabledDoesNothing() {
        nodeConfig.setEnabled(false);

        p2pService.connectToNetwork();

        verify(networkConfig).getNode();
        verifyNoInteractions(nodeRepository);
    }

    @Test
    void connectToNetwork_enabledDiscoversPeers() {
        when(nodeRepository.findByStatus(NodeStatus.ACTIVE)).thenReturn(List.of());

        p2pService.connectToNetwork();

        verify(networkConfig, atLeastOnce()).getP2p();
        verify(networkConfig, atLeastOnce()).getNode();
    }

    @Test
    void disconnectFromNetwork_setsAllNodesDisconnected() {
        Node n1 = new Node();
        n1.setNodeId("n1");
        n1.setStatus(NodeStatus.ACTIVE);

        Node n2 = new Node();
        n2.setNodeId("n2");
        n2.setStatus(NodeStatus.ACTIVE);

        when(nodeRepository.findByStatus(NodeStatus.ACTIVE))
                .thenReturn(List.of(n1, n2));

        p2pService.disconnectFromNetwork();

        assertEquals(NodeStatus.DISCONNECTED, n1.getStatus());
        assertEquals(NodeStatus.DISCONNECTED, n2.getStatus());
        verify(nodeRepository, times(2)).save(any(Node.class));
    }

    @Test
    void getNetworkStatus_usesMapper() {
        List<Node> all = List.of(new Node());
        when(nodeRepository.findAll()).thenReturn(all);

        NetworkStatusDTO dto = mock(NetworkStatusDTO.class);
        when(networkMapper.toStatusDTO(all)).thenReturn(dto);

        NetworkStatusDTO result = p2pService.getNetworkStatus();

        assertEquals(dto, result);
    }

    @Test
    void sendMessageToPeer_nodeMustExist() {
        Node n = new Node();
        n.setNodeId("n1");
        when(nodeRepository.findByNodeId("n1")).thenReturn(Optional.of(n));

        assertDoesNotThrow(() -> p2pService.sendMessageToPeer("n1", "msg"));
    }

    @Test
    void sendMessageToPeer_nodeNotFound_throws() {
        when(nodeRepository.findByNodeId("n1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> p2pService.sendMessageToPeer("n1", "msg"));
    }

    @Test
    void broadcastMessage_sendsToAllActiveAndWebSocket() {
        Node n1 = new Node();
        n1.setNodeId("n1");

        Node n2 = new Node();
        n2.setNodeId("n2");

        when(nodeRepository.findByStatus(NodeStatus.ACTIVE))
                .thenReturn(List.of(n1, n2));
        when(nodeRepository.findByNodeId("n1")).thenReturn(Optional.of(n1));
        when(nodeRepository.findByNodeId("n2")).thenReturn(Optional.of(n2));

        p2pService.broadcastMessage("test message");

        verify(messagingTemplate).convertAndSend("/topic/messages", "test message");
    }

    @Test
    void isNetworkHealthy_comparesCountWithMinPeers() {
        when(nodeRepository.countByStatus(NodeStatus.ACTIVE)).thenReturn(5L);

        assertTrue(p2pService.isNetworkHealthy());
    }

    @Test
    void isNetworkHealthy_falseWhenBelowMinimum() {
        when(nodeRepository.countByStatus(NodeStatus.ACTIVE)).thenReturn(2L);

        assertFalse(p2pService.isNetworkHealthy());
    }

    @Test
    void getConnectedPeersCount_delegatesToRepository() {
        when(nodeRepository.countByStatus(NodeStatus.ACTIVE)).thenReturn(4L);

        assertEquals(4, p2pService.getConnectedPeersCount());
    }

    @Test
    void requestBlockchainFromPeer_sendsPeerMessage() {
        Node n = new Node();
        n.setNodeId("peer1");
        when(nodeRepository.findByNodeId("peer1")).thenReturn(Optional.of(n));

        p2pService.requestBlockchainFromPeer("peer1");

        verify(nodeRepository).findByNodeId("peer1");
    }

    @Test
    void synchronizeWithNetwork_logsAction() {
        assertDoesNotThrow(() -> p2pService.synchronizeWithNetwork());
    }
}