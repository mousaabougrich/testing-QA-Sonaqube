package com.wallet.biochain.mappers;

import com.wallet.biochain.dto.TransactionPoolStatusDTO;
import com.wallet.biochain.entities.TransactionPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class TransactionPoolMapperTest {

    private TransactionPoolMapper transactionPoolMapper;
    private TransactionPool transactionPool;

    @BeforeEach
    void setUp() {
        transactionPoolMapper = new TransactionPoolMapper();

        transactionPool = new TransactionPool();
        transactionPool.setId(1L);
        transactionPool.setPoolName("Main Pool");
        transactionPool.setMaxSize(1000);
        transactionPool.setCurrentSize(50);
        transactionPool.setIsActive(true);
        transactionPool.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testToStatusDTO_success() {
        TransactionPoolStatusDTO result = transactionPoolMapper.toStatusDTO(transactionPool);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Main Pool", result.poolName());
        assertEquals(50, result.currentSize());
        assertEquals(1000, result.maxSize());
        assertTrue(result.isActive());
        assertFalse(result.isFull());
        assertTrue(result.fillPercentage() > 0);
    }

    @Test
    void testToStatusDTO_withNullPool() {
        TransactionPoolStatusDTO result = transactionPoolMapper.toStatusDTO(null);

        assertNull(result);
    }

    @Test
    void testToStatusDTO_emptyPool() {
        transactionPool.setCurrentSize(0);
        TransactionPoolStatusDTO result = transactionPoolMapper.toStatusDTO(transactionPool);

        assertNotNull(result);
        assertEquals(0, result.currentSize());
        assertEquals(0.0, result.fillPercentage());
    }

    @Test
    void testToStatusDTO_fullPool() {
        transactionPool.setCurrentSize(1000);
        TransactionPoolStatusDTO result = transactionPoolMapper.toStatusDTO(transactionPool);

        assertNotNull(result);
        assertEquals(1000, result.currentSize());
        assertEquals(1000, result.maxSize());
        assertTrue(result.isFull());
        assertEquals(100.0, result.fillPercentage());
    }
}

