package com.wallet.biochain.mappers;

import com.wallet.biochain.dto.TransactionResponseDTO;
import com.wallet.biochain.dto.TransactionHistoryDTO;
import com.wallet.biochain.dto.TransactionBroadcastDTO;
import com.wallet.biochain.entities.Transaction;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransactionMapper {

    public TransactionResponseDTO toResponseDTO(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        return new TransactionResponseDTO(
                transaction.getId(),
                transaction.getTransactionHash(),
                transaction.getSenderAddress(),
                transaction.getRecipientAddress(),
                transaction.getAmount(),
                transaction.getFee(),
                transaction.getStatus(),
                transaction.getConfirmationCount(),
                transaction.getTimestamp(),
                transaction.getCreatedAt(),
                transaction.getMemo()
        );
    }

    public List<TransactionResponseDTO> toResponseDTOList(List<Transaction> transactions) {
        if (transactions == null) {
            return null;
        }

        return transactions.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public TransactionHistoryDTO toHistoryDTO(Transaction transaction, String walletAddress) {
        if (transaction == null) {
            return null;
        }

        String type = transaction.getSenderAddress().equals(walletAddress) ? "SENT" : "RECEIVED";

        LocalDateTime timestamp = transaction.getTimestamp() != null
                ? LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(transaction.getTimestamp()),
                ZoneId.systemDefault()
        )
                : transaction.getCreatedAt();

        return new TransactionHistoryDTO(
                transaction.getTransactionHash(),
                transaction.getSenderAddress(),
                transaction.getRecipientAddress(),
                transaction.getAmount(),
                transaction.getFee(),
                transaction.getStatus(),
                transaction.getConfirmationCount(),
                type,
                timestamp,
                transaction.getBlock() != null ? transaction.getBlock().getBlockIndex() : null,
                transaction.getMemo()
        );
    }

    public List<TransactionHistoryDTO> toHistoryDTOList(List<Transaction> transactions, String walletAddress) {
        if (transactions == null) {
            return null;
        }

        return transactions.stream()
                .map(tx -> toHistoryDTO(tx, walletAddress))
                .collect(Collectors.toList());
    }

    public TransactionBroadcastDTO toBroadcastDTO(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        return new TransactionBroadcastDTO(
                transaction.getTransactionHash(),
                transaction.getSenderAddress(),
                transaction.getRecipientAddress(),
                transaction.getAmount(),
                transaction.getSignature(),
                transaction.getTimestamp()
        );
    }
}