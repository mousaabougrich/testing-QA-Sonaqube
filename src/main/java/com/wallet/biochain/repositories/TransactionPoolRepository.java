package com.wallet.biochain.repositories;

import com.wallet.biochain.entities.TransactionPool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface TransactionPoolRepository extends JpaRepository<TransactionPool, Long> {

    Optional<TransactionPool> findByPoolName(String poolName);

    List<TransactionPool> findByIsActive(Boolean isActive);

    @Query("SELECT tp FROM TransactionPool tp WHERE tp.currentSize < tp.maxSize AND tp.isActive = true")
    List<TransactionPool> findAvailablePools();

    @Query("SELECT tp FROM TransactionPool tp WHERE tp.currentSize >= tp.maxSize")
    List<TransactionPool> findFullPools();

    @Query("SELECT tp FROM TransactionPool tp ORDER BY tp.currentSize DESC")
    List<TransactionPool> findAllOrderBySize();

    @Query("SELECT COUNT(tp) FROM TransactionPool tp WHERE tp.isActive = true")
    Long countActivePools();

    boolean existsByPoolName(String poolName);
}