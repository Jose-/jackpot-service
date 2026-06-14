package com.example.jackpot.jackpot.domain;

import com.example.jackpot.shared.domain.MonetaryPrecision;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record JackpotContribution(
        UUID betId,
        UUID userId,
        UUID jackpotId,
        BigDecimal stakeAmount,
        BigDecimal contributionAmount,
        BigDecimal currentJackpotAmount,
        Instant createdAt) {

    public JackpotContribution {
        betId = Objects.requireNonNull(betId, "betId must not be null");
        userId = Objects.requireNonNull(userId, "userId must not be null");
        jackpotId = Objects.requireNonNull(jackpotId, "jackpotId must not be null");
        stakeAmount = normalizeStake(stakeAmount);
        contributionAmount = MonetaryPrecision.ledger(contributionAmount, "contributionAmount");
        currentJackpotAmount =
                MonetaryPrecision.ledger(currentJackpotAmount, "currentJackpotAmount");
        createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");

        if (stakeAmount.signum() <= 0) {
            throw new IllegalArgumentException("Stake amount must be positive");
        }
        if (contributionAmount.signum() <= 0) {
            throw new IllegalArgumentException("Contribution amount must be positive");
        }
        if (currentJackpotAmount.signum() <= 0) {
            throw new IllegalArgumentException("Current jackpot amount must be positive");
        }
    }

    private static BigDecimal normalizeStake(BigDecimal value) {
        return Objects.requireNonNull(value, "stakeAmount must not be null")
                .setScale(MonetaryPrecision.STAKE_SCALE, RoundingMode.UNNECESSARY);
    }
}
