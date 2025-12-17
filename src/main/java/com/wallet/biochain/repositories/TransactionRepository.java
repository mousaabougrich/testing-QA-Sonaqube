package com.wallet.biochain.repositories;

import com.wallet.biochain.entities.Transaction;
import com.wallet.biochain.entities.Block;
import com.wallet.biochain.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionHash(String transactionHash);

    List<Transaction> findBySenderAddress(String senderAddress);

    List<Transaction> findByRecipientAddress(String recipientAddress);

    @Query("SELECT t FROM Transaction t WHERE t.senderAddress = :address OR t.recipientAddress = :address")
    List<Transaction> findByWalletAddress(@Param("address") String address);

    @Query("SELECT t FROM Transaction t WHERE t.senderAddress = :address OR t.recipientAddress = :address ORDER BY t.timestamp DESC")
    List<Transaction> findByWalletAddressOrderByTimestampDesc(@Param("address") String address);

    List<Transaction> findByStatus(TransactionStatus status);

    List<Transaction> findByBlock(Block block);

    List<Transaction> findByBlockId(Long blockId);

    @Query("SELECT t FROM Transaction t WHERE t.status = 'PENDING' ORDER BY t.fee DESC, t.timestamp ASC")
    List<Transaction> findPendingTransactionsOrderedByFee();

    @Query("SELECT t FROM Transaction t WHERE t.block IS NULL AND t.status = 'PENDING'")
    List<Transaction> findUnconfirmedTransactions();

    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    List<Transaction> findTransactionsBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = :status")
    Long countByStatus(@Param("status") TransactionStatus status);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.status = 'CONFIRMED'")
    BigDecimal getTotalTransactionVolume();

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.senderAddress = :address OR t.recipientAddress = :address")
    Long countTransactionsByAddress(@Param("address") String address);

    boolean existsByTransactionHash(String transactionHash);
}