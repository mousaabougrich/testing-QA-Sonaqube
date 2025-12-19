package com.wallet.biochain.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class WalletEntityTest {

    private Wallet wallet;
    private User user;

    @BeforeEach
    void setUp() {
        wallet = new Wallet();
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
    }

    @Test
    void testWalletCreation() {
        wallet.setAddress("0x1234567890abcdef");
        wallet.setPublicKey("publickey123");
        wallet.setBalance(new BigDecimal("100.50"));
        wallet.setIsActive(true);
        wallet.setUser(user);

        assertEquals("0x1234567890abcdef", wallet.getAddress());
        assertEquals("publickey123", wallet.getPublicKey());
        assertEquals(new BigDecimal("100.50"), wallet.getBalance());
        assertTrue(wallet.getIsActive());
        assertEquals(user.getId(), wallet.getUser().getId());
    }

    @Test
    void testWalletTransactions() {
        wallet.setSentTransactions(new ArrayList<>());
        wallet.setReceivedTransactions(new ArrayList<>());
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        wallet.getSentTransactions().add(transaction);

        assertNotNull(wallet.getSentTransactions());
        assertEquals(1, wallet.getSentTransactions().size());
        assertNotNull(wallet.getReceivedTransactions());
        assertEquals(0, wallet.getReceivedTransactions().size());
    }

    @Test
    void testWalletBalance() {
        wallet.setBalance(BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, wallet.getBalance());

        BigDecimal newBalance = new BigDecimal("500.75");
        wallet.setBalance(newBalance);
        assertEquals(newBalance, wallet.getBalance());
    }

    @Test
    void testWalletActive() {
        wallet.setIsActive(true);
        assertTrue(wallet.getIsActive());

        wallet.setIsActive(false);
        assertFalse(wallet.getIsActive());
    }

    @Test
    void testWalletTimestamps() {
        LocalDateTime now = LocalDateTime.now();
        wallet.setCreatedAt(now);
        wallet.setUpdatedAt(now);

        assertEquals(now, wallet.getCreatedAt());
        assertEquals(now, wallet.getUpdatedAt());
    }

    @Test
    void testWalletPrePersist() {
        wallet.setAddress("0x1234567890abcdef");
        wallet.setPublicKey("publickey123");
        wallet.setBalance(new BigDecimal("100.50"));

        assertNotNull(wallet.getAddress());
        assertNotNull(wallet.getPublicKey());
        assertTrue(wallet.getIsActive());
    }

    @Test
    void testWalletPreUpdate() {
        wallet.setCreatedAt(LocalDateTime.now().minusDays(1));
        LocalDateTime originalUpdated = LocalDateTime.now().minusHours(1);
        wallet.setUpdatedAt(originalUpdated);

        assertNotNull(wallet.getUpdatedAt());
    }
}

