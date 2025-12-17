package com.wallet.biochain.services;

import com.wallet.biochain.config.NetworkConfig;
import com.wallet.biochain.dto.BlockValidationDTO;
import com.wallet.biochain.entities.Block;
import com.wallet.biochain.entities.Node;
import com.wallet.biochain.enums.NodeStatus;
import com.wallet.biochain.repositories.BlockRepository;
import com.wallet.biochain.repositories.NodeRepository;
import com.wallet.biochain.services.impl.BlockchainSyncServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class BlockchainSyncServiceImplTest {

    @Mock
    private BlockRepository blockRepository;
    @Mock
    private NodeRepository nodeRepository;
    @Mock
    private BlockService blockService;
    @Mock
    private BlockchainService blockchainService;
    @Mock
    private NetworkConfig networkConfig;

    @InjectMocks
    private BlockchainSyncServiceImpl syncService;

    private NetworkConfig.Sync syncConfig;
    private NetworkConfig.Node nodeConfig;

    @BeforeEach
    void setUp() {
        // Create real nested config objects
        syncConfig = new NetworkConfig.Sync();
        syncConfig.setBatchSize(100);
        syncConfig.setTimeout(60000L);
        syncConfig.setAutoSync(true);
        syncConfig.setMaxRetries(3);

        nodeConfig = new NetworkConfig.Node();
        nodeConfig.setNodeId("test-node-001");
        nodeConfig.setHost("localhost");
        nodeConfig.setPort(8545);
        nodeConfig.setEnabled(true);

        // Mock the getters
        when(networkConfig.getSync()).thenReturn(syncConfig);
        when(networkConfig.getNode()).thenReturn(nodeConfig);
    }

    @Test
    void synchronize_noActiveNodes_doesNothing() {
        when(nodeRepository.findByStatus(NodeStatus.ACTIVE)).thenReturn(List.of());

        syncService.synchronize();

        verify(nodeRepository).findByStatus(NodeStatus.ACTIVE);
        verifyNoInteractions(blockRepository);
    }

    @Test
    void synchronize_selectsBestNode() {
        Node n1 = new Node();
        n1.setNodeId("n1");
        n1.setStatus(NodeStatus.ACTIVE);
        n1.setBlockHeight(5);

        Node n2 = new Node();
        n2.setNodeId("n2");
        n2.setStatus(NodeStatus.ACTIVE);
        n2.setBlockHeight(10);

        when(nodeRepository.findByStatus(NodeStatus.ACTIVE))
                .thenReturn(List.of(n1, n2));
        when(nodeRepository.findByNodeId("n2")).thenReturn(Optional.of(n2));
        when(blockRepository.findMaxBlockIndex()).thenReturn(3);

        syncService.synchronize();

        verify(nodeRepository, atLeastOnce()).findByStatus(NodeStatus.ACTIVE);
    }

    @Test
    void synchronize_nodeWithNullHeight_skipped() {
        Node n1 = new Node();
        n1.setNodeId("n1");
        n1.setStatus(NodeStatus.ACTIVE);
        n1.setBlockHeight(null);

        when(nodeRepository.findByStatus(NodeStatus.ACTIVE))
                .thenReturn(List.of(n1));

        syncService.synchronize();

        verify(nodeRepository).findByStatus(NodeStatus.ACTIVE);
        verify(nodeRepository, never()).findByNodeId(anyString());
    }

    @Test
    void needsSynchronization_trueWhenPeerHigher() {
        when(blockRepository.findMaxBlockIndex()).thenReturn(5);

        Node peer = new Node();
        peer.setNodeId("p1");
        peer.setStatus(NodeStatus.ACTIVE);
        peer.setBlockHeight(8);

        when(nodeRepository.findByStatus(NodeStatus.ACTIVE))
                .thenReturn(List.of(peer));

        assertTrue(syncService.needsSynchronization());
    }

    @Test
    void needsSynchronization_falseWhenNoHigher() {
        when(blockRepository.findMaxBlockIndex()).thenReturn(10);

        Node peer = new Node();
        peer.setStatus(NodeStatus.ACTIVE);
        peer.setBlockHeight(8);

        when(nodeRepository.findByStatus(NodeStatus.ACTIVE))
                .thenReturn(List.of(peer));

        assertFalse(syncService.needsSynchronization());
    }

    @Test
    void needsSynchronization_falseWhenNoPeers() {
        when(blockRepository.findMaxBlockIndex()).thenReturn(5);
        when(nodeRepository.findByStatus(NodeStatus.ACTIVE))
                .thenReturn(List.of());

        assertFalse(syncService.needsSynchronization());
    }

    @Test
    void needsSynchronization_handlesNullCurrentHeight() {
        when(blockRepository.findMaxBlockIndex()).thenReturn(null);

        Node peer = new Node();
        peer.setStatus(NodeStatus.ACTIVE);
        peer.setBlockHeight(5);

        when(nodeRepository.findByStatus(NodeStatus.ACTIVE))
                .thenReturn(List.of(peer));

        assertTrue(syncService.needsSynchronization());
    }

    @Test
    void getSyncProgress_100WhenNoPeers() {
        when(blockRepository.findMaxBlockIndex()).thenReturn(0);
        when(nodeRepository.findByStatus(NodeStatus.ACTIVE)).thenReturn(List.of());

        assertEquals(100, syncService.getSyncProgress());
    }

    @Test
    void getSyncProgress_calculatesCorrectPercentage() {
        when(blockRepository.findMaxBlockIndex()).thenReturn(50);

        Node peer = new Node();
        peer.setStatus(NodeStatus.ACTIVE);
        peer.setBlockHeight(100);

        when(nodeRepository.findByStatus(NodeStatus.ACTIVE))
                .thenReturn(List.of(peer));

        assertEquals(50, syncService.getSyncProgress());
    }

    @Test
    void validateReceivedBlocks_validChainTrue() {
        Block b1 = new Block();
        b1.setBlockIndex(1);
        b1.setHash("h1");

        Block b2 = new Block();
        b2.setBlockIndex(2);
        b2.setPreviousHash("h1");
        b2.setHash("h2");

        when(blockService.validateBlock(b1)).thenReturn(
                new BlockValidationDTO(true, "h1", 1, List.of(), "ok")
        );
        when(blockService.validateBlock(b2)).thenReturn(
                new BlockValidationDTO(true, "h2", 2, List.of(), "ok")
        );
        when(blockRepository.findByBlockIndex(0)).thenReturn(Optional.empty());

        assertTrue(syncService.validateReceivedBlocks(List.of(b1, b2)));
    }

    @Test
    void validateReceivedBlocks_invalidBlock_returnsFalse() {
        Block b1 = new Block();
        b1.setBlockIndex(1);
        b1.setHash("h1");

        when(blockService.validateBlock(b1)).thenReturn(
                new BlockValidationDTO(false, "h1", 1,
                        List.of("Invalid hash"), "invalid")
        );

        assertFalse(syncService.validateReceivedBlocks(List.of(b1)));
    }

    @Test
    void validateReceivedBlocks_emptyList_returnsTrue() {
        assertTrue(syncService.validateReceivedBlocks(List.of()));
    }

    @Test
    void validateReceivedBlocks_nullList_returnsTrue() {
        assertTrue(syncService.validateReceivedBlocks(null));
    }

    @Test
    void validateReceivedBlocks_brokenChain_returnsFalse() {
        Block b1 = new Block();
        b1.setBlockIndex(1);
        b1.setHash("h1");

        Block b2 = new Block();
        b2.setBlockIndex(2);
        b2.setPreviousHash("wrong");
        b2.setHash("h2");

        when(blockService.validateBlock(b1)).thenReturn(
                new BlockValidationDTO(true, "h1", 1, List.of(), "ok")
        );
        when(blockService.validateBlock(b2)).thenReturn(
                new BlockValidationDTO(true, "h2", 2, List.of(), "ok")
        );

        assertFalse(syncService.validateReceivedBlocks(List.of(b1, b2)));
    }

    @Test
    void getMissingBlocks_identifiesGaps() {
        Block b1 = new Block();
        b1.setBlockIndex(1);
        Block b3 = new Block();
        b3.setBlockIndex(3);

        when(blockRepository.findBlocksInRange(1, 3))
                .thenReturn(List.of(b1, b3));

        List<Block> missing = syncService.getMissingBlocks(1, 3);

        assertEquals(1, missing.size());
        assertEquals(2, missing.get(0).getBlockIndex());
    }

    @Test
    void requestBlocksFromPeer_updatesLastSeen() {
        Node peer = new Node();
        peer.setNodeId("peer1");

        when(nodeRepository.findByNodeId("peer1")).thenReturn(Optional.of(peer));
        when(blockRepository.findBlocksInRange(1, 10))
                .thenReturn(new ArrayList<>());

        syncService.requestBlocksFromPeer("peer1", 1, 10);

        assertNotNull(peer.getLastSeen());
        verify(nodeRepository).save(peer);
    }

    @Test
    void requestBlocksFromPeer_notFound_throws() {
        when(nodeRepository.findByNodeId("peer1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> syncService.requestBlocksFromPeer("peer1", 1, 10));
    }

    @Test
    void addSynchronizedBlocks_skipsDuplicates() {
        Block block = new Block();
        block.setBlockIndex(1);

        when(blockRepository.existsByBlockIndex(1)).thenReturn(true);

        syncService.addSynchronizedBlocks(List.of(block));

        verify(blockRepository).existsByBlockIndex(1);
        verify(blockService, never()).addBlock(any());
    }

    @Test
    void synchronizeWithPeer_alreadyUpToDate_returnsEarly() {
        Node peer = new Node();
        peer.setNodeId("peer1");
        peer.setBlockHeight(5);

        when(nodeRepository.findByNodeId("peer1")).thenReturn(Optional.of(peer));
        when(blockRepository.findMaxBlockIndex()).thenReturn(10);

        syncService.synchronizeWithPeer("peer1");

        verify(blockRepository).findMaxBlockIndex();
        verify(blockRepository, never()).findBlocksInRange(anyInt(), anyInt());
    }
}
