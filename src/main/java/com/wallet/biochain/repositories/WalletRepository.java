package com.wallet.biochain.repositories;

import com.wallet.biochain.entities.Wallet;
import com.wallet.biochain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByAddress(String address);

    List<Wallet> findByUser(User user);

    List<Wallet> findByUserId(Long userId);

    boolean existsByAddress(String address);

    List<Wallet> findByIsActive(Boolean isActive);

    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId AND w.isActive = true")
    List<Wallet> findActiveWalletsByUserId(@Param("userId") Long userId);

    @Query("SELECT w FROM Wallet w WHERE w.balance > :minBalance")
    List<Wallet> findWalletsWithMinBalance(@Param("minBalance") BigDecimal minBalance);

    @Query("SELECT SUM(w.balance) FROM Wallet w WHERE w.isActive = true")
    BigDecimal getTotalBalance();

    @Query("SELECT COUNT(w) FROM Wallet w WHERE w.user.id = :userId")
    Long countWalletsByUserId(@Param("userId") Long userId);
}