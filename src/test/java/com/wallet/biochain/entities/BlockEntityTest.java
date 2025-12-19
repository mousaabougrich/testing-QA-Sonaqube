package com.wallet.biochain.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class BlockEntityTest {

    private Block block;

    @BeforeEach
    void setUp() {
        block = new Block();
    }

    @Test
    void testBlockCreation() {
        block.setBlockIndex(0);
        block.setHash("hash123");
        block.setPreviousHash("prevhash");
        block.setNonce(12345);
        block.setDifficulty(4);
        block.setMerkleRoot("merkleroot");
        block.setMinerAddress("mineraddr");

        assertEquals(0, block.getBlockIndex());
        assertEquals("hash123", block.getHash());
        assertEquals("prevhash", block.getPreviousHash());
        assertEquals(12345, block.getNonce());
        assertEquals(4, block.getDifficulty());
        assertEquals("merkleroot", block.getMerkleRoot());
        assertEquals("mineraddr", block.getMinerAddress());
    }

    @Test
    void testBlockTransactions() {
        block.setTransactions(new ArrayList<>());
        Transaction tx = new Transaction();
        tx.setId(1L);
        block.getTransactions().add(tx);

        assertNotNull(block.getTransactions());
        assertEquals(1, block.getTransactions().size());
    }

    @Test
    void testBlockTimestamps() {
        LocalDateTime now = LocalDateTime.now();
        long timestamp = System.currentTimeMillis();
        block.setTimestamp(timestamp);
        block.setCreatedAt(now);

        assertEquals(now, block.getCreatedAt());
        assertEquals(timestamp, block.getTimestamp());
    }

    @Test
    void testBlockTimestampLong() {
        long timestamp = System.currentTimeMillis();
        block.setTimestamp(timestamp);
        assertEquals(timestamp, block.getTimestamp());
    }

    @Test
    void testBlockChainAssociation() {
        Blockchain blockchain = new Blockchain();
        blockchain.setId(1L);
        block.setBlockchain(blockchain);

        assertNotNull(block.getBlockchain());
        assertEquals(1L, block.getBlockchain().getId());
    }

    @Test
    void testBlockPrePersist() {
        block.setHash("hash123");
        block.setPreviousHash("prevhash");
        block.setBlockIndex(0);
        block.onCreate();

        assertNotNull(block.getCreatedAt());
    }
}

