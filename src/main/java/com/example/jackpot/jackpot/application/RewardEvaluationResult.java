package com.example.jackpot.jackpot.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RewardEvaluationResult(
        UUID betId,
        UUID userId,
        UUID jackpotId,
        boolean won,
        BigDecimal calculatedChance,
        BigDecimal generatedDraw,
        BigDecimal rewardAmount,
        Instant createdAt) {}
