package com.wallet.biochain.mappers;

import com.wallet.biochain.dto.TransactionBroadcastDTO;
import com.wallet.biochain.dto.TransactionHistoryDTO;
import com.wallet.biochain.dto.TransactionResponseDTO;
import com.wallet.biochain.entities.Transaction;
import com.wallet.biochain.enums.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionMapperTest {

    private TransactionMapper transactionMapper;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transactionMapper = new TransactionMapper();

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setTransactionHash("txhash123");
        transaction.setSenderAddress("sender@example.com");
        transaction.setRecipientAddress("recipient@example.com");
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setFee(new BigDecimal("1.00"));
        transaction.setStatus(TransactionStatus.CONFIRMED);
        transaction.setConfirmationCount(6);
        transaction.setTimestamp(System.currentTimeMillis());
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setMemo("Test transaction");
    }

    @Test
    void testToResponseDTO_success() {
        TransactionResponseDTO result = transactionMapper.toResponseDTO(transaction);

        assertNotNull(result);
        assertEquals(transaction.getId(), result.id());
        assertEquals(transaction.getTransactionHash(), result.transactionHash());
        assertEquals(transaction.getSenderAddress(), result.senderAddress());
        assertEquals(transaction.getRecipientAddress(), result.recipientAddress());
        assertEquals(transaction.getAmount(), result.amount());
        assertEquals(transaction.getFee(), result.fee());
        assertEquals(transaction.getStatus(), result.status());
        assertEquals(transaction.getConfirmationCount(), result.confirmationCount());
        assertEquals(transaction.getMemo(), result.memo());
    }

    @Test
    void testToResponseDTO_withNullTransaction() {
        TransactionResponseDTO result = transactionMapper.toResponseDTO(null);

        assertNull(result);
    }

    @Test
    void testToResponseDTOList_success() {
        Transaction tx2 = new Transaction();
        tx2.setId(2L);
        tx2.setTransactionHash("txhash456");
        tx2.setSenderAddress("sender2@example.com");
        tx2.setRecipientAddress("recipient2@example.com");
        tx2.setAmount(new BigDecimal("50.00"));
        tx2.setFee(new BigDecimal("0.50"));
        tx2.setStatus(TransactionStatus.PENDING);
        tx2.setConfirmationCount(0);
        tx2.setTimestamp(System.currentTimeMillis());
        tx2.setCreatedAt(LocalDateTime.now());

        List<Transaction> transactions = Arrays.asList(transaction, tx2);
        List<TransactionResponseDTO> result = transactionMapper.toResponseDTOList(transactions);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(transaction.getId(), result.get(0).id());
        assertEquals(tx2.getId(), result.get(1).id());
    }

    @Test
    void testToResponseDTOList_withNullList() {
        List<TransactionResponseDTO> result = transactionMapper.toResponseDTOList(null);

        assertNull(result);
    }

    @Test
    void testToResponseDTOList_emptyList() {
        List<TransactionResponseDTO> result = transactionMapper.toResponseDTOList(new ArrayList<>());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testToHistoryDTO_senderPerspective() {
        String walletAddress = "sender@example.com";
        TransactionHistoryDTO result = transactionMapper.toHistoryDTO(transaction, walletAddress);

        assertNotNull(result);
        assertEquals(transaction.getTransactionHash(), result.transactionHash());
        assertEquals(transaction.getAmount(), result.amount());
        assertEquals("SENT", result.type());
    }

    @Test
    void testToHistoryDTO_recipientPerspective() {
        String walletAddress = "recipient@example.com";
        TransactionHistoryDTO result = transactionMapper.toHistoryDTO(transaction, walletAddress);

        assertNotNull(result);
        assertEquals(transaction.getTransactionHash(), result.transactionHash());
        assertEquals(transaction.getAmount(), result.amount());
        assertEquals("RECEIVED", result.type());
    }

    @Test
    void testToHistoryDTO_withNullTransaction() {
        TransactionHistoryDTO result = transactionMapper.toHistoryDTO(null, "wallet");

        assertNull(result);
    }

    @Test
    void testToBroadcastDTO_success() {
        TransactionBroadcastDTO result = transactionMapper.toBroadcastDTO(transaction);

        assertNotNull(result);
        assertEquals(transaction.getTransactionHash(), result.transactionHash());
        assertEquals(transaction.getSenderAddress(), result.senderAddress());
        assertEquals(transaction.getRecipientAddress(), result.recipientAddress());
        assertEquals(transaction.getAmount(), result.amount());
        assertEquals(transaction.getTimestamp(), result.timestamp());
    }

    @Test
    void testToBroadcastDTO_withNullTransaction() {
        TransactionBroadcastDTO result = transactionMapper.toBroadcastDTO(null);

        assertNull(result);
    }
}

