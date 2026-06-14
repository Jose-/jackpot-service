package com.example.jackpot.jackpot.persistence;

import com.example.jackpot.jackpot.domain.JackpotContribution;
import com.example.jackpot.shared.domain.MonetaryPrecision;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "jackpot_contributions")
public class JackpotContributionEntity {
    @Id private UUID id;

    @Column(nullable = false, unique = true)
    private UUID betId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID jackpotId;

    @Column(
            precision = MonetaryPrecision.STAKE_PRECISION,
            scale = MonetaryPrecision.STAKE_SCALE,
            nullable = false)
    private BigDecimal stakeAmount;

    @Column(
            precision = MonetaryPrecision.LEDGER_PRECISION,
            scale = MonetaryPrecision.LEDGER_SCALE,
            nullable = false)
    private BigDecimal contributionAmount;

    @Column(
            precision = MonetaryPrecision.LEDGER_PRECISION,
            scale = MonetaryPrecision.LEDGER_SCALE,
            nullable = false)
    private BigDecimal currentJackpotAmount;

    @Column(nullable = false)
    private Instant createdAt;

    protected JackpotContributionEntity() {}

    public JackpotContributionEntity(JackpotContribution c) {
        id = UUID.randomUUID();
        betId = c.betId();
        userId = c.userId();
        jackpotId = c.jackpotId();
        stakeAmount = c.stakeAmount();
        contributionAmount = c.contributionAmount();
        currentJackpotAmount = c.currentJackpotAmount();
        createdAt = c.createdAt();
    }

    public JackpotContribution toDomain() {
        return new JackpotContribution(
                betId,
                userId,
                jackpotId,
                stakeAmount,
                contributionAmount,
                currentJackpotAmount,
                createdAt);
    }

    public UUID betId() {
        return betId;
    }

    public UUID jackpotId() {
        return jackpotId;
    }

    public UUID userId() {
        return userId;
    }

    public BigDecimal stakeAmount() {
        return stakeAmount;
    }
}
