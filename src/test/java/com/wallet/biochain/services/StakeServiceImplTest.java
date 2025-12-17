package com.wallet.biochain.services;

import com.wallet.biochain.config.BlockchainConfig;
import com.wallet.biochain.dto.StakeDTO;
import com.wallet.biochain.entities.Stake;
import com.wallet.biochain.entities.User;
import com.wallet.biochain.entities.Wallet;
import com.wallet.biochain.enums.StakeStatus;
import com.wallet.biochain.mappers.StakeMapper;
import com.wallet.biochain.repositories.StakeRepository;
import com.wallet.biochain.repositories.UserRepository;
import com.wallet.biochain.repositories.WalletRepository;
import com.wallet.biochain.services.PoSConsensusService;
import com.wallet.biochain.services.impl.StakeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class StakeServiceImplTest {

    @Mock
    private StakeRepository stakeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private PoSConsensusService posConsensusService;
    @Mock
    private StakeMapper stakeMapper;
    @Mock
    private BlockchainConfig blockchainConfig;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private StakeServiceImpl stakeService;

    private BlockchainConfig.Consensus consensusConfig;
    private User user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        consensusConfig = new BlockchainConfig.Consensus();
        consensusConfig.setMinStakeAmount(new BigDecimal("10.0"));

        when(blockchainConfig.getConsensus()).thenReturn(consensusConfig);

        user = new User();
        user.setId(1L);

        wallet = new Wallet();
        wallet.setAddress("0x" + "a".repeat(40));
        wallet.setBalance(new BigDecimal("100.0"));
        wallet.setUser(user);
    }

    @Test
    void createStake_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(walletRepository.findByAddress(wallet.getAddress()))
                .thenReturn(Optional.of(wallet));

        Stake stake = new Stake();
        stake.setId(1L);
        when(stakeRepository.save(any(Stake.class))).thenReturn(stake);

        StakeDTO dto = mock(StakeDTO.class);
        when(stakeMapper.toDTO(stake)).thenReturn(dto);

        StakeDTO result = stakeService.createStake(1L, wallet.getAddress(),
                new BigDecimal("50.0"), null);

        assertNotNull(result);
        assertEquals(new BigDecimal("50.0"), wallet.getBalance());
        verify(walletRepository).save(wallet);
        verify(stakeRepository).save(any(Stake.class));
    }

    @Test
    void createStake_belowMinimum_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> stakeService.createStake(1L, wallet.getAddress(),
                        new BigDecimal("5.0"), null));
    }

    @Test
    void createStake_userNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> stakeService.createStake(1L, wallet.getAddress(),
                        new BigDecimal("50.0"), null));
    }

    @Test
    void createStake_walletNotBelongsToUser_throws() {
        User otherUser = new User();
        otherUser.setId(2L);
        wallet.setUser(otherUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(walletRepository.findByAddress(wallet.getAddress()))
                .thenReturn(Optional.of(wallet));

        assertThrows(IllegalArgumentException.class,
                () -> stakeService.createStake(1L, wallet.getAddress(),
                        new BigDecimal("50.0"), null));
    }

    @Test
    void createStake_insufficientBalance_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(walletRepository.findByAddress(wallet.getAddress()))
                .thenReturn(Optional.of(wallet));

        assertThrows(IllegalStateException.class,
                () -> stakeService.createStake(1L, wallet.getAddress(),
                        new BigDecimal("200.0"), null));
    }

    @Test
    void createStake_withLockPeriod_setsLocked() {
        LocalDateTime future = LocalDateTime.now().plusDays(30);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(walletRepository.findByAddress(wallet.getAddress()))
                .thenReturn(Optional.of(wallet));

        Stake stake = new Stake();
        when(stakeRepository.save(any(Stake.class))).thenReturn(stake);

        StakeDTO dto = mock(StakeDTO.class);
        when(stakeMapper.toDTO(any())).thenReturn(dto);

        stakeService.createStake(1L, wallet.getAddress(), new BigDecimal("50.0"), future);

        verify(stakeRepository).save(argThat(s ->
                s.getStatus() == StakeStatus.LOCKED));
    }

    @Test
    void unlockExpiredStakes_unlocksExpired() {
        Stake stake = new Stake();
        stake.setId(1L);
        stake.setStatus(StakeStatus.LOCKED);
        stake.setLockedUntil(LocalDateTime.now().minusDays(1));

        when(stakeRepository.findExpiredLocks(any())).thenReturn(List.of(stake));

        stakeService.unlockExpiredStakes();

        assertEquals(StakeStatus.UNLOCKED, stake.getStatus());
        verify(stakeRepository).save(stake);
    }

    @Test
    void withdrawStake_success() {
        Stake stake = new Stake();
        stake.setId(1L);
        stake.setStatus(StakeStatus.ACTIVE);
        stake.setWalletAddress(wallet.getAddress());
        stake.setStakedAmount(new BigDecimal("50.0"));
        stake.setRewardsEarned(new BigDecimal("5.0"));

        when(stakeRepository.findById(1L)).thenReturn(Optional.of(stake));
        when(walletRepository.findByAddress(wallet.getAddress()))
                .thenReturn(Optional.of(wallet));

        stakeService.withdrawStake(1L);

        assertEquals(StakeStatus.WITHDRAWN, stake.getStatus());
        assertEquals(new BigDecimal("155.0"), wallet.getBalance());
        verify(walletRepository).save(wallet);
    }

    @Test
    void withdrawStake_locked_throws() {
        Stake stake = new Stake();
        stake.setId(1L);
        stake.setStatus(StakeStatus.LOCKED);
        stake.setLockedUntil(LocalDateTime.now().plusDays(10));

        when(stakeRepository.findById(1L)).thenReturn(Optional.of(stake));

        assertThrows(IllegalStateException.class,
                () -> stakeService.withdrawStake(1L));
    }

    @Test
    void withdrawStake_alreadyWithdrawn_throws() {
        Stake stake = new Stake();
        stake.setId(1L);
        stake.setStatus(StakeStatus.WITHDRAWN);

        when(stakeRepository.findById(1L)).thenReturn(Optional.of(stake));

        assertThrows(IllegalStateException.class,
                () -> stakeService.withdrawStake(1L));
    }

    @Test
    void calculateRewards_delegatesToPoS() {
        Stake stake = new Stake();
        stake.setId(1L);

        when(stakeRepository.findById(1L)).thenReturn(Optional.of(stake));
        when(posConsensusService.calculateStakingReward(stake))
                .thenReturn(new BigDecimal("2.5"));

        BigDecimal reward = stakeService.calculateRewards(1L);

        assertEquals(new BigDecimal("2.5"), reward);
    }

    @Test
    void distributeRewards_distributesToActive() {
        Stake stake = new Stake();
        stake.setId(1L);
        stake.setRewardsEarned(BigDecimal.ZERO);

        when(stakeRepository.findActiveStakes()).thenReturn(List.of(stake));
        when(posConsensusService.calculateStakingReward(stake))
                .thenReturn(new BigDecimal("1.5"));

        stakeService.distributeRewards();

        assertEquals(new BigDecimal("1.5"), stake.getRewardsEarned());
        assertNotNull(stake.getLastRewardTime());
        verify(stakeRepository).save(stake);
    }

    @Test
    void distributeRewards_noActiveStakes() {
        when(stakeRepository.findActiveStakes()).thenReturn(new ArrayList<>());

        stakeService.distributeRewards();

        verify(stakeRepository, never()).save(any());
    }

    @Test
    void getTotalStakedAmount_returnsTotal() {
        when(stakeRepository.getTotalStakedAmount())
                .thenReturn(new BigDecimal("500.0"));

        BigDecimal total = stakeService.getTotalStakedAmount();

        assertEquals(new BigDecimal("500.0"), total);
    }

    @Test
    void getTotalStakedAmount_null_returnsZero() {
        when(stakeRepository.getTotalStakedAmount()).thenReturn(null);

        BigDecimal total = stakeService.getTotalStakedAmount();

        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void getTopStakers_returnsLimited() {
        List<Stake> stakes = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            stakes.add(new Stake());
        }

        when(stakeRepository.findTopStakers()).thenReturn(stakes);
        when(stakeMapper.toDTOList(anyList())).thenReturn(new ArrayList<>());

        stakeService.getTopStakers();

        verify(stakeMapper).toDTOList(argThat(list -> list.size() == 10));
    }
}
