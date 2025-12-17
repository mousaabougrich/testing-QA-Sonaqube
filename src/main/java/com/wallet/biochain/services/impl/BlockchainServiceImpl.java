package com.wallet.biochain.services.impl;

import com.wallet.biochain.config.BlockchainConfig;
import com.wallet.biochain.dto.BlockchainStatusDTO;
import com.wallet.biochain.entities.Block;
import com.wallet.biochain.entities.Blockchain;
import com.wallet.biochain.enums.ConsensusType;
import com.wallet.biochain.mappers.BlockchainMapper;
import com.wallet.biochain.repositories.BlockRepository;
import com.wallet.biochain.repositories.BlockchainRepository;
import com.wallet.biochain.repositories.TransactionRepository;
import com.wallet.biochain.services.BlockService;
import com.wallet.biochain.services.BlockchainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainServiceImpl implements BlockchainService {

    private final BlockchainRepository blockchainRepository;
    private final BlockRepository blockRepository;
    private final TransactionRepository transactionRepository;
    private final BlockService blockService;
    private final BlockchainMapper blockchainMapper;
    private final BlockchainConfig blockchainConfig;

    @Override
    @Transactional
    public Blockchain initializeBlockchain(String name, ConsensusType consensusType) {
        log.info("Initializing blockchain: {} with consensus: {}", name, consensusType);

        Optional<Blockchain> existing = blockchainRepository.findByName(name);
        if (existing.isPresent()) {
            log.warn("Blockchain already exists: {}", name);
            return existing.get();
        }

        Block genesisBlock = createGenesisBlock();
        Block savedGenesisBlock = blockRepository.save(genesisBlock);

        Blockchain blockchain = new Blockchain();
        blockchain.setChainId(blockchainConfig.getChainId());
        blockchain.setName(name);
        blockchain.setGenesisHash(savedGenesisBlock.getHash());
        blockchain.setCurrentHeight(0);
        blockchain.setDifficulty(blockchainConfig.getMining().getInitialDifficulty());
        blockchain.setBlockReward(blockchainConfig.getMining().getInitialReward());
        blockchain.setBlockTimeSeconds((int) (blockchainConfig.getMining().getTargetBlockTime() / 1000));
        blockchain.setConsensusType(consensusType);
        blockchain.setIsValid(true);

        Blockchain savedBlockchain = blockchainRepository.save(blockchain);
        savedGenesisBlock.setBlockchain(savedBlockchain);
        blockRepository.save(savedGenesisBlock);

        log.info("Blockchain initialized successfully");
        return savedBlockchain;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Blockchain> getBlockchainById(Long id) {
        return blockchainRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Blockchain> getBlockchainByChainId(String chainId) {
        return blockchainRepository.findByChainId(chainId);
    }

    @Override
    @Transactional(readOnly = true)
    public BlockchainStatusDTO getBlockchainStatus(String chainId) {
        Blockchain blockchain = blockchainRepository.findByChainId(chainId)
                .orElseThrow(() -> new IllegalArgumentException("Blockchain not found"));

        Integer totalTx = transactionRepository.findAll().size();
        Integer pendingTx = transactionRepository.findByStatus(
                com.wallet.biochain.enums.TransactionStatus.PENDING).size();

        Optional<Block> latestBlock = blockRepository.findLatestBlockByBlockchainId(blockchain.getId());
        Integer avgBlockTime = calculateAverageBlockTime(blockchain.getId());

        return blockchainMapper.toStatusDTO(blockchain, totalTx, pendingTx, avgBlockTime,
                latestBlock.orElse(null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Blockchain> getAllBlockchains() {
        return blockchainRepository.findAll();
    }

    @Override
    @Transactional
    public void addBlockToChain(String chainId, Block block) {
        Blockchain blockchain = blockchainRepository.findByChainId(chainId)
                .orElseThrow(() -> new IllegalArgumentException("Blockchain not found"));

        if (block.getBlockIndex() != blockchain.getCurrentHeight() + 1) {
            throw new IllegalStateException("Invalid block index");
        }

        block.setBlockchain(blockchain);
        blockRepository.save(block);

        blockchain.setCurrentHeight(block.getBlockIndex());
        blockchainRepository.save(blockchain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateBlockchain(String chainId) {
        Blockchain blockchain = blockchainRepository.findByChainId(chainId)
                .orElseThrow(() -> new IllegalArgumentException("Blockchain not found"));

        List<Block> blocks = blockRepository.findByBlockchainId(blockchain.getId());
        return blockService.validateBlockchain(blocks);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getBlockchainHeight(String chainId) {
        return blockchainRepository.findByChainId(chainId)
                .orElseThrow(() -> new IllegalArgumentException("Blockchain not found"))
                .getCurrentHeight();
    }

    @Override
    @Transactional
    public void updateDifficulty(String chainId, Integer newDifficulty) {
        Blockchain blockchain = blockchainRepository.findByChainId(chainId)
                .orElseThrow(() -> new IllegalArgumentException("Blockchain not found"));

        blockchain.setDifficulty(newDifficulty);
        blockchainRepository.save(blockchain);
    }

    @Override
    @Transactional(readOnly = true)
    public Block getGenesisBlock(String chainId) {
        Blockchain blockchain = blockchainRepository.findByChainId(chainId)
                .orElseThrow(() -> new IllegalArgumentException("Blockchain not found"));

        return blockRepository.findByHash(blockchain.getGenesisHash())
                .orElseThrow(() -> new IllegalStateException("Genesis block not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkIntegrity(String chainId) {
        return validateBlockchain(chainId);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getTotalTransactions(String chainId) {
        Blockchain blockchain = blockchainRepository.findByChainId(chainId)
                .orElseThrow(() -> new IllegalArgumentException("Blockchain not found"));

        return blockRepository.findByBlockchainId(blockchain.getId()).stream()
                .mapToInt(block -> block.getTransactions() != null ? block.getTransactions().size() : 0)
                .sum();
    }

    @Override
    @Transactional
    public void synchronizeBlockchain(String chainId) {
        log.info("Synchronization delegated to BlockchainSyncService");
    }

    private Block createGenesisBlock() {
        Block genesisBlock = new Block();
        genesisBlock.setBlockIndex(0);
        genesisBlock.setPreviousHash(blockchainConfig.getGenesis().getPreviousHash());
        genesisBlock.setTimestamp(blockchainConfig.getGenesis().getTimestamp());
        genesisBlock.setNonce(0);
        genesisBlock.setDifficulty(blockchainConfig.getMining().getInitialDifficulty());
        genesisBlock.setMinerAddress(blockchainConfig.getGenesis().getMinerAddress());
        genesisBlock.setTransactions(new ArrayList<>());

        String merkleRoot = blockService.calculateMerkleRoot(genesisBlock.getTransactions());
        genesisBlock.setMerkleRoot(merkleRoot);

        String hash = blockService.calculateBlockHash(genesisBlock);
        genesisBlock.setHash(hash);

        return genesisBlock;
    }

    private Integer calculateAverageBlockTime(Long blockchainId) {
        List<Block> blocks = blockRepository.findByBlockchainId(blockchainId).stream()
                .sorted((b1, b2) -> b2.getBlockIndex().compareTo(b1.getBlockIndex()))
                .limit(10)
                .toList();

        if (blocks.size() < 2) {
            return blockchainConfig.getMining().getTargetBlockTime().intValue() / 1000;
        }

        long totalTime = 0;
        for (int i = 0; i < blocks.size() - 1; i++) {
            totalTime += blocks.get(i).getTimestamp() - blocks.get(i + 1).getTimestamp();
        }

        return (int) (totalTime / (blocks.size() - 1) / 1000);
    }
}