package com.example.jackpot.jackpot.persistence;

import com.example.jackpot.shared.domain.MonetaryPrecision;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "jackpot_reward_evaluations")
public class JackpotRewardEvaluationEntity {
    @Id private UUID id;

    @Column(nullable = false, unique = true)
    private UUID betId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID jackpotId;

    @Column(nullable = false)
    private boolean won;

    @Column(precision = 7, scale = 4, nullable = false)
    private BigDecimal calculatedChance;

    @Column(precision = 7, scale = 4, nullable = false)
    private BigDecimal generatedDraw;

    @Column(precision = MonetaryPrecision.LEDGER_PRECISION, scale = MonetaryPrecision.LEDGER_SCALE)
    private BigDecimal rewardAmount;

    @Column(nullable = false)
    private Instant createdAt;

    protected JackpotRewardEvaluationEntity() {}

    public JackpotRewardEvaluationEntity(
            UUID betId,
            UUID userId,
            UUID jackpotId,
            boolean won,
            BigDecimal chance,
            BigDecimal draw,
            BigDecimal reward,
            Instant now) {
        id = UUID.randomUUID();
        this.betId = betId;
        this.userId = userId;
        this.jackpotId = jackpotId;
        this.won = won;
        calculatedChance = chance;
        generatedDraw = draw;
        rewardAmount = reward;
        createdAt = now;
    }

    public UUID betId() {
        return betId;
    }

    public UUID userId() {
        return userId;
    }

    public UUID jackpotId() {
        return jackpotId;
    }

    public boolean won() {
        return won;
    }

    public BigDecimal calculatedChance() {
        return calculatedChance;
    }

    public BigDecimal generatedDraw() {
        return generatedDraw;
    }

    public BigDecimal rewardAmount() {
        return rewardAmount;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
