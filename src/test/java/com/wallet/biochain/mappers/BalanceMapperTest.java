package com.wallet.biochain.mappers;

import com.wallet.biochain.dto.BalanceDTO;
import com.wallet.biochain.entities.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BalanceMapperTest {

    private BalanceMapper balanceMapper;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        balanceMapper = new BalanceMapper();

        wallet = new Wallet();
        wallet.setId(1L);
        wallet.setAddress("0x1234567890abcdef");
        wallet.setBalance(new BigDecimal("1000.00"));
        wallet.setIsActive(true);
        wallet.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testToDTO_withWallet_success() {
        BigDecimal pendingBalance = new BigDecimal("100.00");

        BalanceDTO result = balanceMapper.toDTO(wallet, pendingBalance);

        assertNotNull(result);
        assertEquals(wallet.getAddress(), result.walletAddress());
        assertEquals(wallet.getBalance(), result.balance());
        assertEquals(pendingBalance, result.pendingBalance());
        assertEquals(new BigDecimal("1100.00"), result.totalBalance());
        assertEquals("BTC", result.currency());
    }

    @Test
    void testToDTO_withWallet_nullPendingBalance() {
        BalanceDTO result = balanceMapper.toDTO(wallet, null);

        assertNotNull(result);
        assertEquals(wallet.getAddress(), result.walletAddress());
        assertEquals(wallet.getBalance(), result.balance());
        assertEquals(BigDecimal.ZERO, result.pendingBalance());
        assertEquals(wallet.getBalance(), result.totalBalance());
    }

    @Test
    void testToDTO_withWallet_nullWallet() {
        BalanceDTO result = balanceMapper.toDTO((Wallet) null, new BigDecimal("100.00"));

        assertNull(result);
    }

    @Test
    void testToDTO_withAddress_success() {
        String address = "0x1234567890abcdef";
        BigDecimal balance = new BigDecimal("1000.00");
        BigDecimal pendingBalance = new BigDecimal("100.00");

        BalanceDTO result = balanceMapper.toDTO(address, balance, pendingBalance);

        assertNotNull(result);
        assertEquals(address, result.walletAddress());
        assertEquals(balance, result.balance());
        assertEquals(pendingBalance, result.pendingBalance());
        assertEquals(new BigDecimal("1100.00"), result.totalBalance());
    }

    @Test
    void testToDTO_withAddress_nullBalance() {
        String address = "0x1234567890abcdef";
        BigDecimal pendingBalance = new BigDecimal("100.00");

        BalanceDTO result = balanceMapper.toDTO(address, null, pendingBalance);

        assertNotNull(result);
        assertEquals(address, result.walletAddress());
        assertEquals(BigDecimal.ZERO, result.balance());
        assertEquals(pendingBalance, result.pendingBalance());
        assertEquals(pendingBalance, result.totalBalance());
    }

    @Test
    void testToDTO_withAddress_nullPendingBalance() {
        String address = "0x1234567890abcdef";
        BigDecimal balance = new BigDecimal("1000.00");

        BalanceDTO result = balanceMapper.toDTO(address, balance, null);

        assertNotNull(result);
        assertEquals(address, result.walletAddress());
        assertEquals(balance, result.balance());
        assertEquals(BigDecimal.ZERO, result.pendingBalance());
        assertEquals(balance, result.totalBalance());
    }

    @Test
    void testToDTO_withAddress_nullAddress() {
        BalanceDTO result = balanceMapper.toDTO((String) null, new BigDecimal("1000.00"), new BigDecimal("100.00"));

        assertNull(result);
    }

    @Test
    void testToDTO_withZeroBalances() {
        BalanceDTO result = balanceMapper.toDTO("0xaddress", BigDecimal.ZERO, BigDecimal.ZERO);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.balance());
        assertEquals(BigDecimal.ZERO, result.pendingBalance());
        assertEquals(BigDecimal.ZERO, result.totalBalance());
    }
}

