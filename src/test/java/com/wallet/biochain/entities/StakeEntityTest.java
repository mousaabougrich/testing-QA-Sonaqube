package com.wallet.biochain.entities;

import com.wallet.biochain.enums.StakeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class StakeEntityTest {

    private Stake stake;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        stake = new Stake();
    }

    @Test
    void testStakeCreation_AllFields() {
        // Given
        String walletAddress = "0x1234567890abcdef";
        BigDecimal stakedAmount = new BigDecimal("1000.50");
        LocalDateTime lockedUntil = LocalDateTime.now().plusDays(30);

        // When
        stake.setId(1L);
        stake.setUser(user);
        stake.setWalletAddress(walletAddress);
        stake.setStakedAmount(stakedAmount);
        stake.setLockedUntil(lockedUntil);
        stake.setStatus(StakeStatus.ACTIVE);
        stake.setRewardsEarned(new BigDecimal("10.5"));
        stake.setStakeWeight(1.5);

        // Then
        assertEquals(1L, stake.getId());
        assertEquals(user, stake.getUser());
        assertEquals(walletAddress, stake.getWalletAddress());
        assertEquals(stakedAmount, stake.getStakedAmount());
        assertEquals(lockedUntil, stake.getLockedUntil());
        assertEquals(StakeStatus.ACTIVE, stake.getStatus());
        assertEquals(new BigDecimal("10.5"), stake.getRewardsEarned());
        assertEquals(1.5, stake.getStakeWeight());
    }

    @Test
    void testStakeConstructor_WithParameters() {
        // Given
        String walletAddress = "0x1234567890abcdef";
        BigDecimal stakedAmount = new BigDecimal("1000.50");
        LocalDateTime lockedUntil = LocalDateTime.now().plusDays(30);

        // When
        Stake newStake = new Stake(user, walletAddress, stakedAmount, lockedUntil);

        // Then
        assertEquals(user, newStake.getUser());
        assertEquals(walletAddress, newStake.getWalletAddress());
        assertEquals(stakedAmount, newStake.getStakedAmount());
        assertEquals(lockedUntil, newStake.getLockedUntil());
        assertEquals(StakeStatus.ACTIVE, newStake.getStatus());
        assertEquals(BigDecimal.ZERO, newStake.getRewardsEarned());
        assertEquals(1.0, newStake.getStakeWeight());
    }

    @Test
    void testIsLocked_WithFutureDate_ReturnsTrue() {
        // Given
        stake.setLockedUntil(LocalDateTime.now().plusDays(10));

        // When
        boolean isLocked = stake.isLocked();

        // Then
        assertTrue(isLocked);
    }

    @Test
    void testIsLocked_WithPastDate_ReturnsFalse() {
        // Given
        stake.setLockedUntil(LocalDateTime.now().minusDays(10));

        // When
        boolean isLocked = stake.isLocked();

        // Then
        assertFalse(isLocked);
    }

    @Test
    void testIsLocked_WithNullDate_ReturnsFalse() {
        // Given
        stake.setLockedUntil(null);

        // When
        boolean isLocked = stake.isLocked();

        // Then
        assertFalse(isLocked);
    }

    @Test
    void testPrePersist_InitializesDefaults() {
        // Given
        stake.setUser(user);
        stake.setWalletAddress("0x1234567890abcdef");
        stake.setStakedAmount(new BigDecimal("1000"));

        // When
        stake.onCreate();

        // Then
        assertNotNull(stake.getCreatedAt());
        assertNotNull(stake.getUpdatedAt());
        assertEquals(StakeStatus.ACTIVE, stake.getStatus());
        assertEquals(BigDecimal.ZERO, stake.getRewardsEarned());
        assertEquals(1.0, stake.getStakeWeight());
    }

    @Test
    void testPreUpdate_UpdatesTimestamp() throws InterruptedException {
        // Given
        stake.onCreate();
        LocalDateTime originalUpdated = stake.getUpdatedAt();
        Thread.sleep(10);

        // When
        stake.onUpdate();

        // Then
        assertNotNull(stake.getUpdatedAt());
        assertTrue(stake.getUpdatedAt().isAfter(originalUpdated) ||
                   stake.getUpdatedAt().isEqual(originalUpdated));
    }

    @Test
    void testStakeStatus_AllValues() {
        // Test all status values
        stake.setStatus(StakeStatus.ACTIVE);
        assertEquals(StakeStatus.ACTIVE, stake.getStatus());

        stake.setStatus(StakeStatus.LOCKED);
        assertEquals(StakeStatus.LOCKED, stake.getStatus());

        stake.setStatus(StakeStatus.WITHDRAWN);
        assertEquals(StakeStatus.WITHDRAWN, stake.getStatus());
    }

    @Test
    void testRewardsEarned_Calculation() {
        // Given
        stake.setRewardsEarned(new BigDecimal("10.5"));

        // When
        BigDecimal additionalReward = new BigDecimal("5.5");
        stake.setRewardsEarned(stake.getRewardsEarned().add(additionalReward));

        // Then
        assertEquals(new BigDecimal("16.0"), stake.getRewardsEarned());
    }

    @Test
    void testStakeWeight_DefaultValue() {
        // Given & When
        stake.onCreate();

        // Then
        assertEquals(1.0, stake.getStakeWeight());
    }

    @Test
    void testStakeWeight_CustomValue() {
        // Given
        stake.setStakeWeight(2.5);

        // Then
        assertEquals(2.5, stake.getStakeWeight());
    }

    @Test
    void testLastRewardTime_Update() {
        // Given
        LocalDateTime rewardTime = LocalDateTime.now();
        stake.setLastRewardTime(rewardTime);

        // Then
        assertEquals(rewardTime, stake.getLastRewardTime());
    }

    @Test
    void testStakedAmount_Precision() {
        // Given
        BigDecimal preciseAmount = new BigDecimal("1000.12345678");
        stake.setStakedAmount(preciseAmount);

        // Then
        assertEquals(preciseAmount, stake.getStakedAmount());
    }

    @Test
    void testRewardsEarned_Precision() {
        // Given
        BigDecimal preciseReward = new BigDecimal("10.12345678");
        stake.setRewardsEarned(preciseReward);

        // Then
        assertEquals(preciseReward, stake.getRewardsEarned());
    }

    @Test
    void testLockedUntil_ExactTime() {
        // Given
        LocalDateTime lockTime = LocalDateTime.of(2025, 12, 31, 23, 59, 59);
        stake.setLockedUntil(lockTime);

        // Then
        assertEquals(lockTime, stake.getLockedUntil());
    }

    @Test
    void testStakeEquality_SameId() {
        // Given
        Stake stake1 = new Stake();
        stake1.setId(1L);
        Stake stake2 = new Stake();
        stake2.setId(1L);

        // Then
        assertEquals(stake1.getId(), stake2.getId());
    }

    @Test
    void testStakeEquality_DifferentId() {
        // Given
        Stake stake1 = new Stake();
        stake1.setId(1L);
        Stake stake2 = new Stake();
        stake2.setId(2L);

        // Then
        assertNotEquals(stake1.getId(), stake2.getId());
    }

    @Test
    void testWalletAddressFormat() {
        // Given
        String[] validAddresses = {
            "0x1234567890abcdef",
            "0xABCDEF1234567890",
            "0x0000000000000000"
        };

        // When & Then
        for (String address : validAddresses) {
            stake.setWalletAddress(address);
            assertEquals(address, stake.getWalletAddress());
        }
    }

    @Test
    void testStakeAmount_ZeroValue() {
        // Given
        stake.setStakedAmount(BigDecimal.ZERO);

        // Then
        assertEquals(BigDecimal.ZERO, stake.getStakedAmount());
    }

    @Test
    void testStakeAmount_LargeValue() {
        // Given
        BigDecimal largeAmount = new BigDecimal("999999999999.99999999");
        stake.setStakedAmount(largeAmount);

        // Then
        assertEquals(largeAmount, stake.getStakedAmount());
    }

    @Test
    void testUserRelationship() {
        // Given
        stake.setUser(user);

        // Then
        assertNotNull(stake.getUser());
        assertEquals(user.getId(), stake.getUser().getId());
        assertEquals(user.getUsername(), stake.getUser().getUsername());
    }
}

