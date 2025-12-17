package com.wallet.biochain.services;

import com.wallet.biochain.dto.BlockDTO;
import com.wallet.biochain.dto.BlockValidationDTO;
import com.wallet.biochain.entities.Block;
import com.wallet.biochain.mappers.BlockMapper;
import com.wallet.biochain.repositories.BlockRepository;
import com.wallet.biochain.services.impl.BlockServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockServiceImplTest {

    @Mock
    private BlockRepository blockRepository;
    @Mock
    private CryptographyService cryptographyService;
    @Mock
    private BlockMapper blockMapper;

    @InjectMocks
    private BlockServiceImpl blockService;

    @Test
    void createBlock_setsFieldsAndHashes() {
        when(cryptographyService.hash(anyString())).thenReturn("root", "hash");

        Block block = blockService.createBlock(1, "prev", List.of());

        assertEquals(1, block.getBlockIndex());
        assertEquals("prev", block.getPreviousHash());
        assertEquals("root", block.getMerkleRoot());
        assertEquals("hash", block.getHash());
    }

    @Test
    void calculateMerkleRoot_emptyUsesEmptyHash() {
        when(cryptographyService.hash("empty")).thenReturn("h");
        String root = blockService.calculateMerkleRoot(List.of());
        assertEquals("h", root);
    }

    @Test
    void validateBlock_detectsInvalidHash() {
        Block b = new Block();
        b.setBlockIndex(0);
        b.setHash("wrong");
        b.setPreviousHash("prev");
        b.setTimestamp(System.currentTimeMillis());
        b.setMerkleRoot("m");
        b.setNonce(0);

        when(cryptographyService.hash(anyString())).thenReturn("correct");
        when(blockMapper.toValidationDTO(eq(false), eq(b), anyList()))
                .thenReturn(new BlockValidationDTO(false, "wrong", 0, List.of("Block hash mismatch"), "invalid"));

        BlockValidationDTO result = blockService.validateBlock(b);

        assertFalse(result.isValid());
    }

    @Test
    void validateBlockchain_falseWhenBroken() {
        Block b1 = new Block();
        b1.setBlockIndex(1);
        b1.setHash("h1");
        Block b2 = new Block();
        b2.setBlockIndex(2);
        b2.setPreviousHash("bad");
        b2.setHash("h2");

        when(cryptographyService.hash(anyString()))
                .thenReturn("h1", "h2");

        boolean result = blockService.validateBlockchain(List.of(b1, b2));

        assertFalse(result);
    }

    @Test
    void addBlock_saves() {
        Block b = new Block();
        when(blockRepository.save(b)).thenReturn(b);
        assertEquals(b, blockService.addBlock(b));
        verify(blockRepository).save(b);
    }

    @Test
    void getBlockByHash_mapsToDto() {
        Block b = new Block();
        when(blockRepository.findByHash("h")).thenReturn(Optional.of(b));
        BlockDTO dto = mock(BlockDTO.class);
        when(blockMapper.toDTO(b)).thenReturn(dto);

        Optional<BlockDTO> result = blockService.getBlockByHash("h");

        assertTrue(result.isPresent());
        assertEquals(dto, result.get());
    }
}

