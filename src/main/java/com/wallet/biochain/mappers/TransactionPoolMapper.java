package com.wallet.biochain.mappers;

import com.wallet.biochain.dto.TransactionPoolStatusDTO;
import com.wallet.biochain.entities.TransactionPool;
import org.springframework.stereotype.Component;

@Component
public class TransactionPoolMapper {

    public TransactionPoolStatusDTO toStatusDTO(TransactionPool pool) {
        if (pool == null) {
            return null;
        }

        double fillPercentage = pool.getMaxSize() > 0
                ? (pool.getCurrentSize().doubleValue() / pool.getMaxSize().doubleValue()) * 100
                : 0.0;

        return new TransactionPoolStatusDTO(
                pool.getId(),
                pool.getPoolName(),
                pool.getCurrentSize(),
                pool.getMaxSize(),
                Math.round(fillPercentage * 100.0) / 100.0,
                pool.getIsActive(),
                pool.isFull(),
                pool.getUpdatedAt()
        );
    }
}