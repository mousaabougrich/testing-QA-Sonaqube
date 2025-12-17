package com.wallet.biochain.entities;

import com.wallet.biochain.enums.ConsensusType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "blockchains")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Blockchain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chain_id", nullable = false, unique = true)
    private String chainId;

    @Column(nullable = false)
    private String name;

    @Column(name = "genesis_hash", nullable = false)
    private String genesisHash;

    @Column(name = "current_height", nullable = false)
    private Integer currentHeight;

    @Column(nullable = false)
    private Integer difficulty;

    @Column(name = "block_reward", precision = 20, scale = 8)
    private java.math.BigDecimal blockReward;

    @Column(name = "block_time_seconds")
    private Integer blockTimeSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "consensus_type", nullable = false)
    private ConsensusType consensusType;

    @Column(name = "is_valid")
    private Boolean isValid;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "blockchain", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Block> blocks = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (currentHeight == null) {
            currentHeight = 0;
        }
        if (difficulty == null) {
            difficulty = 4;
        }
        if (isValid == null) {
            isValid = true;
        }
        if (blockTimeSeconds == null) {
            blockTimeSeconds = 600; // 10 minutes default
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Blockchain(String chainId, String name, String genesisHash, ConsensusType consensusType) {
        this.chainId = chainId;
        this.name = name;
        this.genesisHash = genesisHash;
        this.consensusType = consensusType;
        this.currentHeight = 0;
        this.difficulty = 4;
        this.isValid = true;
    }

}