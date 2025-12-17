package com.wallet.biochain.repositories;

import com.wallet.biochain.entities.Stake;
import com.wallet.biochain.entities.User;
import com.wallet.biochain.enums.StakeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StakeRepository extends JpaRepository<Stake, Long> {

    List<Stake> findByUser(User user);

    List<Stake> findByUserId(Long userId);

    List<Stake> findByWalletAddress(String walletAddress);

    List<Stake> findByStatus(StakeStatus status);

    @Query("SELECT s FROM Stake s WHERE s.walletAddress = :address AND s.status = :status")
    List<Stake> findByWalletAddressAndStatus(
            @Param("address") String address,
            @Param("status") StakeStatus status
    );

    @Query("SELECT s FROM Stake s WHERE s.status = 'ACTIVE' OR s.status = 'LOCKED'")
    List<Stake> findActiveStakes();

    @Query("SELECT s FROM Stake s WHERE s.lockedUntil < :now AND s.status = 'LOCKED'")
    List<Stake> findExpiredLocks(@Param("now") LocalDateTime now);

    @Query("SELECT SUM(s.stakedAmount) FROM Stake s WHERE s.status = 'ACTIVE' OR s.status = 'LOCKED'")
    BigDecimal getTotalStakedAmount();

    @Query("SELECT SUM(s.stakedAmount) FROM Stake s WHERE s.walletAddress = :address AND (s.status = 'ACTIVE' OR s.status = 'LOCKED')")
    BigDecimal getTotalStakedAmountByAddress(@Param("address") String address);

    @Query("SELECT SUM(s.rewardsEarned) FROM Stake s WHERE s.walletAddress = :address")
    BigDecimal getTotalRewardsByAddress(@Param("address") String address);

    @Query("SELECT COUNT(s) FROM Stake s WHERE s.status = :status")
    Long countByStatus(@Param("status") StakeStatus status);

    @Query("SELECT s FROM Stake s WHERE s.status = 'ACTIVE' ORDER BY s.stakedAmount DESC")
    List<Stake> findTopStakers();
}