package com.wallet.biochain.services.impl;

import com.wallet.biochain.dto.TransactionHistoryDTO;
import com.wallet.biochain.dto.TransactionRequestDTO;
import com.wallet.biochain.dto.TransactionResponseDTO;
import com.wallet.biochain.entities.Transaction;
import com.wallet.biochain.entities.Wallet;
import com.wallet.biochain.enums.TransactionStatus;
import com.wallet.biochain.mappers.TransactionMapper;
import com.wallet.biochain.repositories.BlockRepository;
import com.wallet.biochain.repositories.TransactionRepository;
import com.wallet.biochain.repositories.WalletRepository;
import com.wallet.biochain.services.CryptographyService;
import com.wallet.biochain.services.P2PNetworkService;
import com.wallet.biochain.services.TransactionService;
import com.wallet.biochain.services.ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final BlockRepository blockRepository;
    private final CryptographyService cryptographyService;
    private final ValidationService validationService;
    private final P2PNetworkService p2pNetworkService;
    private final TransactionMapper transactionMapper;

    private static final BigDecimal FEE_PERCENTAGE = new BigDecimal("0.001"); // 0.1% fee
    private static final BigDecimal MIN_FEE = new BigDecimal("0.00001");

    @Override
    @Transactional
    public TransactionResponseDTO createTransaction(TransactionRequestDTO request) {
        log.info("Creating transaction from {} to {}", request.senderAddress(), request.recipientAddress());

        // Validate wallets exist
        Wallet senderWallet = walletRepository.findByAddress(request.senderAddress())
                .orElseThrow(() -> new IllegalArgumentException("Sender wallet not found"));

        Wallet recipientWallet = walletRepository.findByAddress(request.recipientAddress())
                .orElseThrow(() -> new IllegalArgumentException("Recipient wallet not found"));

        // Calculate fee if not provided
        BigDecimal fee = request.fee() != null ? request.fee() : calculateTransactionFee(request.amount());

        // Validate sufficient balance
        BigDecimal totalRequired = request.amount().add(fee);
        if (senderWallet.getBalance().compareTo(totalRequired) < 0) {
            throw new IllegalStateException("Insufficient balance. Required: " + totalRequired + ", Available: " + senderWallet.getBalance());
        }

        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setSenderAddress(request.senderAddress());
        transaction.setRecipientAddress(request.recipientAddress());
        transaction.setAmount(request.amount());
        transaction.setFee(fee);
        transaction.setTimestamp(System.currentTimeMillis());
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setMemo(request.memo());
        transaction.setSenderWallet(senderWallet);
        transaction.setRecipientWallet(recipientWallet);

        // Generate transaction hash
        String txData = buildTransactionData(transaction);
        String txHash = cryptographyService.hash(txData);
        transaction.setTransactionHash(txHash);

        // Sign transaction
        if (request.privateKey() != null && !request.privateKey().isEmpty()) {
            String signature = signTransaction(transaction, request.privateKey());
            transaction.setSignature(signature);
        } else {
            // In production, retrieve private key securely
            throw new IllegalArgumentException("Private key is required for signing");
        }

        // Validate transaction
        if (!validationService.validateTransaction(transaction)) {
            throw new IllegalStateException("Transaction validation failed");
        }

        // Save transaction
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created successfully: {}", txHash);

        // Broadcast to network (asynchronous)
        try {
            broadcastTransaction(savedTransaction);
        } catch (Exception e) {
            log.error("Failed to broadcast transaction, but transaction was saved", e);
        }

        return transactionMapper.toResponseDTO(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TransactionResponseDTO> getTransactionByHash(String transactionHash) {
        log.debug("Fetching transaction by hash: {}", transactionHash);
        return transactionRepository.findByTransactionHash(transactionHash)
                .map(transactionMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionHistoryDTO> getTransactionHistory(String walletAddress) {
        log.debug("Fetching transaction history for wallet: {}", walletAddress);
        List<Transaction> transactions = transactionRepository.findByWalletAddressOrderByTimestampDesc(walletAddress);
        return transactionMapper.toHistoryDTOList(transactions, walletAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getSentTransactions(String walletAddress) {
        log.debug("Fetching sent transactions for wallet: {}", walletAddress);
        return transactionMapper.toResponseDTOList(
                transactionRepository.findBySenderAddress(walletAddress)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getReceivedTransactions(String walletAddress) {
        log.debug("Fetching received transactions for wallet: {}", walletAddress);
        return transactionMapper.toResponseDTOList(
                transactionRepository.findByRecipientAddress(walletAddress)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getPendingTransactions() {
        log.debug("Fetching pending transactions");
        return transactionMapper.toResponseDTOList(
                transactionRepository.findByStatus(TransactionStatus.PENDING)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getConfirmedTransactions() {
        log.debug("Fetching confirmed transactions");
        return transactionMapper.toResponseDTOList(
                transactionRepository.findByStatus(TransactionStatus.CONFIRMED)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateTransaction(Transaction transaction) {
        return validationService.validateTransaction(transaction);
    }

    @Override
    public String signTransaction(Transaction transaction, String privateKey) {
        log.debug("Signing transaction: {}", transaction.getTransactionHash());

        String dataToSign = buildTransactionData(transaction);
        String signature = cryptographyService.sign(dataToSign, privateKey);

        log.debug("Transaction signed successfully");
        return signature;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifyTransactionSignature(Transaction transaction) {
        return validationService.validateSignature(transaction);
    }

    @Override
    public void broadcastTransaction(Transaction transaction) {
        log.info("Broadcasting transaction to network: {}", transaction.getTransactionHash());

        try {
            p2pNetworkService.broadcastTransaction(transaction);
            log.info("Transaction broadcasted successfully");
        } catch (Exception e) {
            log.error("Failed to broadcast transaction", e);
            throw new RuntimeException("Failed to broadcast transaction", e);
        }
    }

    @Override
    @Transactional
    public void confirmTransaction(String transactionHash, Long blockId) {
        log.info("Confirming transaction: {}", transactionHash);

        Transaction transaction = transactionRepository.findByTransactionHash(transactionHash)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionHash));

        var block = blockRepository.findById(blockId)
                .orElseThrow(() -> new IllegalArgumentException("Block not found: " + blockId));

        // Update transaction status
        transaction.setStatus(TransactionStatus.CONFIRMED);
        transaction.setBlock(block);
        transaction.setConfirmationCount(1);

        transactionRepository.save(transaction);

        // Update wallet balances
        updateWalletBalances(transaction);

        log.info("Transaction confirmed successfully: {}", transactionHash);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTransactionCount(String walletAddress) {
        log.debug("Counting transactions for wallet: {}", walletAddress);
        return transactionRepository.countTransactionsByAddress(walletAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalTransactionVolume() {
        log.debug("Calculating total transaction volume");
        BigDecimal volume = transactionRepository.getTotalTransactionVolume();
        return volume != null ? volume : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculateTransactionFee(BigDecimal amount) {
        BigDecimal calculatedFee = amount.multiply(FEE_PERCENTAGE).setScale(8, RoundingMode.HALF_UP);

        // Ensure minimum fee
        if (calculatedFee.compareTo(MIN_FEE) < 0) {
            return MIN_FEE;
        }

        return calculatedFee;
    }

    /**
     * Build transaction data for hashing/signing
     */
    private String buildTransactionData(Transaction transaction) {
        return transaction.getSenderAddress() +
                transaction.getRecipientAddress() +
                transaction.getAmount().toPlainString() +
                transaction.getFee().toPlainString() +
                transaction.getTimestamp();
    }

    /**
     * Update wallet balances after transaction confirmation
     */
    private void updateWalletBalances(Transaction transaction) {
        // Deduct from sender
        Wallet senderWallet = transaction.getSenderWallet();
        if (senderWallet != null) {
            BigDecimal totalDeduction = transaction.getAmount().add(transaction.getFee());
            senderWallet.setBalance(senderWallet.getBalance().subtract(totalDeduction));
            walletRepository.save(senderWallet);
            log.debug("Deducted {} from sender wallet: {}", totalDeduction, senderWallet.getAddress());
        }

        // Add to recipient
        Wallet recipientWallet = transaction.getRecipientWallet();
        if (recipientWallet != null) {
            recipientWallet.setBalance(recipientWallet.getBalance().add(transaction.getAmount()));
            walletRepository.save(recipientWallet);
            log.debug("Added {} to recipient wallet: {}", transaction.getAmount(), recipientWallet.getAddress());
        }
    }
}