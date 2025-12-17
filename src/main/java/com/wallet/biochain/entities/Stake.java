package com.wallet.biochain.entities;

import com.wallet.biochain.enums.StakeStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stakes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stake {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "wallet_address", nullable = false)
    private String walletAddress;

    @Column(name = "staked_amount", nullable = false, precision = 20, scale = 8)
    private BigDecimal stakedAmount;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StakeStatus status;

    @Column(name = "rewards_earned", precision = 20, scale = 8)
    private BigDecimal rewardsEarned;

    @Column(name = "stake_weight")
    private Double stakeWeight;

    @Column(name = "last_reward_time")
    private LocalDateTime lastRewardTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = StakeStatus.ACTIVE;
        }
        if (rewardsEarned == null) {
            rewardsEarned = BigDecimal.ZERO;
        }
        if (stakeWeight == null) {
            stakeWeight = 1.0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Stake(User user, String walletAddress, BigDecimal stakedAmount, LocalDateTime lockedUntil) {
        this.user = user;
        this.walletAddress = walletAddress;
        this.stakedAmount = stakedAmount;
        this.lockedUntil = lockedUntil;
        this.status = StakeStatus.ACTIVE;
        this.rewardsEarned = BigDecimal.ZERO;
        this.stakeWeight = 1.0;
    }

    public boolean isLocked() {
        if (lockedUntil == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(lockedUntil);
    }
}