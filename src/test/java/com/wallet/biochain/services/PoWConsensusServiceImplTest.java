package com.wallet.biochain.services;

import com.wallet.biochain.entities.Block;
import com.wallet.biochain.services.BlockService;
import com.wallet.biochain.services.impl.PoWConsensusServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PoWConsensusServiceImplTest {

    @Mock
    private BlockService blockService;

    @InjectMocks
    private PoWConsensusServiceImpl powService;

    @Test
    void mineBlock_findsValidNonce() {
        Block block = new Block();
        block.setBlockIndex(1);
        block.setNonce(0);

        when(blockService.calculateBlockHash(any()))
                .thenReturn("1111abc", "0001def", "0000ghi");

        Block result = powService.mineBlock(block, 4);

        assertNotNull(result);
        assertEquals("0000ghi", result.getHash());
        assertTrue(result.getNonce() > 0);
    }

    @Test
    void validateProofOfWork_validBlock_returnsTrue() {
        Block block = new Block();
        block.setHash("0000abc");

        when(blockService.calculateBlockHash(block)).thenReturn("0000abc");

        assertTrue(powService.validateProofOfWork(block, 4));
    }

    @Test
    void validateProofOfWork_invalidHash_returnsFalse() {
        Block block = new Block();
        block.setHash("0000abc");

        when(blockService.calculateBlockHash(block)).thenReturn("1111abc");

        assertFalse(powService.validateProofOfWork(block, 4));
    }

    @Test
    void validateProofOfWork_wrongTarget_returnsFalse() {
        Block block = new Block();
        block.setHash("0001abc");

        when(blockService.calculateBlockHash(block)).thenReturn("0001abc");

        assertFalse(powService.validateProofOfWork(block, 4));
    }

    @Test
    void calculateHash_delegatesToBlockService() {
        Block block = new Block();
        when(blockService.calculateBlockHash(block)).thenReturn("hash");

        String result = powService.calculateHash(block);

        assertEquals("hash", result);
        verify(blockService).calculateBlockHash(block);
    }

    @Test
    void meetsTarget_validHash() {
        assertTrue(powService.meetsTarget("0000abc", 4));
        assertTrue(powService.meetsTarget("00abc", 2));
    }

    @Test
    void meetsTarget_invalidHash() {
        assertFalse(powService.meetsTarget("0001abc", 4));
        assertFalse(powService.meetsTarget("1000abc", 1));
    }

    @Test
    void getDifficultyTarget_generatesCorrectTarget() {
        assertEquals("0000", powService.getDifficultyTarget(4));
        assertEquals("00", powService.getDifficultyTarget(2));
        assertEquals("", powService.getDifficultyTarget(0));
    }

    @Test
    void getDifficultyTarget_negativeDifficulty() {
        assertEquals("", powService.getDifficultyTarget(-1));
    }
}
