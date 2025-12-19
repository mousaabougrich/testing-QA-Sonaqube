package com.wallet.biochain.mappers;

import com.wallet.biochain.dto.StakeDTO;
import com.wallet.biochain.entities.Stake;
import com.wallet.biochain.enums.StakeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StakeMapperTest {

    private StakeMapper stakeMapper;
    private Stake stake;

    @BeforeEach
    void setUp() {
        stakeMapper = new StakeMapper();

        stake = new Stake();
        stake.setId(1L);
        stake.setWalletAddress("0x1234567890");
        stake.setStakedAmount(new BigDecimal("1000.00"));
        stake.setRewardsEarned(new BigDecimal("50.00"));
        stake.setStatus(StakeStatus.ACTIVE);
        stake.setLockedUntil(LocalDateTime.now().plusDays(30));
        stake.setStakeWeight(1500.00);
        stake.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testToDTO_success() {
        StakeDTO result = stakeMapper.toDTO(stake);

        assertNotNull(result);
        assertEquals(stake.getId(), result.id());
        assertEquals(stake.getWalletAddress(), result.walletAddress());
        assertEquals(stake.getStakedAmount(), result.stakedAmount());
        assertEquals(stake.getRewardsEarned(), result.rewardsEarned());
        assertEquals(stake.getStatus(), result.status());
        assertEquals(stake.getStakeWeight(), result.stakeWeight());
    }

    @Test
    void testToDTO_withNullStake() {
        StakeDTO result = stakeMapper.toDTO(null);

        assertNull(result);
    }

    @Test
    void testToDTO_unlocked() {
        stake.setStatus(StakeStatus.ACTIVE);

        StakeDTO result = stakeMapper.toDTO(stake);

        assertNotNull(result);
        assertEquals(StakeStatus.ACTIVE, result.status());
    }

    @Test
    void testToDTOList_success() {
        Stake stake2 = new Stake();
        stake2.setId(2L);
        stake2.setWalletAddress("0x0987654321");
        stake2.setStakedAmount(new BigDecimal("500.00"));
        stake2.setRewardsEarned(new BigDecimal("25.00"));
        stake2.setStatus(StakeStatus.ACTIVE);
        stake2.setLockedUntil(LocalDateTime.now().plusDays(30));
        stake2.setStakeWeight(750.00);
        stake2.setCreatedAt(LocalDateTime.now());

        List<Stake> stakes = Arrays.asList(stake, stake2);
        List<StakeDTO> result = stakeMapper.toDTOList(stakes);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(stake.getId(), result.get(0).id());
        assertEquals(stake2.getId(), result.get(1).id());
    }

    @Test
    void testToDTOList_withNullList() {
        List<StakeDTO> result = stakeMapper.toDTOList(null);

        assertNull(result);
    }

    @Test
    void testToDTOList_emptyList() {
        List<StakeDTO> result = stakeMapper.toDTOList(new ArrayList<>());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

