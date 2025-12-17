package com.wallet.biochain.controllers;

import com.wallet.biochain.dto.TransactionHistoryDTO;
import com.wallet.biochain.dto.TransactionRequestDTO;
import com.wallet.biochain.dto.TransactionResponseDTO;
import com.wallet.biochain.enums.TransactionStatus;
import com.wallet.biochain.services.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Test
    void createTransaction_success() throws Exception {
        TransactionResponseDTO dto = new TransactionResponseDTO(
                1L, "hash", "sender", "recipient", BigDecimal.TEN, BigDecimal.ONE,
                TransactionStatus.PENDING, 0, 1234567890L, LocalDateTime.now(), "memo");
        
        when(transactionService.createTransaction(any())).thenReturn(dto);

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"senderAddress\":\"sender\",\"recipientAddress\":\"recipient\",\"amount\":10.0,\"privateKey\":\"key\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionHash").value("hash"));

        verify(transactionService).createTransaction(any());
    }

    @Test
    void createTransaction_failure() throws Exception {
        when(transactionService.createTransaction(any()))
                .thenThrow(new IllegalArgumentException("Invalid"));

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"senderAddress\":\"sender\",\"recipientAddress\":\"recipient\",\"amount\":10.0,\"privateKey\":\"key\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTransactionByHash_found() throws Exception {
        TransactionResponseDTO dto = new TransactionResponseDTO(
                1L, "hash", "sender", "recipient", BigDecimal.TEN, BigDecimal.ONE,
                TransactionStatus.CONFIRMED, 1, 1234567890L, LocalDateTime.now(), "memo");
        
        when(transactionService.getTransactionByHash("hash")).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/transactions/hash"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionHash").value("hash"));
    }

    @Test
    void getTransactionByHash_notFound() throws Exception {
        when(transactionService.getTransactionByHash("hash")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/transactions/hash"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTransactionHistory_success() throws Exception {
        TransactionHistoryDTO dto = new TransactionHistoryDTO(
                "hash", "sender", "recipient", BigDecimal.TEN, BigDecimal.ONE,
                TransactionStatus.CONFIRMED, 1, "SENT", LocalDateTime.now(), 1, "memo");
        
        when(transactionService.getTransactionHistory("address")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/transactions/wallet/address/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getSentTransactions_success() throws Exception {
        TransactionResponseDTO dto = new TransactionResponseDTO(
                1L, "hash", "sender", "recipient", BigDecimal.TEN, BigDecimal.ONE,
                TransactionStatus.CONFIRMED, 1, 1234567890L, LocalDateTime.now(), "memo");
        
        when(transactionService.getSentTransactions("address")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/transactions/wallet/address/sent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getReceivedTransactions_success() throws Exception {
        TransactionResponseDTO dto = new TransactionResponseDTO(
                1L, "hash", "sender", "recipient", BigDecimal.TEN, BigDecimal.ONE,
                TransactionStatus.CONFIRMED, 1, 1234567890L, LocalDateTime.now(), "memo");
        
        when(transactionService.getReceivedTransactions("address")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/transactions/wallet/address/received"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getPendingTransactions_success() throws Exception {
        TransactionResponseDTO dto = new TransactionResponseDTO(
                1L, "hash", "sender", "recipient", BigDecimal.TEN, BigDecimal.ONE,
                TransactionStatus.PENDING, 0, 1234567890L, LocalDateTime.now(), "memo");
        
        when(transactionService.getPendingTransactions()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/transactions/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getConfirmedTransactions_success() throws Exception {
        TransactionResponseDTO dto = new TransactionResponseDTO(
                1L, "hash", "sender", "recipient", BigDecimal.TEN, BigDecimal.ONE,
                TransactionStatus.CONFIRMED, 1, 1234567890L, LocalDateTime.now(), "memo");
        
        when(transactionService.getConfirmedTransactions()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/transactions/confirmed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void confirmTransaction_success() throws Exception {
        doNothing().when(transactionService).confirmTransaction("hash", 1L);

        mockMvc.perform(post("/api/transactions/hash/confirm")
                .param("blockId", "1"))
                .andExpect(status().isOk());

        verify(transactionService).confirmTransaction("hash", 1L);
    }

    @Test
    void confirmTransaction_failure() throws Exception {
        doThrow(new IllegalArgumentException("Not found"))
                .when(transactionService).confirmTransaction("hash", 1L);

        mockMvc.perform(post("/api/transactions/hash/confirm")
                .param("blockId", "1"))
                .andExpect(status().isBadRequest());
    }
}
