package com.example.jackpot.jackpot.domain.configuration;

import com.example.jackpot.shared.domain.MonetaryPrecision;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;

public record VariableRewardConfiguration(
        BigDecimal initialWinProbabilityPercentage,
        BigDecimal winProbabilityIncreasePerIntervalPercentagePoints,
        BigDecimal poolGrowthIntervalAmount,
        BigDecimal guaranteedWinPoolAmount)
        implements RewardConfiguration {

    public static final String STRATEGY = "VARIABLE";
    private static final int PERCENTAGE_SCALE = 4;
    private static final BigDecimal MAX_PERCENTAGE = BigDecimal.valueOf(100);

    public VariableRewardConfiguration {
        initialWinProbabilityPercentage =
                Objects.requireNonNull(
                                initialWinProbabilityPercentage,
                                "initial win probability percentage must not be null")
                        .setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP);
        winProbabilityIncreasePerIntervalPercentagePoints =
                Objects.requireNonNull(
                                winProbabilityIncreasePerIntervalPercentagePoints,
                                "win probability increase per interval percentage points must not be null")
                        .setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP);
        poolGrowthIntervalAmount =
                MonetaryPrecision.ledger(
                        Objects.requireNonNull(
                                poolGrowthIntervalAmount,
                                "pool growth interval amount must not be null"),
                        "poolGrowthIntervalAmount");
        guaranteedWinPoolAmount =
                MonetaryPrecision.ledger(
                        Objects.requireNonNull(
                                guaranteedWinPoolAmount,
                                "guaranteed win pool amount must not be null"),
                        "guaranteedWinPoolAmount");

        if (!isPercentage(initialWinProbabilityPercentage)) {
            throw new IllegalArgumentException(
                    "Initial win probability percentage must be in [0, 100]");
        }
        if (!isPositivePercentage(winProbabilityIncreasePerIntervalPercentagePoints)) {
            throw new IllegalArgumentException(
                    "Win probability increase per interval percentage points must be in (0, 100]");
        }
        if (poolGrowthIntervalAmount.signum() <= 0) {
            throw new IllegalArgumentException("Pool growth interval amount must be positive");
        }
        if (guaranteedWinPoolAmount.signum() < 0) {
            throw new IllegalArgumentException("Guaranteed win pool amount must be non-negative");
        }
    }

    private static boolean isPercentage(BigDecimal value) {
        return value.signum() >= 0 && value.compareTo(MAX_PERCENTAGE) <= 0;
    }

    private static boolean isPositivePercentage(BigDecimal value) {
        return value.signum() > 0 && value.compareTo(MAX_PERCENTAGE) <= 0;
    }

    @Override
    public String strategy() {
        return STRATEGY;
    }

    @Override
    public Map<String, BigDecimal> parameters() {
        return Map.of(
                "initialWinProbabilityPercentage", initialWinProbabilityPercentage,
                "winProbabilityIncreasePerIntervalPercentagePoints",
                        winProbabilityIncreasePerIntervalPercentagePoints,
                "poolGrowthIntervalAmount", poolGrowthIntervalAmount,
                "guaranteedWinPoolAmount", guaranteedWinPoolAmount);
    }

    @Override
    public void validateFor(BigDecimal initialPool) {
        if (guaranteedWinPoolAmount.compareTo(initialPool) < 0) {
            throw new IllegalArgumentException(
                    "Guaranteed win pool amount cannot be below initial pool");
        }
    }
}
