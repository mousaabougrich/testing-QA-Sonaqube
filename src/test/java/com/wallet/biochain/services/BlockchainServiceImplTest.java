package com.wallet.biochain.services;

import com.wallet.biochain.config.BlockchainConfig;
import com.wallet.biochain.dto.BlockchainStatusDTO;
import com.wallet.biochain.entities.Block;
import com.wallet.biochain.entities.Blockchain;
import com.wallet.biochain.enums.ConsensusType;
import com.wallet.biochain.mappers.BlockchainMapper;
import com.wallet.biochain.repositories.BlockRepository;
import com.wallet.biochain.repositories.BlockchainRepository;
import com.wallet.biochain.repositories.TransactionRepository;
import com.wallet.biochain.services.impl.BlockchainServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class BlockchainServiceImplTest {

    @Mock
    private BlockchainRepository blockchainRepository;
    @Mock
    private BlockRepository blockRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private BlockService blockService;
    @Mock
    private BlockchainMapper blockchainMapper;
    @Mock
    private BlockchainConfig blockchainConfig;

    @InjectMocks
    private BlockchainServiceImpl blockchainService;

    private BlockchainConfig.Genesis genesisConfig;
    private BlockchainConfig.Mining miningConfig;

    @BeforeEach
    void setUp() {
        genesisConfig = new BlockchainConfig.Genesis();
        genesisConfig.setPreviousHash("0000000000000000000000000000000000000000000000000000000000000000");
        genesisConfig.setTimestamp(1609459200000L);
        genesisConfig.setMinerAddress("0x0000000000000000000000000000000000000000");

        miningConfig = new BlockchainConfig.Mining();
        miningConfig.setInitialDifficulty(4);
        miningConfig.setInitialReward(new BigDecimal("50"));
        miningConfig.setTargetBlockTime(600000L);

        when(blockchainConfig.getChainId()).thenReturn("biochain-001");
        when(blockchainConfig.getGenesis()).thenReturn(genesisConfig);
        when(blockchainConfig.getMining()).thenReturn(miningConfig);
    }

    @Test
    void initializeBlockchain_newChain() {
        when(blockchainRepository.findByName("TestChain")).thenReturn(Optional.empty());

        Block genesisBlock = new Block();
        genesisBlock.setHash("genesisHash");
        when(blockRepository.save(any(Block.class))).thenReturn(genesisBlock);
        when(blockService.calculateMerkleRoot(anyList())).thenReturn("merkleRoot");
        when(blockService.calculateBlockHash(any())).thenReturn("genesisHash");

        Blockchain blockchain = new Blockchain();
        when(blockchainRepository.save(any(Blockchain.class))).thenReturn(blockchain);

        Blockchain result = blockchainService.initializeBlockchain("TestChain",
                ConsensusType.PROOF_OF_WORK);

        assertNotNull(result);
        verify(blockRepository, times(2)).save(any(Block.class));
        verify(blockchainRepository).save(any(Blockchain.class));
    }

    @Test
    void initializeBlockchain_alreadyExists() {
        Blockchain existing = new Blockchain();
        existing.setName("TestChain");
        when(blockchainRepository.findByName("TestChain")).thenReturn(Optional.of(existing));

        Blockchain result = blockchainService.initializeBlockchain("TestChain",
                ConsensusType.PROOF_OF_WORK);

        assertEquals(existing, result);
        verify(blockRepository, never()).save(any());
    }

    @Test
    void getBlockchainStatus_success() {
        Blockchain blockchain = new Blockchain();
        blockchain.setId(1L);

        when(blockchainRepository.findByChainId("biochain-001"))
                .thenReturn(Optional.of(blockchain));
        when(transactionRepository.findAll()).thenReturn(new ArrayList<>());
        when(transactionRepository.findByStatus(any())).thenReturn(new ArrayList<>());
        when(blockRepository.findLatestBlockByBlockchainId(1L)).thenReturn(Optional.empty());
        when(blockRepository.findByBlockchainId(1L)).thenReturn(new ArrayList<>());

        BlockchainStatusDTO dto = mock(BlockchainStatusDTO.class);
        when(blockchainMapper.toStatusDTO(any(), anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(dto);

        BlockchainStatusDTO result = blockchainService.getBlockchainStatus("biochain-001");

        assertNotNull(result);
    }

    @Test
    void getBlockchainStatus_notFound_throws() {
        when(blockchainRepository.findByChainId("biochain-001"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> blockchainService.getBlockchainStatus("biochain-001"));
    }

    @Test
    void addBlockToChain_success() {
        Blockchain blockchain = new Blockchain();
        blockchain.setCurrentHeight(0);

        Block block = new Block();
        block.setBlockIndex(1);

        when(blockchainRepository.findByChainId("biochain-001"))
                .thenReturn(Optional.of(blockchain));

        blockchainService.addBlockToChain("biochain-001", block);

        assertEquals(1, blockchain.getCurrentHeight());
        verify(blockRepository).save(block);
        verify(blockchainRepository).save(blockchain);
    }

    @Test
    void addBlockToChain_invalidIndex_throws() {
        Blockchain blockchain = new Blockchain();
        blockchain.setCurrentHeight(5);

        Block block = new Block();
        block.setBlockIndex(10);

        when(blockchainRepository.findByChainId("biochain-001"))
                .thenReturn(Optional.of(blockchain));

        assertThrows(IllegalStateException.class,
                () -> blockchainService.addBlockToChain("biochain-001", block));
    }

    @Test
    void validateBlockchain_valid() {
        Blockchain blockchain = new Blockchain();
        blockchain.setId(1L);

        when(blockchainRepository.findByChainId("biochain-001"))
                .thenReturn(Optional.of(blockchain));
        when(blockRepository.findByBlockchainId(1L)).thenReturn(new ArrayList<>());
        when(blockService.validateBlockchain(anyList())).thenReturn(true);

        assertTrue(blockchainService.validateBlockchain("biochain-001"));
    }

    @Test
    void getBlockchainHeight_returnsHeight() {
        Blockchain blockchain = new Blockchain();
        blockchain.setCurrentHeight(100);

        when(blockchainRepository.findByChainId("biochain-001"))
                .thenReturn(Optional.of(blockchain));

        assertEquals(100, blockchainService.getBlockchainHeight("biochain-001"));
    }

    @Test
    void updateDifficulty_success() {
        Blockchain blockchain = new Blockchain();
        blockchain.setDifficulty(4);

        when(blockchainRepository.findByChainId("biochain-001"))
                .thenReturn(Optional.of(blockchain));

        blockchainService.updateDifficulty("biochain-001", 5);

        assertEquals(5, blockchain.getDifficulty());
        verify(blockchainRepository).save(blockchain);
    }

    @Test
    void getGenesisBlock_found() {
        Blockchain blockchain = new Blockchain();
        blockchain.setGenesisHash("genesisHash");

        Block genesisBlock = new Block();
        genesisBlock.setBlockIndex(0);

        when(blockchainRepository.findByChainId("biochain-001"))
                .thenReturn(Optional.of(blockchain));
        when(blockRepository.findByHash("genesisHash"))
                .thenReturn(Optional.of(genesisBlock));

        Block result = blockchainService.getGenesisBlock("biochain-001");

        assertEquals(0, result.getBlockIndex());
    }

    @Test
    void checkIntegrity_delegatesToValidate() {
        Blockchain blockchain = new Blockchain();
        blockchain.setId(1L);

        when(blockchainRepository.findByChainId("biochain-001"))
                .thenReturn(Optional.of(blockchain));
        when(blockRepository.findByBlockchainId(1L)).thenReturn(new ArrayList<>());
        when(blockService.validateBlockchain(anyList())).thenReturn(true);

        assertTrue(blockchainService.checkIntegrity("biochain-001"));
    }

    @Test
    void getTotalTransactions_calculatesTotal() {
        Blockchain blockchain = new Blockchain();
        blockchain.setId(1L);

        Block b1 = new Block();
        b1.setTransactions(new ArrayList<>(List.of(new com.wallet.biochain.entities.Transaction(),
                new com.wallet.biochain.entities.Transaction())));

        Block b2 = new Block();
        b2.setTransactions(new ArrayList<>(List.of(new com.wallet.biochain.entities.Transaction())));

        when(blockchainRepository.findByChainId("biochain-001"))
                .thenReturn(Optional.of(blockchain));
        when(blockRepository.findByBlockchainId(1L)).thenReturn(List.of(b1, b2));

        Integer total = blockchainService.getTotalTransactions("biochain-001");

        assertEquals(3, total);
    }
}
