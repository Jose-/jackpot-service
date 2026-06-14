package com.example.jackpot.jackpot.domain;

import com.example.jackpot.shared.domain.MonetaryPrecision;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record JackpotReward(
        UUID betId, UUID userId, UUID jackpotId, BigDecimal rewardAmount, Instant createdAt) {

    public JackpotReward {
        betId = Objects.requireNonNull(betId, "betId must not be null");
        userId = Objects.requireNonNull(userId, "userId must not be null");
        jackpotId = Objects.requireNonNull(jackpotId, "jackpotId must not be null");
        rewardAmount =
                MonetaryPrecision.ledger(
                        Objects.requireNonNull(rewardAmount, "rewardAmount must not be null"),
                        "rewardAmount");
        createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");

        if (rewardAmount.signum() <= 0) {
            throw new IllegalArgumentException("Reward amount must be positive");
        }
    }
}
