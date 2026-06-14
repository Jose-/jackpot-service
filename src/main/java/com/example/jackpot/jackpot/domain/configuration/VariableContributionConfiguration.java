package com.example.jackpot.jackpot.domain.configuration;

import com.example.jackpot.shared.domain.MonetaryPrecision;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;

public record VariableContributionConfiguration(
        BigDecimal initialRatePercentage,
        BigDecimal rateDecreasePerIntervalPercentagePoints,
        BigDecimal poolGrowthIntervalAmount,
        BigDecimal minimumRatePercentage)
        implements ContributionConfiguration {
    public static final String STRATEGY = "VARIABLE";

    private static final int PERCENTAGE_SCALE = 4;
    private static final BigDecimal MAX_PERCENTAGE = BigDecimal.valueOf(100);

    public VariableContributionConfiguration {
        initialRatePercentage =
                Objects.requireNonNull(
                                initialRatePercentage, "initial rate percentage must not be null")
                        .setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP);
        rateDecreasePerIntervalPercentagePoints =
                Objects.requireNonNull(
                                rateDecreasePerIntervalPercentagePoints,
                                "rate decrease per interval percentage points must not be null")
                        .setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP);
        poolGrowthIntervalAmount =
                MonetaryPrecision.ledger(
                        Objects.requireNonNull(
                                poolGrowthIntervalAmount,
                                "pool growth interval amount must not be null"),
                        "poolGrowthIntervalAmount");
        minimumRatePercentage =
                Objects.requireNonNull(
                                minimumRatePercentage, "minimum rate percentage must not be null")
                        .setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP);

        if (!isPositivePercentage(initialRatePercentage)
                || !isPositivePercentage(rateDecreasePerIntervalPercentagePoints)
                || !isPositivePercentage(minimumRatePercentage)) {
            throw new IllegalArgumentException(
                    "Variable contribution rate percentages and percentage points must be in (0, 100]");
        }
        if (minimumRatePercentage.compareTo(initialRatePercentage) > 0) {
            throw new IllegalArgumentException(
                    "Minimum rate percentage cannot exceed initial rate percentage");
        }
        if (poolGrowthIntervalAmount.signum() <= 0) {
            throw new IllegalArgumentException("Pool growth interval amount must be positive");
        }
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
                "initialRatePercentage", initialRatePercentage,
                "rateDecreasePerIntervalPercentagePoints", rateDecreasePerIntervalPercentagePoints,
                "poolGrowthIntervalAmount", poolGrowthIntervalAmount,
                "minimumRatePercentage", minimumRatePercentage);
    }
}
