package com.wallet.biochain.repositories;

import com.wallet.biochain.entities.Blockchain;
import com.wallet.biochain.enums.ConsensusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface BlockchainRepository extends JpaRepository<Blockchain, Long> {

    Optional<Blockchain> findByChainId(String chainId);

    Optional<Blockchain> findByName(String name);

    List<Blockchain> findByConsensusType(ConsensusType consensusType);

    List<Blockchain> findByIsValid(Boolean isValid);

    @Query("SELECT b FROM Blockchain b WHERE b.currentHeight = (SELECT MAX(b2.currentHeight) FROM Blockchain b2)")
    Optional<Blockchain> findBlockchainWithMaxHeight();

    @Query("SELECT b FROM Blockchain b ORDER BY b.currentHeight DESC")
    List<Blockchain> findAllOrderByHeightDesc();

    boolean existsByChainId(String chainId);

    boolean existsByName(String name);
}