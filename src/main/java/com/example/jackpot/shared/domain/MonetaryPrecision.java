package com.example.jackpot.shared.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class MonetaryPrecision {
    public static final int STAKE_PRECISION = 19;
    public static final int STAKE_SCALE = 2;
    public static final int LEDGER_PRECISION = 25;
    public static final int LEDGER_SCALE = 8;

    private MonetaryPrecision() {}

    public static BigDecimal ledger(BigDecimal value, String name) {
        return Objects.requireNonNull(value, name + " must not be null")
                .setScale(LEDGER_SCALE, RoundingMode.HALF_UP);
    }
}
