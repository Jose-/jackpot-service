package com.example.jackpot.jackpot.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record RewardDecision(boolean won, BigDecimal calculatedChance, BigDecimal generatedDraw) {

    private static final int PROBABILITY_SCALE = 4;
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    public RewardDecision {
        calculatedChance =
                Objects.requireNonNull(calculatedChance, "calculatedChance must not be null")
                        .setScale(PROBABILITY_SCALE, RoundingMode.HALF_UP);
        generatedDraw =
                Objects.requireNonNull(generatedDraw, "generatedDraw must not be null")
                        .setScale(PROBABILITY_SCALE, RoundingMode.DOWN);
        if (calculatedChance.signum() < 0 || calculatedChance.compareTo(ONE_HUNDRED) > 0) {
            throw new IllegalArgumentException("Calculated chance must be in [0, 100]");
        }
        if (generatedDraw.signum() < 0 || generatedDraw.compareTo(ONE_HUNDRED) >= 0) {
            throw new IllegalArgumentException("Generated draw must be in [0, 100)");
        }
        if (won != (generatedDraw.compareTo(calculatedChance) < 0)) {
            throw new IllegalArgumentException("Reward outcome must match chance and draw");
        }
    }
}
