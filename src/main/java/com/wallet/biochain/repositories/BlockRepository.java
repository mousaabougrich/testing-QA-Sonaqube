package com.wallet.biochain.repositories;

import com.wallet.biochain.entities.Block;
import com.wallet.biochain.entities.Blockchain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {

    Optional<Block> findByHash(String hash);

    Optional<Block> findByBlockIndex(Integer blockIndex);

    List<Block> findByBlockchain(Blockchain blockchain);

    List<Block> findByBlockchainId(Long blockchainId);

    @Query("SELECT b FROM Block b ORDER BY b.blockIndex DESC")
    List<Block> findAllOrderByBlockIndexDesc();

    @Query("SELECT b FROM Block b WHERE b.blockIndex = (SELECT MAX(b2.blockIndex) FROM Block b2)")
    Optional<Block> findLatestBlock();

    @Query("SELECT b FROM Block b WHERE b.blockIndex = (SELECT MAX(b2.blockIndex) FROM Block b2 WHERE b2.blockchain.id = :blockchainId)")
    Optional<Block> findLatestBlockByBlockchainId(@Param("blockchainId") Long blockchainId);

    @Query("SELECT b FROM Block b WHERE b.minerAddress = :minerAddress")
    List<Block> findByMinerAddress(@Param("minerAddress") String minerAddress);

    @Query("SELECT b FROM Block b WHERE b.blockIndex BETWEEN :startIndex AND :endIndex")
    List<Block> findBlocksInRange(
            @Param("startIndex") Integer startIndex,
            @Param("endIndex") Integer endIndex
    );

    @Query("SELECT COUNT(b) FROM Block b WHERE b.minerAddress = :minerAddress")
    Long countBlocksByMiner(@Param("minerAddress") String minerAddress);

    @Query("SELECT MAX(b.blockIndex) FROM Block b")
    Integer findMaxBlockIndex();

    boolean existsByHash(String hash);

    boolean existsByBlockIndex(Integer blockIndex);
}