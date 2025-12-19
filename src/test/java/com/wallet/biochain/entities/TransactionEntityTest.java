package com.wallet.biochain.entities;

import com.wallet.biochain.enums.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransactionEntityTest {

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transaction = new Transaction();
    }

    @Test
    void testTransactionCreation() {
        transaction.setTransactionHash("txhash123");
        transaction.setSenderAddress("sender@example.com");
        transaction.setRecipientAddress("recipient@example.com");
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setFee(new BigDecimal("1.00"));
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setMemo("Test transaction");

        assertEquals("txhash123", transaction.getTransactionHash());
        assertEquals("sender@example.com", transaction.getSenderAddress());
        assertEquals("recipient@example.com", transaction.getRecipientAddress());
        assertEquals(new BigDecimal("100.00"), transaction.getAmount());
        assertEquals(new BigDecimal("1.00"), transaction.getFee());
        assertEquals(TransactionStatus.PENDING, transaction.getStatus());
        assertEquals("Test transaction", transaction.getMemo());
    }

    @Test
    void testTransactionConfirmations() {
        transaction.setConfirmationCount(0);
        assertEquals(0, transaction.getConfirmationCount());

        transaction.setConfirmationCount(6);
        assertEquals(6, transaction.getConfirmationCount());
    }

    @Test
    void testTransactionStatus() {
        transaction.setStatus(TransactionStatus.PENDING);
        assertEquals(TransactionStatus.PENDING, transaction.getStatus());

        transaction.setStatus(TransactionStatus.CONFIRMED);
        assertEquals(TransactionStatus.CONFIRMED, transaction.getStatus());

        transaction.setStatus(TransactionStatus.FAILED);
        assertEquals(TransactionStatus.FAILED, transaction.getStatus());
    }

    @Test
    void testTransactionTimestamps() {
        LocalDateTime now = LocalDateTime.now();
        long timestamp = System.currentTimeMillis();
        transaction.setTimestamp(timestamp);
        transaction.setCreatedAt(now);

        assertEquals(timestamp, transaction.getTimestamp());
        assertEquals(now, transaction.getCreatedAt());
    }

    @Test
    void testTransactionBlockAssociation() {
        Block block = new Block();
        block.setId(1L);
        transaction.setBlock(block);

        assertNotNull(transaction.getBlock());
        assertEquals(1L, transaction.getBlock().getId());
    }

    @Test
    void testTransactionPrePersist() {
        transaction.setTransactionHash("txhash123");
        transaction.setSenderAddress("sender@example.com");
        transaction.setRecipientAddress("recipient@example.com");
        transaction.setAmount(new BigDecimal("100.00"));

        assertNotNull(transaction.getTransactionHash());
        assertNotNull(transaction.getSenderAddress());
    }
}

