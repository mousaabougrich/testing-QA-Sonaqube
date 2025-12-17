package com.wallet.biochain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transaction_pools")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionPool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pool_name", nullable = false)
    private String poolName;

    @Column(name = "max_size")
    private Integer maxSize;

    @Column(name = "current_size")
    private Integer currentSize;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "transactionPool", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> pendingTransactions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (maxSize == null) {
            maxSize = 1000;
        }
        if (currentSize == null) {
            currentSize = 0;
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        currentSize = pendingTransactions != null ? pendingTransactions.size() : 0;
    }

    public TransactionPool(String poolName) {
        this.poolName = poolName;
        this.maxSize = 1000;
        this.currentSize = 0;
        this.isActive = true;
    }

    public boolean isFull() {
        return currentSize >= maxSize;
    }

    public void addTransaction(Transaction transaction) {
        if (!isFull()) {
            pendingTransactions.add(transaction);
            currentSize++;
        }
    }

    public void removeTransaction(Transaction transaction) {
        if (pendingTransactions.remove(transaction)) {
            currentSize--;
        }
    }
}