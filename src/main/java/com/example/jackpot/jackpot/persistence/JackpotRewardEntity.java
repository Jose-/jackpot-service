package com.example.jackpot.jackpot.persistence;

import com.example.jackpot.jackpot.domain.JackpotReward;
import com.example.jackpot.shared.domain.MonetaryPrecision;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "jackpot_rewards")
public class JackpotRewardEntity {
    @Id private UUID id;

    @Column(nullable = false, unique = true)
    private UUID betId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID jackpotId;

    @Column(
            name = "jackpot_reward_amount",
            precision = MonetaryPrecision.LEDGER_PRECISION,
            scale = MonetaryPrecision.LEDGER_SCALE,
            nullable = false)
    private BigDecimal rewardAmount;

    @Column(nullable = false)
    private Instant createdAt;

    protected JackpotRewardEntity() {}

    public JackpotRewardEntity(JackpotReward r) {
        id = UUID.randomUUID();
        betId = r.betId();
        userId = r.userId();
        jackpotId = r.jackpotId();
        rewardAmount = r.rewardAmount();
        createdAt = r.createdAt();
    }
}
