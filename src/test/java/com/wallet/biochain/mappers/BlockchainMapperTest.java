package com.wallet.biochain.mappers;

import com.wallet.biochain.dto.BlockchainStatusDTO;
import com.wallet.biochain.entities.Block;
import com.wallet.biochain.entities.Blockchain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BlockchainMapperTest {

    private BlockchainMapper blockchainMapper;
    private Blockchain blockchain;
    private Block latestBlock;

    @BeforeEach
    void setUp() {
        blockchainMapper = new BlockchainMapper();

        blockchain = new Blockchain();
        blockchain.setId(1L);
        blockchain.setChainId("BTC");
        blockchain.setName("Bitcoin");
        blockchain.setCurrentHeight(750000);
        blockchain.setDifficulty(350000);
        blockchain.setBlockReward(new BigDecimal("6.25"));
        blockchain.setCreatedAt(LocalDateTime.now().minusYears(10));
        blockchain.setIsValid(true);

        latestBlock = new Block();
        latestBlock.setId(1L);
        latestBlock.setHash("latesblockhash");
        latestBlock.setBlockIndex(750000);
        latestBlock.setTimestamp(System.currentTimeMillis());
    }

    @Test
    void testToStatusDTO_success() {
        BlockchainStatusDTO result = blockchainMapper.toStatusDTO(
                blockchain,
                10000,
                100,
                600,
                latestBlock
        );

        assertNotNull(result);
        assertEquals(blockchain.getChainId(), result.chainId());
        assertEquals(blockchain.getName(), result.name());
        assertEquals(blockchain.getCurrentHeight(), result.currentHeight());
        assertEquals(10000, result.totalTransactions());
        assertEquals(100, result.pendingTransactions());
        assertEquals(600, result.averageBlockTime());
        assertNotNull(result.latestBlockHash());
    }

    @Test
    void testToStatusDTO_withNullBlockchain() {
        BlockchainStatusDTO result = blockchainMapper.toStatusDTO(
                null,
                10000,
                100,
                600,
                latestBlock
        );

        assertNull(result);
    }

    @Test
    void testToStatusDTO_withNullLatestBlock() {
        BlockchainStatusDTO result = blockchainMapper.toStatusDTO(
                blockchain,
                10000,
                100,
                600,
                null
        );

        assertNotNull(result);
        assertNull(result.latestBlockHash());
    }

    @Test
    void testToStatusDTO_withZeroValues() {
        BlockchainStatusDTO result = blockchainMapper.toStatusDTO(
                blockchain,
                0,
                0,
                0,
                latestBlock
        );

        assertNotNull(result);
        assertEquals(0, result.totalTransactions());
        assertEquals(0, result.pendingTransactions());
    }

    @Test
    void testToStatusDTO_simpleVersion() {
        BlockchainStatusDTO result = blockchainMapper.toStatusDTO(blockchain);

        assertNotNull(result);
        assertEquals(blockchain.getChainId(), result.chainId());
        assertEquals(blockchain.getName(), result.name());
    }
}

