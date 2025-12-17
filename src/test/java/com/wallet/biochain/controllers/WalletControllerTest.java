package com.wallet.biochain.controllers;

import com.wallet.biochain.dto.BalanceDTO;
import com.wallet.biochain.dto.WalletCreateRequestDTO;
import com.wallet.biochain.dto.WalletCreateResponseDTO;
import com.wallet.biochain.dto.WalletDTO;
import com.wallet.biochain.services.WalletService;
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
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    @Test
    void createWallet_success() throws Exception {
        WalletCreateResponseDTO dto = new WalletCreateResponseDTO(
                1L, "address", "publicKey", "privateKey", BigDecimal.TEN, LocalDateTime.now(), "Created");
        
        when(walletService.createWallet(any())).thenReturn(dto);

        mockMvc.perform(post("/api/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.address").value("address"))
                .andExpect(jsonPath("$.publicKey").value("publicKey"))
                .andExpect(jsonPath("$.privateKey").value("privateKey"));

        verify(walletService).createWallet(any());
    }

    @Test
    void getWallet_found() throws Exception {
        WalletDTO dto = new WalletDTO(
                1L, "address", "publicKey", BigDecimal.TEN, true, LocalDateTime.now(), "user1");
        
        when(walletService.getWalletByAddress("address")).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/wallets/address"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("address"))
                .andExpect(jsonPath("$.balance").value(10));
    }

    @Test
    void getWallet_notFound() throws Exception {
        when(walletService.getWalletByAddress("address")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/wallets/address"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getWalletsByUserId_success() throws Exception {
        WalletDTO dto1 = new WalletDTO(
                1L, "address1", "publicKey1", BigDecimal.TEN, true, LocalDateTime.now(), "user1");
        WalletDTO dto2 = new WalletDTO(
                2L, "address2", "publicKey2", BigDecimal.ONE, true, LocalDateTime.now(), "user1");
        
        when(walletService.getWalletsByUserId(1L)).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/wallets/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getActiveWalletsByUserId_success() throws Exception {
        WalletDTO dto = new WalletDTO(
                1L, "address1", "publicKey1", BigDecimal.TEN, true, LocalDateTime.now(), "user1");
        
        when(walletService.getActiveWalletsByUserId(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/wallets/user/1/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getBalance_success() throws Exception {
        BalanceDTO dto = new BalanceDTO("address", BigDecimal.TEN, BigDecimal.ONE, 
                BigDecimal.valueOf(11), "BIO");
        
        when(walletService.getBalance("address")).thenReturn(dto);

        mockMvc.perform(get("/api/wallets/address/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletAddress").value("address"))
                .andExpect(jsonPath("$.balance").value(10));
    }

    @Test
    void getBalance_notFound() throws Exception {
        when(walletService.getBalance("address"))
                .thenThrow(new IllegalArgumentException("Not found"));

        mockMvc.perform(get("/api/wallets/address/balance"))
                .andExpect(status().isNotFound());
    }
}
