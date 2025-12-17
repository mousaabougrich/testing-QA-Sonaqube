package com.wallet.biochain.entities;
import com.wallet.biochain.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_hash", nullable = false, unique = true)
    private String transactionHash;

    @Column(name = "sender_address", nullable = false)
    private String senderAddress;

    @Column(name = "recipient_address", nullable = false)
    private String recipientAddress;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal amount;

    @Column(columnDefinition = "TEXT")
    private String signature;

    @Column(nullable = false)
    private Long timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(name = "confirmation_count")
    private Integer confirmationCount;

    @Column(name = "fee", precision = 20, scale = 8)
    private BigDecimal fee;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_wallet_id")
    private Wallet senderWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_wallet_id")
    private Wallet recipientWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id")
    private Block block;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_pool_id")
    private TransactionPool transactionPool;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
        if (status == null) {
            status = TransactionStatus.PENDING;
        }
        if (confirmationCount == null) {
            confirmationCount = 0;
        }
        if (fee == null) {
            fee = BigDecimal.ZERO;
        }
    }

    public Transaction(String senderAddress, String recipientAddress, BigDecimal amount) {
        this.senderAddress = senderAddress;
        this.recipientAddress = recipientAddress;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
        this.status = TransactionStatus.PENDING;
        this.confirmationCount = 0;
    }

    public Transaction(String senderAddress, String recipientAddress, BigDecimal amount, String signature) {
        this.senderAddress = senderAddress;
        this.recipientAddress = recipientAddress;
        this.amount = amount;
        this.signature = signature;
        this.timestamp = System.currentTimeMillis();
        this.status = TransactionStatus.PENDING;
    }

}