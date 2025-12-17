package com.wallet.biochain.services;

import com.wallet.biochain.entities.Block;
import com.wallet.biochain.enums.ConsensusType;
import com.wallet.biochain.services.impl.ConsensusServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsensusServiceImplTest {

    @Mock
    private PoWConsensusService powConsensusService;
    @Mock
    private PoSConsensusService posConsensusService;

    @InjectMocks
    private ConsensusServiceImpl consensusService;

    @Test
    void validateBlock_usesPoWByDefault() {
        Block block = new Block();
        block.setDifficulty(4);
        when(powConsensusService.validateProofOfWork(block, 4)).thenReturn(true);

        boolean result = consensusService.validateBlock(block);

        assertTrue(result);
        verify(powConsensusService).validateProofOfWork(block, 4);
        verify(posConsensusService, never()).validateStake(any(), anyString());
    }

    @Test
    void validateBlock_hybridRequiresBoth() {
        Block block = new Block();
        block.setDifficulty(4);
        block.setMinerAddress("addr");
        consensusService.setConsensusType(ConsensusType.HYBRID);

        when(powConsensusService.validateProofOfWork(block, 4)).thenReturn(true);
        when(posConsensusService.validateStake(block, "addr")).thenReturn(true);

        assertTrue(consensusService.validateBlock(block));
    }

    @Test
    void validateWithPoW_handlesException() {
        Block block = new Block();
        block.setDifficulty(4);
        doThrow(new RuntimeException("err"))
                .when(powConsensusService).validateProofOfWork(block, 4);

        assertFalse(consensusService.validateWithPoW(block));
    }

    @Test
    void validateWithPoS_missingMinerAddress_false() {
        Block block = new Block();
        assertFalse(consensusService.validateWithPoS(block));
        verify(posConsensusService, never()).validateStake(any(), anyString());
    }

    @Test
    void selectNextValidator_onlyForPoSOrHybrid() {
        when(posConsensusService.selectValidator()).thenReturn("addr");

        consensusService.setConsensusType(ConsensusType.PROOF_OF_STAKE);
        assertEquals("addr", consensusService.selectNextValidator());

        consensusService.setConsensusType(ConsensusType.HYBRID);
        assertEquals("addr", consensusService.selectNextValidator());

        consensusService.setConsensusType(ConsensusType.PROOF_OF_WORK);
        assertNull(consensusService.selectNextValidator());
    }

    @Test
    void isValidatorEligible_delegatesToPoS() {
        when(posConsensusService.hasMinimumStake("addr")).thenReturn(true);
        assertTrue(consensusService.isValidatorEligible("addr"));
        verify(posConsensusService).hasMinimumStake("addr");
    }
}
