package com.wallet.biochain.mappers;

import com.wallet.biochain.dto.WalletCreateResponseDTO;
import com.wallet.biochain.dto.WalletDTO;
import com.wallet.biochain.entities.User;
import com.wallet.biochain.entities.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WalletMapperTest {

    private WalletMapper walletMapper;
    private Wallet wallet;
    private User user;

    @BeforeEach
    void setUp() {
        walletMapper = new WalletMapper();

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        wallet = new Wallet();
        wallet.setId(1L);
        wallet.setAddress("0x1234567890abcdef");
        wallet.setPublicKey("publickey123");
        wallet.setBalance(new BigDecimal("100.50"));
        wallet.setIsActive(true);
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setUser(user);
    }

    @Test
    void testToDTO_success() {
        WalletDTO result = walletMapper.toDTO(wallet);

        assertNotNull(result);
        assertEquals(wallet.getId(), result.id());
        assertEquals(wallet.getAddress(), result.address());
        assertEquals(wallet.getPublicKey(), result.publicKey());
        assertEquals(wallet.getBalance(), result.balance());
        assertEquals(wallet.getIsActive(), result.isActive());
        assertEquals(wallet.getCreatedAt(), result.createdAt());
        assertEquals(user.getUsername(), result.username());
    }

    @Test
    void testToDTO_withNullWallet() {
        WalletDTO result = walletMapper.toDTO(null);

        assertNull(result);
    }

    @Test
    void testToDTO_withNullUser() {
        wallet.setUser(null);
        WalletDTO result = walletMapper.toDTO(wallet);

        assertNotNull(result);
        assertNull(result.username());
    }

    @Test
    void testToDTOList_success() {
        Wallet wallet2 = new Wallet();
        wallet2.setId(2L);
        wallet2.setAddress("0xabcdef1234567890");
        wallet2.setPublicKey("publickey456");
        wallet2.setBalance(new BigDecimal("50.25"));
        wallet2.setIsActive(true);
        wallet2.setCreatedAt(LocalDateTime.now());
        wallet2.setUser(user);

        List<Wallet> wallets = Arrays.asList(wallet, wallet2);
        List<WalletDTO> result = walletMapper.toDTOList(wallets);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(wallet.getId(), result.get(0).id());
        assertEquals(wallet2.getId(), result.get(1).id());
    }

    @Test
    void testToDTOList_withNullList() {
        List<WalletDTO> result = walletMapper.toDTOList(null);

        assertNull(result);
    }

    @Test
    void testToDTOList_emptyList() {
        List<WalletDTO> result = walletMapper.toDTOList(new ArrayList<>());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testToCreateResponseDTO_success() {
        String privateKey = "privatekey123";

        WalletCreateResponseDTO result = walletMapper.toCreateResponseDTO(wallet, privateKey);

        assertNotNull(result);
        assertEquals(wallet.getId(), result.id());
        assertEquals(wallet.getAddress(), result.address());
        assertEquals(wallet.getPublicKey(), result.publicKey());
        assertEquals(privateKey, result.privateKey());
        assertEquals(wallet.getBalance(), result.balance());
        assertEquals(wallet.getCreatedAt(), result.createdAt());
    }

    @Test
    void testToCreateResponseDTO_withNullWallet() {
        WalletCreateResponseDTO result = walletMapper.toCreateResponseDTO(null, "key");

        assertNull(result);
    }

    @Test
    void testToCreateResponseDTO_withNullPrivateKey() {
        WalletCreateResponseDTO result = walletMapper.toCreateResponseDTO(wallet, null);

        assertNotNull(result);
        assertNull(result.privateKey());
    }
}

