package com.wallet.biochain.mappers;

import com.wallet.biochain.dto.BlockDTO;
import com.wallet.biochain.dto.BlockMinedEventDTO;
import com.wallet.biochain.dto.BlockValidationDTO;
import com.wallet.biochain.dto.TransactionResponseDTO;
import com.wallet.biochain.entities.Block;
import com.wallet.biochain.entities.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockMapperTest {

    @Mock
    private TransactionMapper transactionMapper;

    private BlockMapper blockMapper;
    private Block block;

    @BeforeEach
    void setUp() {
        blockMapper = new BlockMapper(transactionMapper);

        block = new Block();
        block.setId(1L);
        block.setBlockIndex(0);
        block.setHash("hash123");
        block.setPreviousHash("prevhash");
        block.setTimestamp(System.currentTimeMillis());
        block.setNonce(12345);
        block.setDifficulty(4);
        block.setMerkleRoot("merkleroot123");
        block.setMinerAddress("mineraddr123");
        block.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testToDTO_success() {
        Transaction tx = new Transaction();
        tx.setId(1L);
        block.setTransactions(Arrays.asList(tx));

        TransactionResponseDTO txDTO = new TransactionResponseDTO(1L, "txhash", "sender", "recipient",
                null, null, null, 0, System.currentTimeMillis(), LocalDateTime.now(), null);
        when(transactionMapper.toResponseDTOList(block.getTransactions()))
                .thenReturn(Arrays.asList(txDTO));

        BlockDTO result = blockMapper.toDTO(block);

        assertNotNull(result);
        assertEquals(block.getId(), result.id());
        assertEquals(block.getBlockIndex(), result.blockIndex());
        assertEquals(block.getHash(), result.hash());
        assertEquals(block.getPreviousHash(), result.previousHash());
        assertEquals(1, result.transactionCount());
        assertEquals(1, result.transactions().size());
    }

    @Test
    void testToDTO_withNullBlock() {
        BlockDTO result = blockMapper.toDTO(null);

        assertNull(result);
    }

    @Test
    void testToDTO_withNullTransactions() {
        block.setTransactions(null);
        when(transactionMapper.toResponseDTOList(null)).thenReturn(null);

        BlockDTO result = blockMapper.toDTO(block);

        assertNotNull(result);
        assertEquals(0, result.transactionCount());
    }

    @Test
    void testToDTOWithoutTransactions_success() {
        BlockDTO result = blockMapper.toDTOWithoutTransactions(block);

        assertNotNull(result);
        assertEquals(block.getId(), result.id());
        assertEquals(block.getHash(), result.hash());
        assertNull(result.transactions());
    }

    @Test
    void testToDTOWithoutTransactions_withNullBlock() {
        BlockDTO result = blockMapper.toDTOWithoutTransactions(null);

        assertNull(result);
    }

    @Test
    void testToDTOList_success() {
        Block block2 = new Block();
        block2.setId(2L);
        block2.setBlockIndex(1);
        block2.setHash("hash456");
        block2.setPreviousHash("hash123");
        block2.setTransactions(new ArrayList<>());

        when(transactionMapper.toResponseDTOList(new ArrayList<>())).thenReturn(new ArrayList<>());

        List<BlockDTO> result = blockMapper.toDTOList(Arrays.asList(block, block2));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(block.getId(), result.get(0).id());
        assertEquals(block2.getId(), result.get(1).id());
    }

    @Test
    void testToDTOList_withNullList() {
        List<BlockDTO> result = blockMapper.toDTOList(null);

        assertNull(result);
    }

    @Test
    void testToDTOList_emptyList() {
        List<BlockDTO> result = blockMapper.toDTOList(new ArrayList<>());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testToBlockMinedEventDTO_success() {
        BlockMinedEventDTO result = blockMapper.toMinedEventDTO(block, new BigDecimal("50.00"));

        assertNotNull(result);
        assertEquals(block.getBlockIndex(), result.blockIndex());
        assertEquals(block.getMinerAddress(), result.minerAddress());
    }

    @Test
    void testToBlockMinedEventDTO_withNullBlock() {
        BlockMinedEventDTO result = blockMapper.toMinedEventDTO(null, new BigDecimal("50.00"));

        assertNull(result);
    }

    @Test
    void testToBlockValidationDTO_success() {
        BlockValidationDTO result = blockMapper.toValidationDTO(true, block, null);

        assertNotNull(result);
        assertNotNull(result.blockHash());
        assertTrue(result.isValid());
    }

    @Test
    void testToBlockValidationDTO_invalid() {
        BlockValidationDTO result = blockMapper.toValidationDTO(false, block, Arrays.asList("Invalid hash"));

        assertNotNull(result);
        assertFalse(result.isValid());
    }
}

