package com.wallet.biochain.services.impl;

import com.wallet.biochain.entities.Block;
import com.wallet.biochain.entities.Transaction;
import com.wallet.biochain.enums.TransactionStatus;
import com.wallet.biochain.repositories.TransactionRepository;
import com.wallet.biochain.repositories.WalletRepository;
import com.wallet.biochain.services.CryptographyService;
import com.wallet.biochain.services.ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {

    private final CryptographyService cryptographyService;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    private static final Pattern ADDRESS_PATTERN = Pattern.compile("^0x[a-fA-F0-9]{40}$");
    private static final BigDecimal MIN_TRANSACTION_AMOUNT = new BigDecimal("0.00000001");

    @Override
    public boolean validateTransaction(Transaction transaction) {
        if (transaction == null) {
            log.warn("Transaction is null");
            return false;
        }

        // Validate format
        if (!validateTransactionFormat(transaction)) {
            log.warn("Transaction format validation failed for tx: {}", transaction.getTransactionHash());
            return false;
        }

        // Validate signature
        if (!validateSignature(transaction)) {
            log.warn("Transaction signature validation failed for tx: {}", transaction.getTransactionHash());
            return false;
        }

        // Validate balance
        if (!validateBalance(transaction.getSenderAddress(), transaction.getAmount().add(transaction.getFee()))) {
            log.warn("Insufficient balance for transaction: {}", transaction.getTransactionHash());
            return false;
        }

        // Check double spending
        if (checkDoubleSpending(transaction)) {
            log.warn("Double spending detected for transaction: {}", transaction.getTransactionHash());
            return false;
        }

        log.info("Transaction validated successfully: {}", transaction.getTransactionHash());
        return true;
    }

    @Override
    public boolean validateBlock(Block block) {
        if (block == null) {
            log.warn("Block is null");
            return false;
        }

        // Validate block hash
        if (!validateBlockHash(block)) {
            log.warn("Block hash validation failed for block: {}", block.getBlockIndex());
            return false;
        }

        // Validate all transactions in the block
        if (block.getTransactions() != null) {
            for (Transaction tx : block.getTransactions()) {
                if (!validateTransaction(tx)) {
                    log.warn("Invalid transaction in block: {}", block.getBlockIndex());
                    return false;
                }
            }
        }

        log.info("Block validated successfully: {}", block.getBlockIndex());
        return true;
    }

    @Override
    public boolean validateBlockchain(List<Block> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            log.warn("Blockchain is empty or null");
            return false;
        }

        // Validate genesis block
        Block genesisBlock = blocks.get(0);
        if (genesisBlock.getBlockIndex() != 0) {
            log.warn("Invalid genesis block index");
            return false;
        }
        // Also validate the genesis block's intrinsic validity (hash/transactions)
        if (!validateBlock(genesisBlock)) {
            log.warn("Genesis block validation failed");
            return false;
        }

        // Validate each block and its link to previous block
        for (int i = 1; i < blocks.size(); i++) {
            Block currentBlock = blocks.get(i);
            Block previousBlock = blocks.get(i - 1);

            // Validate current block
            if (!validateBlock(currentBlock)) {
                log.warn("Invalid block at index: {}", i);
                return false;
            }

            // Validate link to previous block
            if (!validatePreviousHash(currentBlock, previousBlock)) {
                log.warn("Invalid previous hash link at block: {}", i);
                return false;
            }
        }

        log.info("Blockchain validated successfully with {} blocks", blocks.size());
        return true;
    }

    @Override
    public boolean validateSignature(Transaction transaction) {
        if (transaction.getSignature() == null || transaction.getSignature().isEmpty()) {
            log.warn("Transaction signature is missing");
            return false;
        }

        try {
            // Get sender's public key from wallet
            var wallet = walletRepository.findByAddress(transaction.getSenderAddress());
            if (wallet.isEmpty()) {
                log.warn("Sender wallet not found: {}", transaction.getSenderAddress());
                return false;
            }

            String publicKey = wallet.get().getPublicKey();
            String dataToVerify = buildTransactionData(transaction);

            boolean isValid = cryptographyService.verifySignature(dataToVerify, transaction.getSignature(), publicKey);

            if (!isValid) {
                log.warn("Signature verification failed for transaction: {}", transaction.getTransactionHash());
            }

            return isValid;
        } catch (Exception e) {
            log.error("Error validating signature for transaction: {}", transaction.getTransactionHash(), e);
            return false;
        }
    }

    @Override
    public boolean validateBalance(String walletAddress, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid amount: {}", amount);
            return false;
        }

        var wallet = walletRepository.findByAddress(walletAddress);
        if (wallet.isEmpty()) {
            log.warn("Wallet not found: {}", walletAddress);
            return false;
        }

        BigDecimal currentBalance = wallet.get().getBalance();
        boolean hasSufficientBalance = currentBalance.compareTo(amount) >= 0;

        if (!hasSufficientBalance) {
            log.warn("Insufficient balance. Required: {}, Available: {}", amount, currentBalance);
        }

        return hasSufficientBalance;
    }

    @Override
    public boolean validateBlockHash(Block block) {
        if (block.getHash() == null || block.getHash().isEmpty()) {
            log.warn("Block hash is missing");
            return false;
        }

        try {
            String calculatedHash = calculateBlockHash(block);
            boolean isValid = block.getHash().equals(calculatedHash);

            if (!isValid) {
                log.warn("Block hash mismatch. Expected: {}, Got: {}", calculatedHash, block.getHash());
            }

            return isValid;
        } catch (Exception e) {
            log.error("Error validating block hash", e);
            return false;
        }
    }

    @Override
    public boolean validatePreviousHash(Block currentBlock, Block previousBlock) {
        if (currentBlock == null || previousBlock == null) {
            log.warn("Current or previous block is null");
            return false;
        }

        if (currentBlock.getBlockIndex() != previousBlock.getBlockIndex() + 1) {
            log.warn("Block index sequence is invalid");
            return false;
        }

        boolean isValid = currentBlock.getPreviousHash().equals(previousBlock.getHash());

        if (!isValid) {
            log.warn("Previous hash mismatch at block: {}", currentBlock.getBlockIndex());
        }

        return isValid;
    }

    @Override
    public boolean validateTransactionFormat(Transaction transaction) {
        // Validate sender address
        if (!validateAddressFormat(transaction.getSenderAddress())) {
            log.warn("Invalid sender address format: {}", transaction.getSenderAddress());
            return false;
        }

        // Validate recipient address
        if (!validateAddressFormat(transaction.getRecipientAddress())) {
            log.warn("Invalid recipient address format: {}", transaction.getRecipientAddress());
            return false;
        }

        // Validate amount
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(MIN_TRANSACTION_AMOUNT) < 0) {
            log.warn("Invalid transaction amount: {}", transaction.getAmount());
            return false;
        }

        // Validate fee
        if (transaction.getFee() == null || transaction.getFee().compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Invalid transaction fee: {}", transaction.getFee());
            return false;
        }

        // Cannot send to same address
        if (transaction.getSenderAddress().equals(transaction.getRecipientAddress())) {
            log.warn("Sender and recipient addresses are the same");
            return false;
        }

        // Validate timestamp
        if (transaction.getTimestamp() == null || transaction.getTimestamp() <= 0) {
            log.warn("Invalid transaction timestamp");
            return false;
        }

        return true;
    }

    @Override
    public boolean validateAddressFormat(String address) {
        if (address == null || address.isEmpty()) {
            return false;
        }
        return ADDRESS_PATTERN.matcher(address).matches();
    }

    @Override
    public boolean checkDoubleSpending(Transaction transaction) {
        // Check if the same transaction hash already exists
        if (transaction.getTransactionHash() != null) {
            boolean exists = transactionRepository.existsByTransactionHash(transaction.getTransactionHash());
            if (exists) {
                log.warn("Transaction with same hash already exists: {}", transaction.getTransactionHash());
                return true;
            }
        }

        // Check if sender has pending transactions that would exceed balance
        List<Transaction> pendingTransactions = transactionRepository.findBySenderAddress(transaction.getSenderAddress())
                .stream()
                .filter(tx -> tx.getStatus() == TransactionStatus.PENDING)
                .toList();

        var wallet = walletRepository.findByAddress(transaction.getSenderAddress());
        if (wallet.isEmpty()) {
            return true; // Wallet doesn't exist
        }

        BigDecimal totalPendingAmount = pendingTransactions.stream()
                .map(tx -> tx.getAmount().add(tx.getFee()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal newTransactionAmount = transaction.getAmount().add(transaction.getFee());
        BigDecimal totalRequired = totalPendingAmount.add(newTransactionAmount);

        if (wallet.get().getBalance().compareTo(totalRequired) < 0) {
            log.warn("Double spending detected: insufficient balance for pending + new transaction");
            return true;
        }

        return false;
    }

    /**
     * Calculate block hash
     */
    private String calculateBlockHash(Block block) {
        String data = block.getBlockIndex() +
                block.getPreviousHash() +
                block.getTimestamp() +
                block.getNonce() +
                block.getMerkleRoot();
        return cryptographyService.hash(data);
    }

    /**
     * Build transaction data for signing/verification
     */
    private String buildTransactionData(Transaction transaction) {
        return transaction.getSenderAddress() +
                transaction.getRecipientAddress() +
                transaction.getAmount() +
                transaction.getTimestamp();
    }
}