package com.example.jackpot.jackpot.domain.configuration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;

public record FixedContributionConfiguration(BigDecimal ratePercentage)
        implements ContributionConfiguration {
    public static final String STRATEGY = "FIXED";

    private static final int PERCENTAGE_SCALE = 4;
    private static final BigDecimal MAX_PERCENTAGE = BigDecimal.valueOf(100);

    public FixedContributionConfiguration {
        ratePercentage =
                Objects.requireNonNull(ratePercentage, "rate percentage must not be null")
                        .setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP);

        if (ratePercentage.signum() <= 0 || ratePercentage.compareTo(MAX_PERCENTAGE) > 0) {
            throw new IllegalArgumentException(
                    "Fixed contribution rate percentage must be in (0, 100]");
        }
    }

    @Override
    public String strategy() {
        return STRATEGY;
    }

    @Override
    public Map<String, BigDecimal> parameters() {
        return Map.of("percentage", ratePercentage);
    }
}
