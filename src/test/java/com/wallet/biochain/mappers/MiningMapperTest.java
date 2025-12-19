package com.wallet.biochain.mappers;

import com.wallet.biochain.dto.MiningResultDTO;
import com.wallet.biochain.entities.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MiningMapperTest {

    private MiningMapper miningMapper;
    private Block block;

    @BeforeEach
    void setUp() {
        miningMapper = new MiningMapper();

        block = new Block();
        block.setId(1L);
        block.setBlockIndex(1000);
        block.setHash("blockhash123");
        block.setPreviousHash("prevhash");
        block.setTimestamp(System.currentTimeMillis());
        block.setNonce(12345);
        block.setDifficulty(4);
        block.setMerkleRoot("merkleroot");
        block.setMinerAddress("mineraddr");
        block.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testToResultDTO_success() {
        Long miningDuration = 5000L;
        BigDecimal reward = new BigDecimal("50.00");
        String message = "Mining successful";

        MiningResultDTO result = miningMapper.toResultDTO(
                block,
                miningDuration,
                reward,
                true,
                message
        );

        assertNotNull(result);
        assertTrue(result.success());
        assertEquals(block.getHash(), result.blockHash());
        assertEquals(block.getBlockIndex(), result.blockIndex());
        assertEquals(miningDuration, result.miningDuration());
        assertEquals(reward, result.reward());
        assertEquals(message, result.message());
    }

    @Test
    void testToResultDTO_failure() {
        String message = "Mining failed";

        MiningResultDTO result = miningMapper.toResultDTO(
                block,
                5000L,
                null,
                false,
                message
        );

        assertNotNull(result);
        assertFalse(result.success());
        assertEquals(message, result.message());
    }

    @Test
    void testToResultDTO_withNullBlock() {
        MiningResultDTO result = miningMapper.toResultDTO(
                null,
                5000L,
                new BigDecimal("50.00"),
                true,
                "Success"
        );

        assertNotNull(result);
        assertTrue(result.success());
        assertNull(result.blockHash());
    }

    @Test
    void testToResultDTO_withNullReward() {
        MiningResultDTO result = miningMapper.toResultDTO(
                block,
                5000L,
                null,
                true,
                "Success"
        );

        assertNotNull(result);
        assertNull(result.reward());
    }
}

