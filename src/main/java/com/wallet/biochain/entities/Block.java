package com.wallet.biochain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "blocks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "block_index", nullable = false, unique = true)
    private Integer blockIndex;

    @Column(name = "previous_hash", nullable = false)
    private String previousHash;

    @Column(nullable = false)
    private String hash;

    @Column(nullable = false)
    private Long timestamp;

    @Column(nullable = false)
    private Integer nonce;

    @Column(name = "difficulty", nullable = false)
    private Integer difficulty;

    @Column(name = "merkle_root")
    private String merkleRoot;

    @Column(name = "miner_address")
    private String minerAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blockchain_id")
    private Blockchain blockchain;

    @OneToMany(mappedBy = "block", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
        if (nonce == null) {
            nonce = 0;
        }
        if (difficulty == null) {
            difficulty = 4;
        }
    }

    public Block(Integer blockIndex, String previousHash, Long timestamp, List<Transaction> transactions) {
        this.blockIndex = blockIndex;
        this.previousHash = previousHash;
        this.timestamp = timestamp;
        this.transactions = transactions;
        this.nonce = 0;
        this.difficulty = 4;
    }

    public Block(Integer blockIndex, String previousHash, String hash, Long timestamp, Integer nonce) {
        this.blockIndex = blockIndex;
        this.previousHash = previousHash;
        this.hash = hash;
        this.timestamp = timestamp;
        this.nonce = nonce;
    }
}