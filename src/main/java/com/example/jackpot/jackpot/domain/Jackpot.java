package com.example.jackpot.jackpot.domain;

import com.example.jackpot.jackpot.domain.configuration.ContributionConfiguration;
import com.example.jackpot.jackpot.domain.configuration.RewardConfiguration;
import com.example.jackpot.jackpot.domain.policy.ContributionPolicy;
import com.example.jackpot.shared.domain.MonetaryPrecision;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Jackpot {
    private final UUID id;
    private final BigDecimal initialPool;
    private final ContributionConfiguration contributionConfiguration;
    private final RewardConfiguration rewardConfiguration;
    private final Instant createdAt;
    private BigDecimal currentPool;
    private Instant updatedAt;

    public Jackpot(
            UUID id,
            BigDecimal initialPool,
            BigDecimal currentPool,
            ContributionConfiguration contributionConfiguration,
            RewardConfiguration rewardConfiguration,
            Instant now) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.initialPool = normalizeMoney(initialPool, "initialPool");
        this.currentPool = normalizeMoney(currentPool, "currentPool");
        this.contributionConfiguration =
                Objects.requireNonNull(
                        contributionConfiguration, "contributionConfiguration must not be null");
        this.rewardConfiguration =
                Objects.requireNonNull(rewardConfiguration, "rewardConfiguration must not be null");
        this.createdAt = Objects.requireNonNull(now, "now must not be null");
        this.updatedAt = now;

        validateInvariants();
    }

    public JackpotContribution contribute(Bet bet, ContributionPolicy policy, Instant now) {
        Objects.requireNonNull(bet, "bet must not be null");
        Objects.requireNonNull(policy, "policy must not be null");
        now = Objects.requireNonNull(now, "now must not be null");

        if (!id.equals(bet.jackpotId())) {
            throw new IllegalArgumentException("Bet targets a different jackpot");
        }
        if (!policy.strategy().equals(contributionConfiguration.strategy())) {
            throw new IllegalArgumentException(
                    "Contribution policy does not match jackpot configuration");
        }

        BigDecimal calculatedContribution =
                Objects.requireNonNull(
                        policy.calculate(bet.amount(), this),
                        "calculated contribution must not be null");
        BigDecimal contributionAmount =
                MonetaryPrecision.ledger(calculatedContribution, "calculatedContribution");
        if (contributionAmount.signum() <= 0) {
            throw new IllegalArgumentException("Contribution amount must be positive");
        }
        currentPool = MonetaryPrecision.ledger(currentPool.add(contributionAmount), "currentPool");
        updatedAt = now;

        return new JackpotContribution(
                bet.id(), bet.userId(), id, bet.amount(), contributionAmount, currentPool, now);
    }

    public BigDecimal rewardAndReset(Instant now) {
        updatedAt = Objects.requireNonNull(now, "now must not be null");
        BigDecimal reward = currentPool;
        currentPool = initialPool;
        return reward;
    }

    public UUID id() {
        return id;
    }

    public BigDecimal initialPool() {
        return initialPool;
    }

    public BigDecimal currentPool() {
        return currentPool;
    }

    public ContributionConfiguration contributionConfiguration() {
        return contributionConfiguration;
    }

    public RewardConfiguration rewardConfiguration() {
        return rewardConfiguration;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    private void validateInvariants() {
        if (initialPool.signum() <= 0) {
            throw new IllegalArgumentException("Initial pool must be positive");
        }
        if (currentPool.compareTo(initialPool) < 0) {
            throw new IllegalArgumentException("Current pool cannot be below initial pool");
        }
        rewardConfiguration.validateFor(initialPool);
    }

    private static BigDecimal normalizeMoney(BigDecimal value, String name) {
        return MonetaryPrecision.ledger(value, name);
    }
}
