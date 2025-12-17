package com.wallet.biochain.services;

import com.wallet.biochain.dto.BalanceDTO;
import com.wallet.biochain.dto.WalletCreateRequestDTO;
import com.wallet.biochain.dto.WalletCreateResponseDTO;
import com.wallet.biochain.dto.WalletDTO;
import com.wallet.biochain.entities.Transaction;
import com.wallet.biochain.entities.User;
import com.wallet.biochain.entities.Wallet;
import com.wallet.biochain.enums.TransactionStatus;
import com.wallet.biochain.mappers.BalanceMapper;
import com.wallet.biochain.mappers.WalletMapper;
import com.wallet.biochain.repositories.TransactionRepository;
import com.wallet.biochain.repositories.UserRepository;
import com.wallet.biochain.repositories.WalletRepository;
import com.wallet.biochain.services.CryptographyService;
import com.wallet.biochain.services.impl.WalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CryptographyService cryptographyService;
    @Mock
    private WalletMapper walletMapper;
    @Mock
    private BalanceMapper balanceMapper;

    @InjectMocks
    private WalletServiceImpl walletService;

    @Mock
    private KeyPair keyPair;
    @Mock
    private PublicKey publicKey;
    @Mock
    private PrivateKey privateKey;

    private User user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        wallet = new Wallet();
        wallet.setId(1L);
        wallet.setAddress("0x" + "a".repeat(40));
        wallet.setBalance(new BigDecimal("100.0"));
        wallet.setPublicKey("publicKey");
        wallet.setEncryptedPrivateKey("encryptedKey");
        wallet.setUser(user);
        wallet.setIsActive(true);
    }

    @Test
    void createWallet_success() {
        WalletCreateRequestDTO request = new WalletCreateRequestDTO(1L, "main");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cryptographyService.generateKeyPair()).thenReturn(keyPair);
        when(keyPair.getPublic()).thenReturn(publicKey);
        when(keyPair.getPrivate()).thenReturn(privateKey);
        when(cryptographyService.encodePublicKey(publicKey)).thenReturn("pub");
        when(cryptographyService.encodePrivateKey(privateKey)).thenReturn("priv");
        when(cryptographyService.generateAddress("pub")).thenReturn("0x" + "a".repeat(40));
        when(cryptographyService.encryptPrivateKey(eq("priv"), anyString()))
                .thenReturn("encPriv");
        when(walletRepository.existsByAddress(anyString())).thenReturn(false);

        Wallet savedWallet = new Wallet();
        savedWallet.setAddress("0x" + "a".repeat(40));
        savedWallet.setPublicKey("pub");
        when(walletRepository.save(any(Wallet.class))).thenReturn(savedWallet);

        WalletCreateResponseDTO responseDTO = mock(WalletCreateResponseDTO.class);
        when(walletMapper.toCreateResponseDTO(savedWallet, "priv")).thenReturn(responseDTO);

        WalletCreateResponseDTO result = walletService.createWallet(request);

        assertNotNull(result);
        verify(walletRepository).save(any(Wallet.class));
        verify(walletMapper).toCreateResponseDTO(savedWallet, "priv");
    }

    @Test
    void createWallet_userNotFound_throws() {
        WalletCreateRequestDTO request = new WalletCreateRequestDTO(1L, "main");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> walletService.createWallet(request));
    }

    @Test
    void createWallet_addressCollision_regenerates() {
        WalletCreateRequestDTO request = new WalletCreateRequestDTO(1L, "main");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cryptographyService.generateKeyPair()).thenReturn(keyPair);
        when(keyPair.getPublic()).thenReturn(publicKey);
        when(keyPair.getPrivate()).thenReturn(privateKey);
        when(cryptographyService.encodePublicKey(publicKey)).thenReturn("pub1", "pub2");
        when(cryptographyService.encodePrivateKey(privateKey)).thenReturn("priv1", "priv2");
        when(cryptographyService.generateAddress("pub1")).thenReturn("addr1");
        when(cryptographyService.generateAddress("pub2")).thenReturn("addr2");
        when(cryptographyService.encryptPrivateKey(anyString(), anyString()))
                .thenReturn("encPriv");

        // First attempt: collision
        when(walletRepository.existsByAddress("addr1")).thenReturn(true);
        // Second attempt: success
        when(walletRepository.existsByAddress("addr2")).thenReturn(false);

        Wallet savedWallet = new Wallet();
        savedWallet.setAddress("addr2");
        when(walletRepository.save(any(Wallet.class))).thenReturn(savedWallet);

        WalletCreateResponseDTO responseDTO = mock(WalletCreateResponseDTO.class);
        when(walletMapper.toCreateResponseDTO(any(), anyString())).thenReturn(responseDTO);

        WalletCreateResponseDTO result = walletService.createWallet(request);

        assertNotNull(result);
        verify(cryptographyService, times(2)).generateKeyPair();
    }

    @Test
    void getWalletById_found() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        WalletDTO dto = mock(WalletDTO.class);
        when(walletMapper.toDTO(wallet)).thenReturn(dto);

        Optional<WalletDTO> result = walletService.getWalletById(1L);

        assertTrue(result.isPresent());
        assertEquals(dto, result.get());
    }

    @Test
    void getWalletById_notFound() {
        when(walletRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<WalletDTO> result = walletService.getWalletById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void getWalletByAddress_found() {
        when(walletRepository.findByAddress("0x" + "a".repeat(40)))
                .thenReturn(Optional.of(wallet));
        WalletDTO dto = mock(WalletDTO.class);
        when(walletMapper.toDTO(wallet)).thenReturn(dto);

        Optional<WalletDTO> result = walletService.getWalletByAddress("0x" + "a".repeat(40));

        assertTrue(result.isPresent());
    }

    @Test
    void getWalletsByUserId_returnsWallets() {
        when(walletRepository.findByUserId(1L)).thenReturn(List.of(wallet));
        WalletDTO dto = mock(WalletDTO.class);
        when(walletMapper.toDTOList(anyList())).thenReturn(List.of(dto));

        List<WalletDTO> result = walletService.getWalletsByUserId(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getActiveWalletsByUserId_returnsActiveOnly() {
        when(walletRepository.findActiveWalletsByUserId(1L)).thenReturn(List.of(wallet));
        WalletDTO dto = mock(WalletDTO.class);
        when(walletMapper.toDTOList(anyList())).thenReturn(List.of(dto));

        List<WalletDTO> result = walletService.getActiveWalletsByUserId(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getBalance_success() {
        String walletAddress = "0x" + "a".repeat(40);

        when(walletRepository.findByAddress(walletAddress))
                .thenReturn(Optional.of(wallet));

        // Mock pending transactions
        Transaction pendingSent = new Transaction();
        pendingSent.setAmount(new BigDecimal("5.0"));
        pendingSent.setFee(new BigDecimal("0.1"));
        pendingSent.setStatus(TransactionStatus.PENDING);
        pendingSent.setSenderAddress(walletAddress);

        Transaction pendingReceived = new Transaction();
        pendingReceived.setAmount(new BigDecimal("3.0"));
        pendingReceived.setFee(new BigDecimal("0.05"));
        pendingReceived.setStatus(TransactionStatus.PENDING);
        pendingReceived.setRecipientAddress(walletAddress);

        when(transactionRepository.findBySenderAddress(walletAddress))
                .thenReturn(List.of(pendingSent));
        when(transactionRepository.findByRecipientAddress(walletAddress))
                .thenReturn(List.of(pendingReceived));

        BalanceDTO dto = mock(BalanceDTO.class);
        when(balanceMapper.toDTO(eq(wallet), any(BigDecimal.class))).thenReturn(dto);

        BalanceDTO result = walletService.getBalance(walletAddress);

        assertNotNull(result);
        verify(balanceMapper).toDTO(eq(wallet), any(BigDecimal.class));
    }

    @Test
    void getBalance_walletNotFound_throws() {
        when(walletRepository.findByAddress("0x" + "a".repeat(40)))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> walletService.getBalance("0x" + "a".repeat(40)));
    }

    @Test
    void updateBalance_success() {
        when(walletRepository.findByAddress(wallet.getAddress()))
                .thenReturn(Optional.of(wallet));

        walletService.updateBalance(wallet.getAddress(), new BigDecimal("200.0"));

        assertEquals(new BigDecimal("200.0"), wallet.getBalance());
        verify(walletRepository).save(wallet);
    }

    @Test
    void updateBalance_negative_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> walletService.updateBalance("addr", new BigDecimal("-1")));
    }

    @Test
    void updateBalance_walletNotFound_throws() {
        when(walletRepository.findByAddress("addr")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> walletService.updateBalance("addr", new BigDecimal("100")));
    }

    @Test
    void addToBalance_positive_updates() {
        when(walletRepository.findByAddress(wallet.getAddress()))
                .thenReturn(Optional.of(wallet));

        walletService.addToBalance(wallet.getAddress(), new BigDecimal("50.0"));

        assertEquals(new BigDecimal("150.0"), wallet.getBalance());
        verify(walletRepository).save(wallet);
    }

    @Test
    void addToBalance_nonPositive_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> walletService.addToBalance("addr", BigDecimal.ZERO));

        assertThrows(IllegalArgumentException.class,
                () -> walletService.addToBalance("addr", new BigDecimal("-10")));
    }

    @Test
    void addToBalance_walletNotFound_throws() {
        when(walletRepository.findByAddress("addr")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> walletService.addToBalance("addr", new BigDecimal("10")));
    }

    @Test
    void subtractFromBalance_sufficient_updates() {
        when(walletRepository.findByAddress(wallet.getAddress()))
                .thenReturn(Optional.of(wallet));

        walletService.subtractFromBalance(wallet.getAddress(), new BigDecimal("30.0"));

        assertEquals(new BigDecimal("70.0"), wallet.getBalance());
        verify(walletRepository).save(wallet);
    }

    @Test
    void subtractFromBalance_insufficient_throws() {
        when(walletRepository.findByAddress(wallet.getAddress()))
                .thenReturn(Optional.of(wallet));

        assertThrows(IllegalStateException.class,
                () -> walletService.subtractFromBalance(wallet.getAddress(),
                        new BigDecimal("200.0")));
    }

    @Test
    void subtractFromBalance_nonPositive_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> walletService.subtractFromBalance("addr", BigDecimal.ZERO));
    }

    @Test
    void deactivateWallet_success() {
        when(walletRepository.findByAddress(wallet.getAddress()))
                .thenReturn(Optional.of(wallet));

        walletService.deactivateWallet(wallet.getAddress());

        assertFalse(wallet.getIsActive());
        verify(walletRepository).save(wallet);
    }

    @Test
    void activateWallet_success() {
        wallet.setIsActive(false);
        when(walletRepository.findByAddress(wallet.getAddress()))
                .thenReturn(Optional.of(wallet));

        walletService.activateWallet(wallet.getAddress());

        assertTrue(wallet.getIsActive());
        verify(walletRepository).save(wallet);
    }

    @Test
    void getTotalBalance_returnsTotal() {
        when(walletRepository.getTotalBalance()).thenReturn(new BigDecimal("1000.0"));

        BigDecimal total = walletService.getTotalBalance();

        assertEquals(new BigDecimal("1000.0"), total);
    }

    @Test
    void getTotalBalance_null_returnsZero() {
        when(walletRepository.getTotalBalance()).thenReturn(null);

        BigDecimal total = walletService.getTotalBalance();

        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void exportWallet_success() {
        when(walletRepository.findByAddress(wallet.getAddress()))
                .thenReturn(Optional.of(wallet));
        when(cryptographyService.decryptPrivateKey(eq("encryptedKey"), anyString()))
                .thenReturn("privateKey");

        String result = walletService.exportWallet(wallet.getAddress(), "password");

        assertEquals("privateKey", result);
        verify(cryptographyService).decryptPrivateKey("encryptedKey", "default-password");
    }

    @Test
    void exportWallet_walletNotFound_throws() {
        when(walletRepository.findByAddress("addr")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> walletService.exportWallet("addr", "password"));
    }

    @Test
    void importWallet_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cryptographyService.decodePrivateKey("privateKey")).thenReturn(privateKey);
        when(cryptographyService.generateKeyPair()).thenReturn(keyPair);
        when(keyPair.getPublic()).thenReturn(publicKey);
        when(cryptographyService.encodePublicKey(publicKey)).thenReturn("pub");
        when(cryptographyService.generateAddress("pub")).thenReturn("0x" + "b".repeat(40));
        when(walletRepository.existsByAddress(anyString())).thenReturn(false);
        when(cryptographyService.encryptPrivateKey(eq("privateKey"), anyString()))
                .thenReturn("encPriv");

        Wallet savedWallet = new Wallet();
        savedWallet.setAddress("0x" + "b".repeat(40));
        when(walletRepository.save(any(Wallet.class))).thenReturn(savedWallet);

        WalletDTO dto = mock(WalletDTO.class);
        when(walletMapper.toDTO(savedWallet)).thenReturn(dto);

        WalletDTO result = walletService.importWallet(1L, "privateKey", "password");

        assertNotNull(result);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void importWallet_userNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> walletService.importWallet(1L, "privateKey", "password"));
    }

    @Test
    void importWallet_alreadyExists_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cryptographyService.decodePrivateKey("privateKey")).thenReturn(privateKey);
        when(cryptographyService.generateKeyPair()).thenReturn(keyPair);
        when(keyPair.getPublic()).thenReturn(publicKey);
        when(cryptographyService.encodePublicKey(publicKey)).thenReturn("pub");
        when(cryptographyService.generateAddress("pub")).thenReturn("0x" + "b".repeat(40));
        when(walletRepository.existsByAddress(anyString())).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> walletService.importWallet(1L, "privateKey", "password"));
    }

    @Test
    void importWallet_invalidPrivateKey_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cryptographyService.decodePrivateKey("invalid"))
                .thenThrow(new RuntimeException("Invalid key"));

        assertThrows(IllegalArgumentException.class,
                () -> walletService.importWallet(1L, "invalid", "password"));
    }

    @Test
    void isValidAddress_valid() {
        assertTrue(walletService.isValidAddress("0x" + "a".repeat(40)));
        assertTrue(walletService.isValidAddress("0x" + "A".repeat(40)));
        assertTrue(walletService.isValidAddress("0x" + "1234567890abcdef".repeat(2) + "12345678"));
    }

    @Test
    void isValidAddress_invalid() {
        assertFalse(walletService.isValidAddress(null));
        assertFalse(walletService.isValidAddress(""));
        assertFalse(walletService.isValidAddress("0x123")); // Too short
        assertFalse(walletService.isValidAddress("abc" + "a".repeat(40))); // No 0x prefix
        assertFalse(walletService.isValidAddress("0x" + "g".repeat(40))); // Invalid hex
    }

    @Test
    void walletExists_true() {
        when(walletRepository.existsByAddress("addr")).thenReturn(true);
        assertTrue(walletService.walletExists("addr"));
    }

    @Test
    void walletExists_false() {
        when(walletRepository.existsByAddress("addr")).thenReturn(false);
        assertFalse(walletService.walletExists("addr"));
    }
}
