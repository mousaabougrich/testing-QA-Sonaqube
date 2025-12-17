package com.wallet.biochain.services;

import com.wallet.biochain.entities.Block;
import com.wallet.biochain.entities.Transaction;

import java.util.List;

public interface ValidationService {

    /**
     * Validate transaction
     */
    boolean validateTransaction(Transaction transaction);

    /**
     * Validate block
     */
    boolean validateBlock(Block block);

    /**
     * Validate blockchain
     */
    boolean validateBlockchain(List<Block> blocks);

    /**
     * Validate transaction signature
     */
    boolean validateSignature(Transaction transaction);

    /**
     * Validate wallet balance for transaction
     */
    boolean validateBalance(String walletAddress, java.math.BigDecimal amount);

    /**
     * Validate block hash
     */
    boolean validateBlockHash(Block block);

    /**
     * Validate previous hash link
     */
    boolean validatePreviousHash(Block currentBlock, Block previousBlock);

    /**
     * Validate transaction format
     */
    boolean validateTransactionFormat(Transaction transaction);

    /**
     * Validate wallet address format
     */
    boolean validateAddressFormat(String address);

    /**
     * Check for double spending
     */
    boolean checkDoubleSpending(Transaction transaction);
}
