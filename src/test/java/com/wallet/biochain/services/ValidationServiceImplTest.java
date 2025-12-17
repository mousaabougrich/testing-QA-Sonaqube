package com.wallet.biochain.services;

import com.wallet.biochain.entities.Block;
import com.wallet.biochain.entities.Transaction;
import com.wallet.biochain.entities.Wallet;
import com.wallet.biochain.enums.TransactionStatus;
import com.wallet.biochain.repositories.TransactionRepository;
import com.wallet.biochain.repositories.WalletRepository;
import com.wallet.biochain.services.CryptographyService;
import com.wallet.biochain.services.impl.ValidationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidationServiceImplTest {

    @Mock
    private CryptographyService cryptographyService;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ValidationServiceImpl validationService;

    private Transaction transaction;
    private Wallet senderWallet;

    @BeforeEach
    void setUp() {
        transaction = new Transaction();
        transaction.setTransactionHash("hash");
        transaction.setSenderAddress("0x" + "a".repeat(40));
        transaction.setRecipientAddress("0x" + "b".repeat(40));
        transaction.setAmount(new BigDecimal("10.0"));
        transaction.setFee(new BigDecimal("0.1"));
        transaction.setTimestamp(System.currentTimeMillis());
        transaction.setSignature("signature");

        senderWallet = new Wallet();
        senderWallet.setAddress(transaction.getSenderAddress());
        senderWallet.setPublicKey("publicKey");
        senderWallet.setBalance(new BigDecimal("100.0"));
    }

    @Test
    void validateTransaction_valid() {
        when(walletRepository.findByAddress(transaction.getSenderAddress()))
                .thenReturn(Optional.of(senderWallet));
        when(cryptographyService.verifySignature(anyString(), anyString(), anyString()))
                .thenReturn(true);
        when(transactionRepository.existsByTransactionHash(anyString())).thenReturn(false);
        when(transactionRepository.findBySenderAddress(anyString()))
                .thenReturn(new ArrayList<>());

        assertTrue(validationService.validateTransaction(transaction));
    }

    @Test
    void validateTransaction_null_returnsFalse() {
        assertFalse(validationService.validateTransaction(null));
    }

    @Test
    void validateTransactionFormat_valid() {
        assertTrue(validationService.validateTransactionFormat(transaction));
    }

    @Test
    void validateTransactionFormat_invalidSenderAddress() {
        transaction.setSenderAddress("invalid");
        assertFalse(validationService.validateTransactionFormat(transaction));
    }

    @Test
    void validateTransactionFormat_sameAddresses() {
        transaction.setRecipientAddress(transaction.getSenderAddress());
        assertFalse(validationService.validateTransactionFormat(transaction));
    }

    @Test
    void validateTransactionFormat_amountTooSmall() {
        transaction.setAmount(new BigDecimal("0.000000001"));
        assertFalse(validationService.validateTransactionFormat(transaction));
    }

    @Test
    void validateTransactionFormat_negativeFee() {
        transaction.setFee(new BigDecimal("-0.1"));
        assertFalse(validationService.validateTransactionFormat(transaction));
    }

    @Test
    void validateSignature_valid() {
        when(walletRepository.findByAddress(transaction.getSenderAddress()))
                .thenReturn(Optional.of(senderWallet));
        when(cryptographyService.verifySignature(anyString(), eq("signature"), eq("publicKey")))
                .thenReturn(true);

        assertTrue(validationService.validateSignature(transaction));
    }

    @Test
    void validateSignature_walletNotFound() {
        when(walletRepository.findByAddress(transaction.getSenderAddress()))
                .thenReturn(Optional.empty());

        assertFalse(validationService.validateSignature(transaction));
    }

    @Test
    void validateSignature_missingSignature() {
        transaction.setSignature(null);
        assertFalse(validationService.validateSignature(transaction));
    }

    @Test
    void validateBalance_sufficient() {
        when(walletRepository.findByAddress(transaction.getSenderAddress()))
                .thenReturn(Optional.of(senderWallet));

        assertTrue(validationService.validateBalance(transaction.getSenderAddress(),
                new BigDecimal("50.0")));
    }

    @Test
    void validateBalance_insufficient() {
        when(walletRepository.findByAddress(transaction.getSenderAddress()))
                .thenReturn(Optional.of(senderWallet));

        assertFalse(validationService.validateBalance(transaction.getSenderAddress(),
                new BigDecimal("200.0")));
    }

    @Test
    void validateBalance_walletNotFound() {
        when(walletRepository.findByAddress("addr")).thenReturn(Optional.empty());
        assertFalse(validationService.validateBalance("addr", BigDecimal.TEN));
    }

    @Test
    void validateAddressFormat_valid() {
        assertTrue(validationService.validateAddressFormat("0x" + "a".repeat(40)));
    }

    @Test
    void validateAddressFormat_invalid() {
        assertFalse(validationService.validateAddressFormat("invalid"));
        assertFalse(validationService.validateAddressFormat("0x123"));
        assertFalse(validationService.validateAddressFormat(null));
    }

    @Test
    void checkDoubleSpending_duplicateHash() {
        when(transactionRepository.existsByTransactionHash("hash")).thenReturn(true);
        assertTrue(validationService.checkDoubleSpending(transaction));
    }

    @Test
    void checkDoubleSpending_exceedsPendingBalance() {
        Transaction pending = new Transaction();
        pending.setAmount(new BigDecimal("50.0"));
        pending.setFee(new BigDecimal("0.5"));
        pending.setStatus(TransactionStatus.PENDING);

        when(transactionRepository.existsByTransactionHash(anyString())).thenReturn(false);
        when(transactionRepository.findBySenderAddress(transaction.getSenderAddress()))
                .thenReturn(List.of(pending));
        when(walletRepository.findByAddress(transaction.getSenderAddress()))
                .thenReturn(Optional.of(senderWallet));

        transaction.setAmount(new BigDecimal("60.0"));

        assertTrue(validationService.checkDoubleSpending(transaction));
    }

    @Test
    void checkDoubleSpending_noDuplicates() {
        when(transactionRepository.existsByTransactionHash(anyString())).thenReturn(false);
        when(transactionRepository.findBySenderAddress(transaction.getSenderAddress()))
                .thenReturn(new ArrayList<>());
        when(walletRepository.findByAddress(transaction.getSenderAddress()))
                .thenReturn(Optional.of(senderWallet));

        assertFalse(validationService.checkDoubleSpending(transaction));
    }

    @Test
    void validateBlock_valid() {
        Block block = new Block();
        block.setBlockIndex(1);
        block.setHash("hash");
        block.setTransactions(new ArrayList<>());

        when(cryptographyService.hash(anyString())).thenReturn("hash");

        assertTrue(validationService.validateBlock(block));
    }

    @Test
    void validateBlock_null_returnsFalse() {
        assertFalse(validationService.validateBlock(null));
    }

    @Test
    void validateBlockHash_valid() {
        Block block = new Block();
        block.setBlockIndex(1);
        block.setPreviousHash("prev");
        block.setTimestamp(123L);
        block.setNonce(1);
        block.setMerkleRoot("merkle");
        block.setHash("hash");

        when(cryptographyService.hash(anyString())).thenReturn("hash");

        assertTrue(validationService.validateBlockHash(block));
    }

    @Test
    void validatePreviousHash_valid() {
        Block previous = new Block();
        previous.setBlockIndex(0);
        previous.setHash("prevHash");

        Block current = new Block();
        current.setBlockIndex(1);
        current.setPreviousHash("prevHash");

        assertTrue(validationService.validatePreviousHash(current, previous));
    }

    @Test
    void validatePreviousHash_mismatch() {
        Block previous = new Block();
        previous.setBlockIndex(0);
        previous.setHash("prevHash");

        Block current = new Block();
        current.setBlockIndex(1);
        current.setPreviousHash("wrongHash");

        assertFalse(validationService.validatePreviousHash(current, previous));
    }

    @Test
    void validateBlockchain_valid() {
        Block b0 = new Block();
        b0.setBlockIndex(0);
        b0.setHash("hash0");
        b0.setTransactions(new ArrayList<>());

        Block b1 = new Block();
        b1.setBlockIndex(1);
        b1.setHash("hash1");
        b1.setPreviousHash("hash0");
        b1.setTransactions(new ArrayList<>());

        when(cryptographyService.hash(anyString())).thenReturn("hash0", "hash1");

        assertTrue(validationService.validateBlockchain(List.of(b0, b1)));
    }

    @Test
    void validateBlockchain_empty_returnsFalse() {
        assertFalse(validationService.validateBlockchain(new ArrayList<>()));
    }
}
