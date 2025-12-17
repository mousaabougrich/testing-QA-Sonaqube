package com.wallet.biochain.services;
import com.wallet.biochain.dto.TransactionPoolStatusDTO;
import com.wallet.biochain.entities.Transaction;
import com.wallet.biochain.entities.TransactionPool;
import com.wallet.biochain.enums.TransactionStatus;
import com.wallet.biochain.mappers.TransactionPoolMapper;
import com.wallet.biochain.repositories.TransactionPoolRepository;
import com.wallet.biochain.repositories.TransactionRepository;
import com.wallet.biochain.services.impl.TransactionPoolServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionPoolServiceImplTest {

    @Mock
    private TransactionPoolRepository poolRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private TransactionPoolMapper poolMapper;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private TransactionPoolServiceImpl poolService;

    @Test
    void createPool_success() {
        when(poolRepository.existsByPoolName("main")).thenReturn(false);

        TransactionPool pool = new TransactionPool("main");
        pool.setMaxSize(10);
        pool.setIsActive(true);
        when(poolRepository.save(any(TransactionPool.class))).thenReturn(pool);

        TransactionPoolStatusDTO dto = mock(TransactionPoolStatusDTO.class);
        when(poolMapper.toStatusDTO(pool)).thenReturn(dto);

        TransactionPool result = poolService.createPool("main", 10);

        assertEquals("main", result.getPoolName());
        verify(messagingTemplate).convertAndSend(eq("/topic/pools/created"), eq(dto));
    }

    @Test
    void addTransaction_successUpdatesCounts() {
        TransactionPool pool = new TransactionPool("main");
        pool.setId(1L);
        pool.setMaxSize(5);
        pool.setCurrentSize(0);
        pool.setIsActive(true);
        pool.setPendingTransactions(new java.util.ArrayList<>());

        when(poolRepository.findByPoolName("main")).thenReturn(Optional.of(pool));

        Transaction tx = new Transaction();
        tx.setTransactionHash("h");
        tx.setStatus(TransactionStatus.PENDING);
        tx.setFee(new BigDecimal("0.1"));

        poolService.addTransaction("main", tx);

        assertEquals(1, pool.getCurrentSize());
        verify(transactionRepository).save(tx);
        verify(poolRepository).save(pool);
    }

    @Test
    void addTransaction_nullTx_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> poolService.addTransaction("main", null));
    }

    @Test
    void getTopTransactionsByFee_ordersCorrectly() {
        TransactionPool pool = new TransactionPool("main");
        pool.setId(1L);
        pool.setPendingTransactions(new java.util.ArrayList<>());
        when(poolRepository.findByPoolName("main")).thenReturn(Optional.of(pool));

        Transaction t1 = new Transaction();
        t1.setTransactionHash("1");
        t1.setFee(new BigDecimal("0.1"));
        t1.setTimestamp(1L);

        Transaction t2 = new Transaction();
        t2.setTransactionHash("2");
        t2.setFee(new BigDecimal("0.2"));
        t2.setTimestamp(2L);

        pool.getPendingTransactions().addAll(List.of(t1, t2));

        List<Transaction> top = poolService.getTopTransactionsByFee("main", 1);

        assertEquals(1, top.size());
        assertEquals("2", top.get(0).getTransactionHash());
    }

    @Test
    void isPoolFull_delegatesToEntity() {
        TransactionPool pool = new TransactionPool("main");
        pool.setMaxSize(1);
        pool.setCurrentSize(1);
        when(poolRepository.findByPoolName("main")).thenReturn(Optional.of(pool));

        assertTrue(poolService.isPoolFull("main"));
    }
}
