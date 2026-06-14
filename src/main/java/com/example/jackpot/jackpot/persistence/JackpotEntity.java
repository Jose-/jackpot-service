package com.example.jackpot.jackpot.persistence;

import com.example.jackpot.jackpot.domain.Jackpot;
import com.example.jackpot.jackpot.domain.configuration.StoredContributionConfiguration;
import com.example.jackpot.jackpot.domain.configuration.StoredRewardConfiguration;
import com.example.jackpot.shared.domain.MonetaryPrecision;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "jackpots")
public class JackpotEntity {
    @Id private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(
            name = "initial_pool_amount",
            precision = MonetaryPrecision.LEDGER_PRECISION,
            scale = MonetaryPrecision.LEDGER_SCALE,
            nullable = false)
    private BigDecimal initialPool;

    @Column(
            name = "current_pool_amount",
            precision = MonetaryPrecision.LEDGER_PRECISION,
            scale = MonetaryPrecision.LEDGER_SCALE,
            nullable = false)
    private BigDecimal currentPool;

    @Column(name = "contribution_strategy", nullable = false)
    private String contributionStrategy;

    @Convert(converter = ContributionParametersConverter.class)
    @Column(name = "contribution_parameters", nullable = false, length = 2000)
    private Map<String, BigDecimal> contributionParameters;

    @Column(name = "reward_strategy", nullable = false)
    private String rewardStrategy;

    @Convert(converter = RewardParametersConverter.class)
    @Column(name = "reward_parameters", nullable = false, length = 2000)
    private Map<String, BigDecimal> rewardParameters;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version private long version;

    protected JackpotEntity() {}

    public JackpotEntity(String name, Jackpot jackpot) {
        this.name = Objects.requireNonNull(name);
        this.id = jackpot.id();
        this.createdAt = jackpot.createdAt();
        apply(jackpot);
    }

    public Jackpot toDomain() {
        return new Jackpot(
                id,
                initialPool,
                currentPool,
                new StoredContributionConfiguration(contributionStrategy, contributionParameters),
                new StoredRewardConfiguration(rewardStrategy, rewardParameters),
                createdAt);
    }

    public void apply(Jackpot jackpot) {
        initialPool = jackpot.initialPool();
        currentPool = jackpot.currentPool();
        updatedAt = jackpot.updatedAt();
        contributionStrategy = jackpot.contributionConfiguration().strategy();
        contributionParameters = jackpot.contributionConfiguration().parameters();
        rewardStrategy = jackpot.rewardConfiguration().strategy();
        rewardParameters = jackpot.rewardConfiguration().parameters();
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public BigDecimal currentPool() {
        return currentPool;
    }
}
