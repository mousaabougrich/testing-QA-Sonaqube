package com.wallet.biochain.services;

import com.wallet.biochain.dto.TransactionRequestDTO;
import com.wallet.biochain.dto.TransactionResponseDTO;
import com.wallet.biochain.entities.Block;
import com.wallet.biochain.entities.Transaction;
import com.wallet.biochain.entities.Wallet;
import com.wallet.biochain.enums.TransactionStatus;
import com.wallet.biochain.mappers.TransactionMapper;
import com.wallet.biochain.repositories.BlockRepository;
import com.wallet.biochain.repositories.TransactionRepository;
import com.wallet.biochain.repositories.WalletRepository;
import com.wallet.biochain.services.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private BlockRepository blockRepository;
    @Mock
    private CryptographyService cryptographyService;
    @Mock
    private ValidationService validationService;
    @Mock
    private P2PNetworkService p2pNetworkService;
    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Wallet sender;
    private Wallet recipient;

    @BeforeEach
    void setUp() {
        sender = new Wallet();
        sender.setAddress("sender");
        sender.setBalance(new BigDecimal("10.0"));

        recipient = new Wallet();
        recipient.setAddress("recipient");
        recipient.setBalance(new BigDecimal("1.0"));
    }

    @Test
    void createTransaction_success() {
        TransactionRequestDTO request = new TransactionRequestDTO(
                "sender", "recipient", new BigDecimal("1.0"),
                null, "memo", "privKey"
        );
        when(walletRepository.findByAddress("sender")).thenReturn(Optional.of(sender));
        when(walletRepository.findByAddress("recipient")).thenReturn(Optional.of(recipient));
        when(cryptographyService.hash(anyString())).thenReturn("txHash");
        when(cryptographyService.sign(anyString(), eq("privKey"))).thenReturn("signature");
        when(validationService.validateTransaction(any(Transaction.class))).thenReturn(true);

        Transaction saved = new Transaction();
        saved.setTransactionHash("txHash");
        saved.setStatus(TransactionStatus.PENDING);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

        TransactionResponseDTO responseDTO = mock(TransactionResponseDTO.class);
        when(transactionMapper.toResponseDTO(saved)).thenReturn(responseDTO);

        TransactionResponseDTO result = transactionService.createTransaction(request);

        assertNotNull(result);
        verify(p2pNetworkService).broadcastTransaction(saved);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createTransaction_insufficientBalance_throws() {
        sender.setBalance(new BigDecimal("0.1"));
        TransactionRequestDTO request = new TransactionRequestDTO(
                "sender", "recipient", new BigDecimal("1.0"),
                null, "memo", "privKey"
        );
        when(walletRepository.findByAddress("sender")).thenReturn(Optional.of(sender));
        when(walletRepository.findByAddress("recipient")).thenReturn(Optional.of(recipient));

        assertThrows(IllegalStateException.class,
                () -> transactionService.createTransaction(request));
    }

    @Test
    void createTransaction_missingPrivateKey_throws() {
        TransactionRequestDTO request = new TransactionRequestDTO(
                "sender", "recipient", new BigDecimal("1.0"),
                null, "memo", ""
        );
        when(walletRepository.findByAddress("sender")).thenReturn(Optional.of(sender));
        when(walletRepository.findByAddress("recipient")).thenReturn(Optional.of(recipient));

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.createTransaction(request));
    }

    @Test
    void getTransactionByHash_returnsDto() {
        Transaction tx = new Transaction();
        tx.setTransactionHash("hash");
        when(transactionRepository.findByTransactionHash("hash")).thenReturn(Optional.of(tx));
        TransactionResponseDTO dto = mock(TransactionResponseDTO.class);
        when(transactionMapper.toResponseDTO(tx)).thenReturn(dto);

        Optional<TransactionResponseDTO> result = transactionService.getTransactionByHash("hash");

        assertTrue(result.isPresent());
        assertEquals(dto, result.get());
    }

    @Test
    void confirmTransaction_updatesStatusAndBalances() {
        Transaction tx = new Transaction();
        tx.setTransactionHash("hash");
        tx.setAmount(new BigDecimal("2.0"));
        tx.setFee(new BigDecimal("0.1"));
        tx.setSenderWallet(sender);
        tx.setRecipientWallet(recipient);

        Block block = new Block();
        block.setId(1L);

        when(transactionRepository.findByTransactionHash("hash")).thenReturn(Optional.of(tx));
        when(blockRepository.findById(1L)).thenReturn(Optional.of(block));

        transactionService.confirmTransaction("hash", 1L);

        assertEquals(TransactionStatus.CONFIRMED, tx.getStatus());
        verify(walletRepository, times(2)).save(any(Wallet.class));
        verify(transactionRepository).save(tx);
    }

    @Test
    void calculateTransactionFee_respectsMin() {
        BigDecimal fee = transactionService.calculateTransactionFee(new BigDecimal("0.00001"));
        assertTrue(fee.compareTo(new BigDecimal("0.00001")) >= 0);
    }
}
