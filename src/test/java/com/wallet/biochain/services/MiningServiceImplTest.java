package com.wallet.biochain.services;

import com.wallet.biochain.dto.BlockDTO;
import com.wallet.biochain.dto.MiningResultDTO;
import com.wallet.biochain.entities.Block;
import com.wallet.biochain.mappers.MiningMapper;
import com.wallet.biochain.services.impl.MiningServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MiningServiceImplTest {

    @Mock
    private BlockService blockService;
    @Mock
    private CryptographyService cryptographyService;
    @Mock
    private MiningMapper miningMapper;

    @InjectMocks
    private MiningServiceImpl miningService;

    @Test
    void mineBlock_success() {
        Block block = new Block();
        block.setBlockIndex(1);
        block.setNonce(0);

        when(blockService.getBlockCount()).thenReturn(1L);
        when(blockService.createBlock(eq(1), anyString(), anyList())).thenReturn(block);
        when(blockService.calculateBlockHash(any())).thenReturn("0000abc", "0001def", "0000ghi");
        when(blockService.addBlock(any())).thenReturn(block);

        MiningResultDTO resultDTO = mock(MiningResultDTO.class);
        when(miningMapper.toResultDTO(any(), anyLong(), any())).thenReturn(resultDTO);

        MiningResultDTO result = miningService.mineBlock(new ArrayList<>(), "miner");

        assertNotNull(result);
        verify(blockService).addBlock(any());
    }

    @Test
    void mineBlockWithDifficulty_withPreviousBlock() {
        BlockDTO previousBlockDTO = mock(BlockDTO.class);
        when(previousBlockDTO.hash()).thenReturn("previousHash");

        when(blockService.getBlockCount()).thenReturn(2L);
        when(blockService.getLatestBlock()).thenReturn(Optional.of(previousBlockDTO));

        Block block = new Block();
        block.setBlockIndex(2);
        when(blockService.createBlock(eq(2), eq("previousHash"), anyList()))
                .thenReturn(block);
        when(blockService.calculateBlockHash(any())).thenReturn("0000abc");
        when(blockService.addBlock(any())).thenReturn(block);

        MiningResultDTO resultDTO = mock(MiningResultDTO.class);
        when(miningMapper.toResultDTO(any(), anyLong(), any())).thenReturn(resultDTO);

        MiningResultDTO result = miningService.mineBlockWithDifficulty(new ArrayList<>(),
                "miner", 4);

        assertNotNull(result);
        assertEquals("previousHash", block.getPreviousHash());
    }

    @Test
    void calculateProofOfWork_findsValidNonce() {
        Block block = new Block();
        block.setNonce(0);

        when(blockService.calculateBlockHash(any()))
                .thenReturn("1111abc", "0001def", "0000ghi");

        String result = miningService.calculateProofOfWork(block, 4);

        assertEquals("0000ghi", result);
        assertTrue(block.getNonce() > 0);
    }

    @Test
    void verifyProofOfWork_validHash_returnsTrue() {
        Block block = new Block();
        block.setHash("0000abc");

        when(blockService.calculateBlockHash(block)).thenReturn("0000abc");

        assertTrue(miningService.verifyProofOfWork(block, 4));
    }

    @Test
    void verifyProofOfWork_invalidHash_returnsFalse() {
        Block block = new Block();
        block.setHash("1234abc");

        when(blockService.calculateBlockHash(block)).thenReturn("1234abc");

        assertFalse(miningService.verifyProofOfWork(block, 4));
    }

    @Test
    void getCurrentDifficulty_returnsDefault() {
        assertEquals(4, miningService.getCurrentDifficulty());
    }

    @Test
    void adjustDifficulty_tooFast_increases() {
        Long avgTime = 300000L; // 5 minutes (too fast)
        Integer adjusted = miningService.adjustDifficulty(avgTime);
        assertEquals(5, adjusted);
    }

    @Test
    void adjustDifficulty_tooSlow_decreases() {
        Long avgTime = 1200000L; // 20 minutes (too slow)
        Integer adjusted = miningService.adjustDifficulty(avgTime);
        assertEquals(3, adjusted);
    }

    @Test
    void adjustDifficulty_justRight_staysSame() {
        Long avgTime = 600000L; // 10 minutes (perfect)
        Integer adjusted = miningService.adjustDifficulty(avgTime);
        assertEquals(4, adjusted);
    }

    @Test
    void calculateMiningReward_genesisBlock() {
        BigDecimal reward = miningService.calculateMiningReward(0);
        assertEquals(new BigDecimal("50"), reward);
    }

    @Test
    void calculateMiningReward_afterHalving() {
        BigDecimal reward = miningService.calculateMiningReward(210000);
        assertEquals(new BigDecimal("25"), reward);
    }

    @Test
    void calculateMiningReward_afterTwoHalvings() {
        BigDecimal reward = miningService.calculateMiningReward(420000);
        assertEquals(new BigDecimal("12.5"), reward);
    }

    @Test
    void hashMeetsDifficulty_valid() {
        assertTrue(miningService.hashMeetsDifficulty("0000abc", 4));
    }

    @Test
    void hashMeetsDifficulty_invalid() {
        assertFalse(miningService.hashMeetsDifficulty("0001abc", 4));
    }

    @Test
    void getEstimatedMiningTime_returnsEstimate() {
        Long estimate = miningService.getEstimatedMiningTime(2);
        assertNotNull(estimate);
        assertTrue(estimate > 0);
    }
}
