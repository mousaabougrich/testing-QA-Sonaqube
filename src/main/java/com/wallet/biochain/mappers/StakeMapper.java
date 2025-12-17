package com.wallet.biochain.mappers;

import com.wallet.biochain.dto.StakeDTO;
import com.wallet.biochain.entities.Stake;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StakeMapper {

    public StakeDTO toDTO(Stake stake) {
        if (stake == null) {
            return null;
        }

        return new StakeDTO(
                stake.getId(),
                stake.getWalletAddress(),
                stake.getStakedAmount(),
                stake.getRewardsEarned(),
                stake.getStatus(),
                stake.getLockedUntil(),
                stake.getStakeWeight(),
                stake.getCreatedAt(),
                stake.isLocked()
        );
    }

    public List<StakeDTO> toDTOList(List<Stake> stakes) {
        if (stakes == null) {
            return null;
        }

        return stakes.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}