package com.wallet.biochain.services;

import com.wallet.biochain.config.BlockchainConfig;
import com.wallet.biochain.entities.Stake;
import com.wallet.biochain.repositories.StakeRepository;
import com.wallet.biochain.services.impl.PoSConsensusServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class PoSConsensusServiceImplTest {

    @Mock
    private StakeRepository stakeRepository;
    @Mock
    private BlockchainConfig blockchainConfig;

    @InjectMocks
    private PoSConsensusServiceImpl posService;

    private BlockchainConfig.Consensus consensusConfig;

    @BeforeEach
    void setUp() {
        // Create real nested config object
        consensusConfig = new BlockchainConfig.Consensus();
        consensusConfig.setMinStakeAmount(new BigDecimal("10.0"));
        consensusConfig.setStakingRewardRate(new BigDecimal("0.10")); // 10% annual

        // Mock the getter to return our real config object
        when(blockchainConfig.getConsensus()).thenReturn(consensusConfig);
    }

    @Test
    void hasMinimumStake_trueWhenEnough() {
        when(stakeRepository.getTotalStakedAmountByAddress("addr"))
                .thenReturn(new BigDecimal("20.0"));

        assertTrue(posService.hasMinimumStake("addr"));
    }

    @Test
    void hasMinimumStake_falseWhenNone() {
        when(stakeRepository.getTotalStakedAmountByAddress("addr")).thenReturn(null);
        assertFalse(posService.hasMinimumStake("addr"));
    }

    @Test
    void hasMinimumStake_falseWhenBelowMinimum() {
        when(stakeRepository.getTotalStakedAmountByAddress("addr"))
                .thenReturn(new BigDecimal("5.0"));
        assertFalse(posService.hasMinimumStake("addr"));
    }

    @Test
    void getEligibleValidators_filtersByMinStake() {
        Stake s1 = new Stake();
        s1.setWalletAddress("A");
        s1.setStakedAmount(new BigDecimal("5.0"));

        Stake s2 = new Stake();
        s2.setWalletAddress("B");
        s2.setStakedAmount(new BigDecimal("15.0"));

        when(stakeRepository.findActiveStakes()).thenReturn(List.of(s1, s2));

        List<String> eligible = posService.getEligibleValidators();

        assertEquals(1, eligible.size());
        assertEquals("B", eligible.get(0));
    }

    @Test
    void getEligibleValidators_returnsEmpty_whenNoActiveStakes() {
        when(stakeRepository.findActiveStakes()).thenReturn(List.of());

        List<String> eligible = posService.getEligibleValidators();

        assertTrue(eligible.isEmpty());
    }

    @Test
    void calculateValidatorProbability_zeroWhenNoStake() {
        when(stakeRepository.getTotalStakedAmountByAddress("addr")).thenReturn(null);
        assertEquals(0.0, posService.calculateValidatorProbability("addr"));
    }

    @Test
    void calculateValidatorProbability_nonZero() {
        when(stakeRepository.getTotalStakedAmountByAddress("addr"))
                .thenReturn(new BigDecimal("20.0"));
        when(stakeRepository.getTotalStakedAmount())
                .thenReturn(new BigDecimal("100.0"));

        Double prob = posService.calculateValidatorProbability("addr");
        assertEquals(20.0, prob);
    }

    @Test
    void calculateValidatorProbability_zeroWhenTotalStakeZero() {
        when(stakeRepository.getTotalStakedAmountByAddress("addr"))
                .thenReturn(new BigDecimal("20.0"));
        when(stakeRepository.getTotalStakedAmount())
                .thenReturn(BigDecimal.ZERO);

        Double prob = posService.calculateValidatorProbability("addr");
        assertEquals(0.0, prob);
    }

    @Test
    void calculateStakingReward_respectsConfigAndWeight() {
        Stake stake = new Stake();
        stake.setId(1L);
        stake.setStakedAmount(new BigDecimal("100.0"));
        stake.setStakeWeight(2.0);

        BigDecimal annualRate = new BigDecimal("0.10");
        BigDecimal dailyRate = annualRate
                .divide(new BigDecimal("365"), 8, RoundingMode.HALF_UP);
        BigDecimal expected = stake.getStakedAmount()
                .multiply(dailyRate)
                .multiply(new BigDecimal(stake.getStakeWeight()));

        BigDecimal reward = posService.calculateStakingReward(stake);

        assertEquals(expected, reward);
    }

    @Test
    void calculateStakingReward_withoutWeight() {
        Stake stake = new Stake();
        stake.setId(1L);
        stake.setStakedAmount(new BigDecimal("100.0"));
        stake.setStakeWeight(null);

        BigDecimal reward = posService.calculateStakingReward(stake);

        assertNotNull(reward);
        assertTrue(reward.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void selectValidator_throwsWhenNoEligibleValidators() {
        when(stakeRepository.findActiveStakes()).thenReturn(List.of());

        assertThrows(IllegalStateException.class,
                () -> posService.selectValidator());
    }

    @Test
    void selectValidator_throwsWhenNoStakeAvailable() {
        Stake s1 = new Stake();
        s1.setWalletAddress("A");
        s1.setStakedAmount(new BigDecimal("15.0"));

        when(stakeRepository.findActiveStakes()).thenReturn(List.of(s1));
        when(stakeRepository.getTotalStakedAmount()).thenReturn(BigDecimal.ZERO);

        assertThrows(IllegalStateException.class,
                () -> posService.selectValidator());
    }

    @Test
    void selectValidator_returnsValidator() {
        Stake s1 = new Stake();
        s1.setWalletAddress("A");
        s1.setStakedAmount(new BigDecimal("15.0"));

        when(stakeRepository.findActiveStakes()).thenReturn(List.of(s1));
        when(stakeRepository.getTotalStakedAmount()).thenReturn(new BigDecimal("15.0"));

        String validator = posService.selectValidator();

        assertNotNull(validator);
        assertEquals("A", validator);
    }

    @Test
    void getTotalStakedAmount_returnsZeroWhenNull() {
        when(stakeRepository.getTotalStakedAmount()).thenReturn(null);

        BigDecimal total = posService.getTotalStakedAmount();

        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void getTotalStakedAmount_returnsActualValue() {
        when(stakeRepository.getTotalStakedAmount())
                .thenReturn(new BigDecimal("500.0"));

        BigDecimal total = posService.getTotalStakedAmount();

        assertEquals(new BigDecimal("500.0"), total);
    }
}
