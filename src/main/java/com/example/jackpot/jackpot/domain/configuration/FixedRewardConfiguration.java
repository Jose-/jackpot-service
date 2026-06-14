package com.example.jackpot.jackpot.domain.configuration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;

public record FixedRewardConfiguration(BigDecimal winProbabilityPercentage)
        implements RewardConfiguration {

    public static final String STRATEGY = "FIXED";
    private static final int PERCENTAGE_SCALE = 4;
    private static final BigDecimal MAX_PERCENTAGE = BigDecimal.valueOf(100);

    public FixedRewardConfiguration {
        winProbabilityPercentage =
                Objects.requireNonNull(
                                winProbabilityPercentage,
                                "win probability percentage must not be null")
                        .setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP);

        if (winProbabilityPercentage.signum() < 0
                || winProbabilityPercentage.compareTo(MAX_PERCENTAGE) > 0) {
            throw new IllegalArgumentException(
                    "Fixed reward win probability percentage must be in [0, 100]");
        }
    }

    @Override
    public String strategy() {
        return STRATEGY;
    }

    @Override
    public Map<String, BigDecimal> parameters() {
        return Map.of("chance", winProbabilityPercentage);
    }
}
